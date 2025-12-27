package org.example.ncy.dataanalyst.demos.web;

import org.example.ncy.dataanalyst.demos.entity.ImportResult;
import org.example.ncy.dataanalyst.demos.servise.ExcelImportService;
import org.example.ncy.dataanalyst.demos.servise.FileServies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/ncy")
public class FileController {

    @Autowired
    private ExcelImportService excelImportService;
    @Autowired
    private FileServies fileServies;

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @PostMapping("/importExcel")
    public ResponseEntity<Map<String, Object>> importExcel( @RequestParam("file") MultipartFile file, @RequestParam("parentId") Long parentId, HttpServletRequest request) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 基本验证
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("message", "文件不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            // 2. 验证文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null ||
                    (!originalFilename.toLowerCase().endsWith(".xlsx") &&
                            !originalFilename.toLowerCase().endsWith(".xls"))) {
                result.put("success", false);
                result.put("message", "只支持Excel文件(.xlsx, .xls)");
                return ResponseEntity.badRequest().body(result);
            }

            // 3. 验证文件大小（限制10MB）
            if (file.getSize() > 10 * 1024 * 1024) {
                result.put("success", false);
                result.put("message", "文件大小不能超过10MB");
                return ResponseEntity.badRequest().body(result);
            }
            result = excelImportService.importExcel(file, parentId,request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("导入Excel失败"+e.getMessage(), e);
            result.put("success", false);
            result.put("message", "导入失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @PostMapping("/deleteExcel")
    public ResponseEntity<Map<String, Object>> deleteExcel(@RequestBody Map<String,Object> map) {

        Map<String, Object> result = new HashMap<>();

        try {
            result = fileServies.deleteExcel(map);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("删除失败"+e.getMessage(), e);
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }


}
