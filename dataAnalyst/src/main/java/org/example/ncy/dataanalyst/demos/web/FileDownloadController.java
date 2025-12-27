package org.example.ncy.dataanalyst.demos.web;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.ncy.dataanalyst.demos.entity.DirectoryType;
import org.example.ncy.dataanalyst.demos.entity.FileEntity;
import org.example.ncy.dataanalyst.demos.servise.DirectoryZipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/ncy")
public class FileDownloadController {

    @Value("${file.storage.path:/data/files}")
    private String fileStoragePath;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DirectoryZipService directoryZipService;

    /**
     * 下载单个Excel文件
     */
    @PostMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestBody Map<String, Object> request) {
        try {
            Long fileId = Long.valueOf(request.get("fileId").toString());

            // 获取文件信息
            FileInfo fileInfo = getFileInfo(fileId);
            if (fileInfo == null) {
                return ResponseEntity.notFound().build();
            }

            // 构建文件路径
            String filePath = buildFilePath(fileInfo);
            File file = new File(filePath);

            if (!file.exists()) {
                log.warn("文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=\"" + fileInfo.getFileName() + "\"");
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            headers.add("Content-Length", String.valueOf(file.length()));

            // 根据文件类型设置Content-Type
            MediaType mediaType = getMediaType(fileInfo.getFileType());

            Resource resource = new FileSystemResource(file);

            log.info("文件下载成功: fileId={}, fileName={}, size={} bytes",
                    fileId, fileInfo.getFileName(), file.length());

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .contentType(mediaType)
                    .body(resource);

        } catch (Exception e) {
            log.error("文件下载失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 下载目录下所有文件（打包为ZIP）
     */
    @PostMapping("/downloadDirectory")
    public ResponseEntity<Resource> downloadDirectoryFiles(@RequestBody Map<String, Object> request) {
        try {
            Long directoryId = Long.valueOf(request.get("directoryId").toString());
            String directoryName = request.get("directoryName").toString();

            //获取需要打包的文件
            StringBuilder sb = new StringBuilder();
            sb.append("select d.* from directorytype d where d.id in(SELECT id FROM GET_DIRECTORY_TREE("+directoryId+") )");
            List<DirectoryType> dirLists = jdbcTemplate.query(sb.toString(),new BeanPropertyRowMapper<>(DirectoryType.class),null);
            sb.setLength(0);
            sb.append("select d.* from files d where d.parent_id in(SELECT id FROM GET_DIRECTORY_TREE("+directoryId+") )");
            List<FileEntity> fileLists = jdbcTemplate.query(sb.toString(),new BeanPropertyRowMapper<>(FileEntity.class),null);
            // 创建临时ZIP文件
            File zipFile = directoryZipService.createDirectoryZip(directoryId, directoryName,dirLists,fileLists);

            if (zipFile == null || !zipFile.exists()) {
                return ResponseEntity.notFound().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=\"" + directoryName + ".zip\"");
            headers.add("Content-Length", String.valueOf(zipFile.length()));

            Resource resource = new FileSystemResource(zipFile);

            log.info("目录文件打包下载成功: directoryId={}, fileName={}, size={} bytes",
                    directoryId, zipFile.getName(), zipFile.length());

            // 返回响应，并在下载完成后删除临时文件
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(zipFile.length())
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("目录文件下载失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 直接文件流下载（适用于大文件）
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFileDirect(@PathVariable Long fileId) {
        try {
            FileInfo fileInfo = getFileInfo(fileId);
            if (fileInfo == null) {
                return ResponseEntity.notFound().build();
            }

            String filePath = buildFilePath(fileInfo);
            Path path = Paths.get(filePath);

            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(path);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=\"" + fileInfo.getFileName() + "\"");
            headers.add("Content-Length", String.valueOf(Files.size(path)));

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(getMediaType(fileInfo.getFileType()))
                    .body(resource);

        } catch (Exception e) {
            log.error("直接文件下载失败: fileId={}", fileId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取文件预览（在线查看）
     */
    @PostMapping("/preview")
    public ResponseEntity<Resource> previewFile(@RequestBody Map<String, Object> request) {
        try {
            Long fileId = Long.valueOf(request.get("fileId").toString());

            FileInfo fileInfo = getFileInfo(fileId);
            if (fileInfo == null) {
                return ResponseEntity.notFound().build();
            }

            String filePath = buildFilePath(fileInfo);
            File file = new File(filePath);

            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            // 对于预览，设置inline而不是attachment
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "inline; filename=\"" + fileInfo.getFileName() + "\"");
            headers.add("Content-Length", String.valueOf(file.length()));

            Resource resource = new FileSystemResource(file);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(getMediaType(fileInfo.getFileType()))
                    .body(resource);

        } catch (Exception e) {
            log.error("文件预览失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 私有辅助方法
    private FileInfo getFileInfo(Long fileId) {
        // 从数据库获取文件信息
        String sql = "SELECT id, file_name, file_path, file_type, file_size, file_path FROM files WHERE parent_id = ?";
        try {
            // 使用JdbcTemplate查询
            Map<String, Object> result = jdbcTemplate.queryForMap(sql, fileId);

            FileInfo fileInfo = new FileInfo();
            fileInfo.setId(Long.valueOf(result.get("id").toString()));
            fileInfo.setFileName(result.get("file_name").toString());
            fileInfo.setFilePath(Optional.ofNullable(result.get("file_path")).map(Object::toString).orElse(""));
            fileInfo.setFileType(result.get("file_type").toString());
            fileInfo.setFileSize(Long.valueOf(result.get("file_size").toString()));
            fileInfo.setStoragePath(Optional.ofNullable(result.get("file_path")).map(Object::toString).orElse(""));

            return fileInfo;
        } catch (Exception e) {
            log.error("获取文件信息失败: fileId={}", fileId, e);
            return null;
        }
    }

    private String buildFilePath(FileInfo fileInfo) {
        // 如果文件有自定义存储路径，使用自定义路径
        if (StringUtils.hasText(fileInfo.getStoragePath())) {
            return fileInfo.getStoragePath();
        }

        // 否则使用默认存储路径
        String fileName = fileInfo.getFileName();
        String fileExtension = getFileExtension(fileName);
        String relativePath = generateRelativePath(fileInfo.getId(), fileExtension);

        return fileStoragePath + File.separator + relativePath;
    }

    private String generateRelativePath(Long fileId, String extension) {
        // 使用文件ID生成目录结构，避免单个目录文件过多
        String hash = String.format("%08x", fileId);
        return hash.substring(0, 2) + File.separator +
                hash.substring(2, 4) + File.separator +
                fileId + "." + extension;
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex + 1) : "";
    }

    private MediaType getMediaType(String fileType) {
        switch (fileType.toLowerCase()) {
            case "xlsx":
            case "xls":
                return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case "pdf":
                return MediaType.APPLICATION_PDF;
            case "txt":
                return MediaType.TEXT_PLAIN;
            case "jpg":
            case "jpeg":
                return MediaType.IMAGE_JPEG;
            case "png":
                return MediaType.IMAGE_PNG;
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private File createDirectoryZip(Long directoryId, String directoryName) {
        // 实现目录打包逻辑
        // 这里需要实现ZIP打包功能
        // 返回临时ZIP文件
        return null; // 简化实现
    }

    // 文件信息类
    @Data
    public static class FileInfo {
        private Long id;
        private String fileName;
        private String filePath;
        private String fileType;
        private Long fileSize;
        private String storagePath;
    }


}
