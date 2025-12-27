package org.example.ncy.dataanalyst.demos.web;

import org.example.ncy.dataanalyst.demos.entity.PageRequest;
import org.example.ncy.dataanalyst.demos.entity.PageResponse;
import org.example.ncy.dataanalyst.demos.entity.TabelColumns;
import org.example.ncy.dataanalyst.demos.servise.ExcelDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


@Controller
@RequestMapping("/ncy")
@CrossOrigin(origins = "*")
public class ExcelDataController {

    @Autowired
    private ExcelDataService excelDataService;



    @RequestMapping("/openExcel")
    public String html(Long fileId,String folderName) {
        return "forward://data/html/excelList.html?folderName="+folderName+"&fileId="+fileId;
    }

    /**
     * 获取表结构信息
     */
    @PostMapping("/findTableColumns")
    public ResponseEntity<?> findTableColumns(@RequestBody Map<String, String> request) {
        try {
            String fileId = request.get("fileId");
            if (!StringUtils.hasText(fileId)) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "fileId不能为空"));
            }

            List<TabelColumns> columns = excelDataService.getTableColumns(fileId);
            return ResponseEntity.ok(columns);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 获取表数据（分页+筛选+排序）
     */
    @PostMapping("/findTableColumnsData")
    public ResponseEntity<PageResponse<Map<String, Object>>> findTableColumnsData( @RequestBody Map<String, Object> request) {

        try {
            String fileId = request.get("fileId")!=null?request.get("fileId").toString():null;
            String globalSearch = request.get("globalSearch")!=null?String.valueOf(request.get("globalSearch")):null;

            if (!StringUtils.hasText(fileId)) {
                return ResponseEntity.ok(PageResponse.error("fileId不能为空"));
            }

            // 解析分页参数
            PageRequest pageRequest = parsePageRequest(request);

            PageResponse<Map<String, Object>> response = excelDataService.getTableData(fileId, globalSearch, pageRequest);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(PageResponse.error("查询失败: " + e.getMessage()));
        }
    }

    //用于统计最大值，最小值，平均值，合计
    @PostMapping("/findTableColumnsDataLists")
    public ResponseEntity findTableColumnsDataLists( @RequestBody Map<String, Object> request) {
        Map<String,Object> result = new HashMap<>();
        try {


            // 解析分页参数
            PageRequest pageRequest = parsePageRequest(request);



            result = excelDataService.getColumnInfos(request, pageRequest);
//            result.put("max",max.setScale(2, RoundingMode.HALF_UP));
//            result.put("min",min.setScale(2, RoundingMode.HALF_UP));
//            result.put("avg",avg.setScale(2, RoundingMode.HALF_UP));
//            result.put("sum",sum.setScale(2, RoundingMode.HALF_UP));
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success",false);
            result.put("message",e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 获取所有表名
     */
    @GetMapping("/tables")
    public ResponseEntity<?> getAllTables() {
        try {
            List<String> tables = excelDataService.getAvailableTables();
            return ResponseEntity.ok(Map.of("success", true, "data", tables));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 解析分页请求参数
     */
    private PageRequest parsePageRequest(Map<String, Object> request) {
        PageRequest pageRequest = new PageRequest();

        // 分页参数
        if (request.get("page") != null) {
            pageRequest.setPage(Integer.parseInt(request.get("page").toString()));
        }
        if (request.get("pageSize") != null) {
            pageRequest.setPageSize(Integer.parseInt(request.get("pageSize").toString()));
        }

        // 筛选条件
        if (request.get("filters") instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, LinkedHashMap<String,String>> filters = (Map<String, LinkedHashMap<String,String>>) request.get("filters");
            pageRequest.setFilters(filters);
        }

        // 排序参数
        if (request.get("sort") instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, String> sort = (Map<String, String>) request.get("sort");
            pageRequest.setSortColumn(sort.get("column"));
            pageRequest.setSortDirection(sort.get("direction"));
        }

        return pageRequest;
    }
}