package org.example.ncy.dataanalyst.demos.servise;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class FileStorageService {

    @Value("${file.storage.path:/data/files}")
    private String baseStoragePath;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取文件存储路径
     */
    public String getFilePath(Long fileId) {
        try {
            String sql = "SELECT file_name, file_path FROM files WHERE parent_id = ?";
            Map<String, Object> result = jdbcTemplate.queryForMap(sql, fileId);

            String fileName = result.get("file_name").toString();
            String storagePath = Optional.ofNullable(result.get("file_path"))
                    .map(Object::toString)
                    .orElse("");

            // 如果有自定义存储路径，直接使用
            if (!storagePath.isEmpty()) {
                return storagePath;
            }

            // 否则生成标准路径
            return generateStandardFilePath(fileId, fileName);

        } catch (Exception e) {
            log.error("获取文件路径失败: fileId={}", fileId, e);
            throw new RuntimeException("文件不存在: " + fileId, e);
        }
    }

    /**
     * 检查文件是否存在
     */
    public boolean fileExists(Long fileId) {
        try {
            String filePath = getFilePath(fileId);
            File file = new File(filePath);
            return file.exists() && file.isFile();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取文件信息
     */
    public Map<String, Object> getFileInfo(Long fileId) {
        String sql = "SELECT f.*, dt.translate as directory_name " +
                "FROM files f " +
                "LEFT JOIN directorytype dt ON f.parent_id = dt.id " +
                "WHERE f.id = ?";

        try {
            return jdbcTemplate.queryForMap(sql, fileId);
        } catch (Exception e) {
            log.error("获取文件信息失败: fileId={}", fileId, e);
            throw new RuntimeException("文件不存在: " + fileId, e);
        }
    }

    /**
     * 获取目录下所有文件
     */
    public List<Map<String, Object>> getFilesInDirectory(Long directoryId) {
        String sql =
                "SELECT f.*, dt.translate as directory_name " +
                        "FROM files f " +
                        "LEFT JOIN directorytype dt ON f.parent_id = dt.id " +
                        "WHERE f.parent_id IN (" +
                        "    WITH RECURSIVE directory_tree(id) AS (" +
                        "        SELECT id FROM directorytype WHERE id = ?" +
                        "        UNION ALL" +
                        "        SELECT d.id FROM directorytype d " +
                        "        INNER JOIN directory_tree dt ON d.fatherid = dt.id" +
                        "    )" +
                        "    SELECT id FROM directory_tree" +
                        ") AND f.file_type = 'excel' " +
                        "ORDER BY f.created_time DESC";

        return jdbcTemplate.queryForList(sql, directoryId);
    }

    /**
     * 生成标准文件路径
     */
    private String generateStandardFilePath(Long fileId, String fileName) {
        // 使用文件ID生成目录结构
        String hash = String.format("%08x", fileId);
        String subDir1 = hash.substring(0, 2);
        String subDir2 = hash.substring(2, 4);

        // 构建完整路径
        String directoryPath = baseStoragePath + File.separator + subDir1 + File.separator + subDir2;

        // 确保目录存在
        createDirectoryIfNotExists(directoryPath);

        // 使用文件ID作为文件名，避免文件名冲突
        String fileExtension = getFileExtension(fileName);
        return directoryPath + File.separator + fileId + "." + fileExtension;
    }

    /**
     * 创建目录（如果不存在）
     */
    private void createDirectoryIfNotExists(String directoryPath) {
        try {
            Path path = Paths.get(directoryPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.debug("创建目录: {}", directoryPath);
            }
        } catch (Exception e) {
            log.error("创建目录失败: {}", directoryPath, e);
            throw new RuntimeException("无法创建存储目录: " + directoryPath, e);
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex + 1).toLowerCase() : "";
    }

    /**
     * 验证文件完整性
     */
    public boolean validateFile(Long fileId) {
        try {
            String filePath = getFilePath(fileId);
            File file = new File(filePath);

            if (!file.exists()) {
                log.warn("文件不存在: {}", filePath);
                return false;
            }

            if (file.length() == 0) {
                log.warn("文件大小为0: {}", filePath);
                return false;
            }

            // 可以添加更多的文件验证逻辑
            return true;

        } catch (Exception e) {
            log.error("文件验证失败: fileId={}", fileId, e);
            return false;
        }
    }

    /**
     * 获取文件大小
     */
    public long getFileSize(Long fileId) {
        try {
            String filePath = getFilePath(fileId);
            File file = new File(filePath);
            return file.exists() ? file.length() : 0;
        } catch (Exception e) {
            log.error("获取文件大小失败: fileId={}", fileId, e);
            return 0;
        }
    }

    /**
     * 更新文件访问记录
     */
    public void recordFileAccess(Long fileId, String accessType) {
        try {
            String sql = "UPDATE files SET last_access_time = CURRENT_TIMESTAMP, access_count = COALESCE(access_count, 0) + 1 WHERE id = ?";
            jdbcTemplate.update(sql, fileId);
            log.debug("记录文件访问: fileId={}, type={}", fileId, accessType);
        } catch (Exception e) {
            log.error("记录文件访问失败: fileId={}", fileId, e);
        }
    }
}
