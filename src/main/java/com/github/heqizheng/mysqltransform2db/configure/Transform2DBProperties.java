package com.github.heqizheng.mysqltransform2db.configure;

import com.github.shyiko.mysql.binlog.event.EventType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 监听配置信息
 * @author heqizheng
 * @version 1.0
 * @date 2022/3/25
 */
@Component
@ConfigurationProperties("transform2db")
public class Transform2DBProperties {
    /**默认关闭监听事件*/
    private boolean open = false;
    /**数据源配置*/
    private DataSource dataSource = new DataSource();
    /**默认线程数*/
    private Integer consumerThreads = 5;
    /**默认休眠时间*/
    private Long queueSleep = 1000L;
    /**默认跳过删除和新增事件*/
    private EventType[] skipEventTypes = {EventType.EXT_DELETE_ROWS, EventType.EXT_WRITE_ROWS};

    public static class DataSource {
        private String host = "localhost";
        private int port = 3306;
        private String username = "root";
        private String password = "root";
        private String schema;
        private List<String> table;
        private String driverName = "com.mysql.cj.jdbc.Driver";

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getSchema() {
            return schema;
        }

        public void setSchema(String schema) {
            this.schema = schema;
        }

        public List<String> getTable() {
            return table;
        }

        public void setTable(List<String> table) {
            this.table = table;
        }

        public String getDriverName() {
            return driverName;
        }

        public void setDriverName(String driverName) {
            this.driverName = driverName;
        }

        @Override
        public String toString() {
            return "DataSource{" +
                    "host='" + host + '\'' +
                    ", port=" + port +
                    ", username='" + username + '\'' +
                    ", password='" + password + '\'' +
                    ", schema='" + schema + '\'' +
                    ", table=" + table +
                    ", driverName='" + driverName + '\'' +
                    '}';
        }
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Integer getConsumerThreads() {
        return consumerThreads;
    }

    public void setConsumerThreads(Integer consumerThreads) {
        this.consumerThreads = consumerThreads;
    }

    public Long getQueueSleep() {
        return queueSleep;
    }

    public void setQueueSleep(Long queueSleep) {
        this.queueSleep = queueSleep;
    }

    public EventType[] getSkipEventTypes() {
        return skipEventTypes;
    }

    public void setSkipEventTypes(EventType[] skipEventTypes) {
        this.skipEventTypes = skipEventTypes;
    }

    @Override
    public String toString() {
        return "Transform2DBProperties{" +
                "open=" + open +
                ", dataSource=" + dataSource +
                ", consumerThreads=" + consumerThreads +
                ", queueSleep=" + queueSleep +
                ", eventTypes=" + Arrays.toString(skipEventTypes) +
                '}';
    }
}
