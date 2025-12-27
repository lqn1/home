package org.example.ncy.dataanalyst.demos.entity;

import lombok.Data;

import java.util.List;
import java.util.Map;

// Excel数据类
@Data
public class ExcelData {
    private List<String> headers;
    private Integer rowNumber;
    private List<Map<String, String>> rowData;
}
