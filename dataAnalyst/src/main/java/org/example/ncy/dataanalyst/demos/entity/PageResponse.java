package org.example.ncy.dataanalyst.demos.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

// 分页响应实体类
@Data
@AllArgsConstructor
public class PageResponse<T> {
    private Boolean success;
    private String message;
    private List<T> data;
    private Integer totalRecords;
    private Integer totalPages;
    private Integer currentPage;
    private Integer pageSize;

    public static <T> PageResponse<T> success(List<T> data, int totalRecords, int currentPage, int pageSize) {
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        return new PageResponse<>(true, "成功", data, totalRecords, totalPages, currentPage, pageSize);
    }

    public static <T> PageResponse<T> error(String message) {
        return new PageResponse<>(false, message, null, 0, 0, 0, 0);
    }
}
