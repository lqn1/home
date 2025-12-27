package org.example.ncy.dataanalyst.demos.servise;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.example.ncy.dataanalyst.demos.entity.DirectoryType;
import org.example.ncy.dataanalyst.demos.entity.FileEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DirectoryZipService {

    @Value("${app.temp.directory:/tmp}")
    private String tempDirectory;


    /**
     * 创建目录的ZIP压缩包
     * @param directoryId 目录ID
     * @param directoryName 目录名称
     * @return 临时ZIP文件
     */
    public File createDirectoryZip(Long directoryId, String directoryName, List<DirectoryType> dirLists,List<FileEntity> fileLists) throws IOException {
        // 验证参数
        if (directoryId == null || directoryName == null || directoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("目录ID和目录名称不能为空");
        }

        //分装目录
        dirLists = editDirectory(dirLists);
        //分装文件
        Map<Long,FileEntity> fileMap = editFile(fileLists);

        // 创建临时ZIP文件
        File zipFile = createTempZipFile(directoryName);

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            // 设置压缩级别
            zos.setLevel(9); // 最高压缩级别
            Map<String,String> map = new HashMap<>();
            int len = 1;
            for(DirectoryType dt : dirLists){

                String icon = dt.getIcon();

                if("fa-file-excel".equals(icon)) {
                    FileEntity fileEntity = fileMap.get(dt.getId());
                    String pathAndName = fileEntity.getFilePath();
                    File file = new File(pathAndName);
                    String suffix = fileEntity.getFileName().split("\\.")[1];
                    String name = fileEntity.getFileName().split("\\.")[0];
                    String path = dt.getTablename()+fileEntity.getFileName();
                    if(map.containsKey(path)){
                        path = dt.getTablename()+name+"("+len+")."+suffix;
                        len++;
                    }
                    map.put(path,path);
                    try {
                        // 创建ZIP条目
                        ZipEntry zipEntry = new ZipEntry(path);
                        zipEntry.setTime(Files.getLastModifiedTime(file.toPath()).toMillis());
                        zipEntry.setSize(fileEntity.getFileSize());

                        zos.putNextEntry(zipEntry);

                        // 写入文件内容
                        Files.copy(file.toPath(), zos);

                        zos.closeEntry();


                    } catch (IOException e) {
                        throw new RuntimeException("处理文件失败: " + file, e);
                    }
                }else{

                }





            }

            // 打包目录

            System.out.println("目录打包完成: " + zipFile.getAbsolutePath());
            return zipFile;

        } catch (IOException e) {
            // 清理临时文件
            if (zipFile.exists()) {
                zipFile.delete();
            }
            throw new RuntimeException("创建ZIP文件失败: " + e.getMessage(), e);
        }
    }



    //分装目录
    private List<DirectoryType> editDirectory(List<DirectoryType> lists){
        Map<String,String> map = new HashMap<>();
        StringBuilder path = new StringBuilder();
        for(DirectoryType dt : lists){
            String icon = dt.getIcon();
            String columnName = dt.getTranslate();
            if("fa-folder".equals(icon)){
                path.append(columnName).append("/");
            }
            dt.setTablename(path.toString());
        }
        return lists;
    }
    //分装文件表
    private Map<Long,FileEntity> editFile(List<FileEntity> lists){
        Map<Long,FileEntity> map = new HashMap<>();
        for(FileEntity dt : lists){
            map.put(dt.getParentId(), dt);
        }
        return map;
    }

    /**
     * 根据目录ID和名称获取实际目录路径
     */
    private Path getDirectoryPath(Long directoryId, String directoryName) {
        // 这里需要根据您的业务逻辑实现
        // 示例：从数据库查询目录路径，或根据规则生成路径

        // 临时实现 - 请根据您的实际需求修改
        String basePath = "/path/to/your/directories"; // 您的实际目录路径
        return Paths.get(basePath, directoryId.toString(), directoryName);
    }

    /**
     * 创建临时ZIP文件
     */
    private File createTempZipFile(String directoryName) throws IOException {
        // 清理文件名中的非法字符
        String safeFileName = directoryName.replaceAll("[^a-zA-Z0-9.-]", "_");

        // 创建临时文件
        String tempFileName = "directory_" + safeFileName + "_" +
                System.currentTimeMillis() + ".zip";

        Path tempDir = Paths.get(tempDirectory);
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
        }

        return tempDir.resolve(tempFileName).toFile();
    }

    /**
     * 递归打包目录
     */
    private void zipDirectory(Path sourceDir, String baseDirName, ZipOutputStream zos) throws IOException {
        Files.walk(sourceDir)
                .filter(path -> !Files.isDirectory(path)) // 只处理文件，不处理目录
                .forEach(file -> {
                    try {
                        // 计算ZIP条目中的相对路径
                        String relativePath = baseDirName + sourceDir.relativize(file).toString();

                        // 创建ZIP条目
                        ZipEntry zipEntry = new ZipEntry(relativePath);
                        zipEntry.setTime(Files.getLastModifiedTime(file).toMillis());
                        zipEntry.setSize(Files.size(file));

                        zos.putNextEntry(zipEntry);

                        // 写入文件内容
                        Files.copy(file, zos);

                        zos.closeEntry();

                    } catch (IOException e) {
                        throw new RuntimeException("处理文件失败: " + file, e);
                    }
                });
    }
}
