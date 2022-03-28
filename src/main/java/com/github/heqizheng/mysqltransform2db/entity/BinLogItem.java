package com.github.heqizheng.mysqltransform2db.entity;

import com.github.heqizheng.mysqltransform2db.util.BinLogUtil;
import com.github.shyiko.mysql.binlog.event.EventType;
import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Map;

/**
 * binlog对象
 * @author heqizheng
 * @version 1.0
 * @date 2022/3/25
 */
public class BinLogItem implements Serializable {

    private String dbTable;
    private EventType eventType;
    private Long timestamp = null;
    private Long serverId = null;
    /**存储字段-之前的值之后的值*/
    private Map<String, Serializable> before = Maps.newHashMap();
    private Map<String, Serializable> after = Maps.newHashMap();
    /**存储字段--类型*/
    private Map<String, Column> columns = null;

    public String getDbTable() {
        return dbTable;
    }

    public void setDbTable(String dbTable) {
        this.dbTable = dbTable;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

    public Map<String, Serializable> getBefore() {
        return before;
    }

    public void setBefore(Map<String, Serializable> before) {
        this.before = before;
    }

    public Map<String, Serializable> getAfter() {
        return after;
    }

    public void setAfter(Map<String, Serializable> after) {
        this.after = after;
    }

    public Map<String, Column> getColumns() {
        return columns;
    }

    public void setColumns(Map<String, Column> columns) {
        this.columns = columns;
    }

    public static BinLogItem itemFromInsertOrDeleted(Serializable[] row, Map<String, Column> columnMap, EventType eventType) {
        if (null == row || null == columnMap)
            throw new IllegalArgumentException("Row or columnMap is null");
        if (row.length != columnMap.size())
            throw new IllegalArgumentException("The row length is different from the columnMap length");
        // 初始化Item
        BinLogItem item = new BinLogItem();
        item.setEventType(eventType);
        item.setColumns(columnMap);

        Map<String, Serializable> beOrAf = Maps.newHashMap();

        columnMap.forEach((key, column) -> beOrAf.put(key, row[column.inx]));

        // 写操作放after，删操作放before
        if (BinLogUtil.isWrite(eventType)) {
            item.setAfter(beOrAf);
        }
        if (BinLogUtil.isDelete(eventType)) {
            item.setAfter(beOrAf);
        }

        return item;
    }

    public static BinLogItem itemFromUpdate(Map.Entry<Serializable[], Serializable[]> mapEntry, Map<String, Column> columnMap, EventType eventType) {
        if (null == mapEntry || null == columnMap)
            throw new IllegalArgumentException("MapEntry or columnMap is null");
        // 初始化Item
        BinLogItem item = new BinLogItem();
        item.setEventType(eventType);
        item.setColumns(columnMap);

        Map<String, Serializable> be = Maps.newHashMap();
        Map<String, Serializable> af = Maps.newHashMap();

        columnMap.forEach((key, column) -> {
            be.put(key, mapEntry.getKey()[column.inx]);

            af.put(key, mapEntry.getValue()[column.inx]);
        });

        item.setBefore(be);
        item.setAfter(af);
        return item;
    }

    @Override
    public String toString() {
        return "BinLogItem{" +
                "dbTable='" + dbTable + '\'' +
                ", eventType=" + eventType +
                ", timestamp=" + timestamp +
                ", serverId=" + serverId +
                ", before=" + before +
                ", after=" + after +
                ", columns=" + columns +
                '}';
    }
}
