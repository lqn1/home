package org.example.ncy.dataanalyst.demos.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

// 分页请求实体类
@Data
public class PageRequest {
    private Integer page = 1;        // 当前页码
    private Integer pageSize = 10;   // 每页大小
    private Map<String, LinkedHashMap<String,String>> filters = new HashMap<>(); // 筛选条件
    private String sortColumn;       // 排序列
    private String sortDirection = "asc"; // 排序方向
}

