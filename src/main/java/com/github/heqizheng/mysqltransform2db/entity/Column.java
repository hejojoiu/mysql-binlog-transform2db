package com.github.heqizheng.mysqltransform2db.entity;

import java.util.Objects;

/**
 * 字段属性对象
 * @author heqizheng
 * @version 1.0
 * @date 2022/3/25
 */
public class Column {

    public Integer inx;
    /**列名*/
    public String colName;
    /**类型*/
    public String dataType;
    /**数据库*/
    public String schema;
    /**表*/
    public String table;

    public Column() {
    }

    public Column(Integer inx,String colName, String dataType, String schema, String table) {
        this.inx = inx;
        this.colName = colName;
        this.dataType = dataType;
        this.schema = schema;
        this.table = table;
    }

    public Integer getInx() {
        return inx;
    }

    public void setInx(Integer inx) {
        this.inx = inx;
    }

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Column column = (Column) o;
        return inx.equals(column.inx) &&
                Objects.equals(colName, column.colName) &&
                Objects.equals(dataType, column.dataType) &&
                Objects.equals(schema, column.schema) &&
                Objects.equals(table, column.table);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inx, colName, dataType, schema, table);
    }

    @Override
    public String toString() {
        return "Column{" +
                "inx=" + inx +
                ", colName='" + colName + '\'' +
                ", dataType='" + dataType + '\'' +
                ", schema='" + schema + '\'' +
                ", table='" + table + '\'' +
                '}';
    }
}
