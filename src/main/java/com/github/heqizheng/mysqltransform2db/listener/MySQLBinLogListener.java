package com.github.heqizheng.mysqltransform2db.listener;

import com.github.heqizheng.mysqltransform2db.configure.Transform2DBProperties;
import com.github.heqizheng.mysqltransform2db.entity.BinLogItem;
import com.github.heqizheng.mysqltransform2db.entity.Column;
import com.github.heqizheng.mysqltransform2db.util.SpringContextUtil;
import com.github.heqizheng.mysqltransform2db.util.BinLogUtil;
import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import com.github.shyiko.mysql.binlog.event.deserialization.NullEventDataDeserializer;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 数据库监听器
 * @author heqizheng
 * @version 1.0
 * @date 2022/3/25
 */
public class MySQLBinLogListener implements BinaryLogClient.EventListener {

    private final BinaryLogClient parseClient;
    private final BlockingQueue<BinLogItem> queue;
    private final ExecutorService consumer;
    /**存放每张数据表对应的listener*/
    private final Multimap<String, DefaultTransform2DBListener> listeners;
    private final Map<String, Map<String, Column>> dbTableCols;
    private String dbTable;
    /**默认监听事件*/
    private static final List<EventType> DEFAULT_EVENT = Arrays.asList(EventType.EXT_UPDATE_ROWS, EventType.EXT_DELETE_ROWS, EventType.EXT_WRITE_ROWS);


    /**
     * 监听器初始化
     */
    public MySQLBinLogListener(Transform2DBProperties dbProperties) {
        Transform2DBProperties.DataSource conf = dbProperties.getDataSource();
        BinaryLogClient client = new BinaryLogClient(conf.getHost(), conf.getPort(), conf.getSchema(),
                conf.getUsername(), conf.getPassword());
        //跳过设置的事件
        setEventDeserializer(client, dbProperties.getSkipEventTypes());
        //序列化
        this.parseClient = client;
        this.queue = new ArrayBlockingQueue<>(1024);
        this.listeners = ArrayListMultimap.create();
        this.dbTableCols = new ConcurrentHashMap<>();
        this.consumer = Executors.newFixedThreadPool(dbProperties.getConsumerThreads());
    }

    private void setEventDeserializer(BinaryLogClient client, EventType[] eventTypes) {
        EventDeserializer eventDeserializer = new EventDeserializer();
        for (EventType eventType : eventTypes) {
            eventDeserializer.setEventDataDeserializer(eventType, new NullEventDataDeserializer());
        }
        client.setEventDeserializer(eventDeserializer);
    }

    /**
     * 监听处理
     *
     * @param event
     */
    @Override
    public void onEvent(Event event) {
        EventType eventType = event.getHeader().getEventType();

        if (eventType == EventType.TABLE_MAP) {
            TableMapEventData tableData = event.getData();
            String db = tableData.getDatabase();
            String table = tableData.getTable();
            dbTable = BinLogUtil.getDbTable(db, table);
        }

        // 只处理添加删除更新三种操作
        if(DEFAULT_EVENT.contains(eventType) && event.getData() != null) {
            switch (eventType) {
                case EXT_UPDATE_ROWS:
                    for (Map.Entry<Serializable[], Serializable[]> row : ((UpdateRowsEventData)event.getData()).getRows()) {
                        if (dbTableCols.containsKey(dbTable)) {
                            BinLogItem item = BinLogItem.itemFromUpdate(row, dbTableCols.get(dbTable), eventType);
                            item.setDbTable(dbTable);
                            queue.add(item);
                        }
                    }
                    break;
                case EXT_WRITE_ROWS:
                    WriteRowsEventData write = event.getData();
                    for (Serializable[] row : write.getRows()) {
                        if (dbTableCols.containsKey(dbTable)) {
                            BinLogItem item = BinLogItem.itemFromInsertOrDeleted(row, dbTableCols.get(dbTable), eventType);
                            item.setDbTable(dbTable);
                            queue.add(item);
                        }
                    }
                    break;
                case EXT_DELETE_ROWS:
                    DeleteRowsEventData delete = event.getData();
                    for (Serializable[] row : delete.getRows()) {
                        if (dbTableCols.containsKey(dbTable)) {
                            BinLogItem item = BinLogItem.itemFromInsertOrDeleted(row, dbTableCols.get(dbTable), eventType);
                            item.setDbTable(dbTable);
                            queue.add(item);
                        }
                    }
                    break;
                default: break;
            }
        }
    }

    /**
     * 注册监听
     *
     * @param dataSource 数据源
     * @param table    操作表
     * @param listener 监听器
     * @throws Exception 异常
     */
    public void regListener(Transform2DBProperties.DataSource dataSource, String table, DefaultTransform2DBListener listener) throws Exception {
        String dbTable = BinLogUtil.getDbTable(dataSource.getSchema(), table);
        // 获取字段集合
        Map<String, Column> cols = BinLogUtil.getColMap(dataSource, table);
        // 保存字段信息
        dbTableCols.put(dbTable, cols);
        // 保存当前注册的listener
        listeners.put(dbTable, (DefaultTransform2DBListener) SpringContextUtil.getBean(dbTable));
    }

    /**
     * 开启多线程消费
     *
     * @throws IOException
     */
    @SuppressWarnings("InfiniteLoopStatement")
    public void parse(Integer consumerThreads, Long queueSleep) throws IOException {
        parseClient.registerEventListener(this);
        for (int i = 0; i < consumerThreads; i++) {
            consumer.submit(() -> {
                while (true) {
                    if (queue.size() > 0) {
                        try {
                            BinLogItem item = queue.take();
                            String dbTable = item.getDbTable();
                            listeners.get(dbTable).forEach(binLogListener -> binLogListener.transform2DB(item));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    TimeUnit.MILLISECONDS.sleep(queueSleep);
                }
            });
        }
        parseClient.connect();
    }
}
