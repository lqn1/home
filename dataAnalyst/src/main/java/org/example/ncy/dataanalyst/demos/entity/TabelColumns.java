package org.example.ncy.dataanalyst.demos.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.List;
import java.util.Map;

// 表结构记录
@Entity
@Table(name="tablecolumns")
public class TabelColumns {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long fileId;
    private String tableName;
    private String column;
    private String columnName;
    private Long ord;



    public TabelColumns() {

    }

    public TabelColumns(Long id, Long fileId, String tableName, String column, String columnName, Long ord) {
        this.id = id;
        this.fileId = fileId;
        this.tableName = tableName;
        this.column = column;
        this.columnName = columnName;
        this.ord = ord;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Long getOrd() {
        return ord;
    }

    public void setOrd(Long ord) {
        this.ord = ord;
    }
}
