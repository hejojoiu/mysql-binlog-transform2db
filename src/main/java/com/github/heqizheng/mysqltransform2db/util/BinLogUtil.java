package com.github.heqizheng.mysqltransform2db.util;

import com.github.heqizheng.mysqltransform2db.configure.Transform2DBProperties;
import com.github.heqizheng.mysqltransform2db.entity.BinLogItem;
import com.github.heqizheng.mysqltransform2db.entity.Column;
import com.github.shyiko.mysql.binlog.event.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;
import java.sql.*;
import java.util.*;

/**
 * 监听工具
 * @author heqizheng
 * @version 1.0
 * @date 2022/3/25
 */
@Component
public class BinLogUtil {

    private static final Logger logger = LoggerFactory.getLogger(BinLogUtil.class);

    @Resource
    Transform2DBProperties dbProperties;

    /**
     * 拼接dbTable
     * @param table 表名
     * @param db 数据库
     */
    public static String getDbTable(String db, String table) {
        return db + "-" + table;
    }

    /**
     * 获取columns集合
     */
    public static Map<String, Column> getColMap(Transform2DBProperties.DataSource conf, String table) throws ClassNotFoundException, SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Class.forName(conf.getDriverName());
            // 保存当前注册的表的column信息
            Connection connection = DriverManager.getConnection("jdbc:mysql://" + conf.getHost() + ":" + conf.getPort(), conf.getUsername(), conf.getPassword());
            // 执行sql
            String preSql = "SELECT TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, DATA_TYPE, ORDINAL_POSITION " +
                    "FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = ? and TABLE_NAME = ?";
            ps = connection.prepareStatement(preSql);
            ps.setString(1, conf.getSchema());
            ps.setString(2, table);
            rs = ps.executeQuery();
            Map<String, Column> map = new HashMap<>(rs.getRow());
            while (rs.next()) {
                String schema = rs.getString("TABLE_SCHEMA");
                String tableName = rs.getString("TABLE_NAME");
                String column = rs.getString("COLUMN_NAME");
                int idx = rs.getInt("ORDINAL_POSITION");
                String dataType = rs.getString("DATA_TYPE");
                if (column != null && idx >= 1) {
                    // sql的位置从1开始
                    map.put(column, new Column(idx - 1, column, dataType, schema, tableName));
                }
            }

            return map;
        } catch (SQLException e) {
            logger.error("load schema conf error, schema_table={}:{} ", conf.getSchema(), table, e);
        } finally {
            ps.close();
            rs.close();
        }
        return null;
    }

    /**
     * 根据DBTable获取table
     *
     * @param dbTable 数据库表名
     * @return java.lang.String
     */
    public static String getTable(String dbTable) {
        if (StrUtil.isEmpty(dbTable))
            return "";
        String[] split = dbTable.split("-");
        if (split.length == 2)
            return split[1];
        return "";
    }

    /**
     * 将逗号拼接字符串转List
     */
    public static List<String> getListByStr(String str) {
        if (StrUtil.isEmpty(str))
            return new ArrayList<>();
        return Arrays.asList(str.split(","));
    }

    /**
     * 根据操作类型获取对应集合
     */
    public static Map<String, Serializable> getOptMap(BinLogItem binLogItem) {
        // 获取操作类型
        EventType eventType = binLogItem.getEventType();
        if (isWrite(eventType) || isUpdate(eventType)) {
            return binLogItem.getAfter();
        }
        if (isDelete(eventType)) {
            return binLogItem.getBefore();
        }
        return null;
    }

    /**
     * 获取操作类型
     */
    public static Integer getOptType(BinLogItem binLogItem) {
        // 获取操作类型
        EventType eventType = binLogItem.getEventType();
        if (isWrite(eventType)) {
            return 1;
        }
        if (isUpdate(eventType)) {
            return 2;
        }
        if (isDelete(eventType)) {
            return 3;
        }
        return null;
    }

    public static boolean isWrite(EventType eventType) {
        if (eventType == null)
            return false;
        return eventType == EventType.EXT_WRITE_ROWS;
    }

    public static boolean isUpdate(EventType eventType) {
        if (eventType == null)
            return false;
        return eventType == EventType.EXT_UPDATE_ROWS;
    }

    public static boolean isDelete(EventType eventType) {
        if (eventType == null)
            return false;
        return eventType == EventType.EXT_DELETE_ROWS;
    }
}
