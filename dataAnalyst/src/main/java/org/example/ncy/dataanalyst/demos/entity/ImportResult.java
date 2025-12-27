package org.example.ncy.dataanalyst.demos.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.Map;

// 导入结果类
@Data
public class ImportResult {
    private boolean success;
    private String fileName;
    private Long fileId;
    private Integer recordCount;
    private Date importTime;
    private String errorMessage;
}