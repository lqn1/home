package org.example.ncy.dataanalyst.demos.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

// 文件实体类
@Entity
@Table(name = "files")
@Data
public class FileEntity {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String storedName;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String fileType;

    private Long fileSize;

    private Long parentId;

    @Column(columnDefinition = "TEXT")
    private String excelData; // JSON格式的Excel数据

    @Temporal(TemporalType.TIMESTAMP)
    private Date uploadTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;
}
