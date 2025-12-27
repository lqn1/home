package org.example.ncy.dataanalyst.demos.servise.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.ncy.dataanalyst.demos.entity.*;
import org.example.ncy.dataanalyst.demos.servise.DirectoryTypeRepository;
import org.example.ncy.dataanalyst.demos.servise.ExcelImportService;
import org.example.ncy.dataanalyst.demos.servise.FileRepository;
import org.example.ncy.dataanalyst.demos.servise.TabelColumnsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import java.util.*;


@Slf4j
@Service
public class ExcelImportServiceImpl implements ExcelImportService {
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private TabelColumnsRepository tabelColumnsRepository;

    @Autowired
    private DirectoryTypeRepository directoryTypeRepository;

    @Value("${file.upload.dir:./uploads}")
    private String uploadDir;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Logger logger = LoggerFactory.getLogger(ExcelImportServiceImpl.class);
    public static long MAX_FILE_SIZE = 30 * 1024 * 1024l;//文件最大不超过30MB


    public Map<String,Object> importExcel(MultipartFile file, Long parentId, HttpServletRequest request){
        Map<String,Object> result = new HashMap<>();
        try {
            // 1. 基本验证
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("message", "文件不能为空");
                return result;
            }

            // 2. 验证文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null ||
                    (!originalFilename.toLowerCase().endsWith(".xlsx") &&
                            !originalFilename.toLowerCase().endsWith(".xls"))) {
                result.put("success", false);
                result.put("message", "只支持Excel文件(.xlsx, .xls)");
                return result;
            }

            // 3. 验证文件大小（限制30MB）
            if (file.getSize() > MAX_FILE_SIZE) {
                result.put("success", false);
                result.put("message", "文件大小不能超过10MB");
                return result;
            }
            //处理文件
            ImportResult ir = processExcelImport( file,  parentId);
            result.put("success", true);
            result.put("message", "导入成功");
            result.put("data", ir);
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("导入Excel失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }


    }

    public ImportResult processExcelImport(MultipartFile file, Long parentId) {
        ImportResult result = new ImportResult();

        try {
            // 1. 验证父文件夹是否存在
            if (parentId != null) {
                DirectoryType parentFolder = directoryTypeRepository.findById(parentId).orElseThrow(() -> new RuntimeException("父文件夹不存在"));
            }

            // 2. 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String storedFilename = generateUniqueFilename(fileExtension);

            // 3. 解析Excel内容
            ExcelData excelData = parseExcelFile(file);

            // 4. 保存文件到服务器
            Path filePath = saveFileToDisk(file, storedFilename);



            // 5.保存一条excel文件记录
            DirectoryType dt = saveDirectoryType(originalFilename,parentId);
            Long id = dt.getId();

            // 6. 将文件记录保存到数据库file
            FileEntity fileEntity = saveToDatabase(originalFilename, storedFilename, filePath.toString(), id);

            //7. 将表数据保存到数据库
            saveContent(excelData,fileEntity.getId());

            // 6. 设置返回结果
            result.setSuccess(true);
            result.setFileName(originalFilename);
            result.setFileId(fileEntity.getId());
            result.setRecordCount(excelData.getRowData().size());
            result.setImportTime(new Date());

            log.info("Excel导入成功: {}，记录数: {}", originalFilename, excelData.getRowData().size());

        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
// 4. 处理Excel导入
//        ImportResult importResult = excelImportService.processExcelImport(file, parentId);
        return result;
    }

    //保存文件信息DirectoryType
    private DirectoryType saveDirectoryType(String folderName,Long parentId) throws Exception {

        try{
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String nowTime = now.format(formatter);
            DirectoryType dt = new DirectoryType();
            dt.setIcon("fa-file-excel");
            dt.setTranslate(folderName);
            dt.setCreatetime(nowTime);
            dt.setUpdatetime(nowTime);
            dt.setFatherid(parentId);
            directoryTypeRepository.saveAndFlush(dt);
            return dt;
        }catch (Exception e){
            e.printStackTrace();
            logger.error(e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    private ExcelData parseExcelFile(MultipartFile file) {
        ExcelData excelData = new ExcelData();

        try (Workbook workbook = getWorkbook(file)) {
            Sheet sheet = workbook.getSheetAt(0); // 读取第一个sheet

            // 读取表头
            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(getDisplayValue(cell).trim());
            }
            excelData.setHeaders(headers);
            List<Map<String,String>> rowDataList = new ArrayList<>();
            // 读取数据行（从第二行开始）
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row dataRow = sheet.getRow(i);
                if (dataRow == null) continue;


                Map<String, String> rowData = new LinkedHashMap<>();

                for (int j = 0; j < headers.size(); j++) {

                    Cell cell = dataRow.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String value = getDisplayValue(cell).trim();
                    //必须是数字结构
                    if(headers.get(j).contains("number")){
                        if(value==null || "".equals(value)) value="0";
                        if(!isNumeric(value))
                            throw new Exception("数据第"+i+"行第"+(j+1)+"列不是数字");
                    }
                    //必须是日期格式
                    if(headers.get(j).contains("time")){
                        if(!isValidYYYYMMDD(value)) throw new Exception("数据第"+i+"行第"+(j+1)+"列不是标准日期格式（YYYY-MM-DD）");
                    }
                    rowData.put(headers.get(j), value);
                }

                rowDataList.add(rowData);
            }
            excelData.setRowNumber(rowDataList.size());
            excelData.setRowData(rowDataList);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return excelData;
    }

    public boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    private Workbook getWorkbook(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename().toLowerCase();
        InputStream inputStream = file.getInputStream();

        if (filename.endsWith(".xlsx")) {
            return new XSSFWorkbook(inputStream);
        } else if (filename.endsWith(".xls")) {
            return new HSSFWorkbook(inputStream);
        } else {
            throw new IllegalArgumentException("不支持的Excel格式");
        }
    }

    //获取格式化后的显示值
    public static String getDisplayValue(Cell cell) {
        if (cell == null) {
            return "";
        }


        Workbook workbook = cell.getSheet().getWorkbook();
        DataFormatter formatter = new DataFormatter();
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

        // 先计算公式，再获取显示值
        evaluator.evaluateFormulaCell(cell);
        return formatter.formatCellValue(cell, evaluator);
    }

    /**
     * 备用方法：手动处理各种情况
     */
    private static String getDisplayValueFallback(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();

            case NUMERIC:
                return getFormattedNumericValue(cell);

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            case FORMULA:
                return getFormulaDisplayValue(cell);

            case BLANK:
                return "";

            default:
                return "";
        }
    }

    /**
     * 获取格式化的数字值
     */
    private static String getFormattedNumericValue(Cell cell) {
        double value = cell.getNumericCellValue();

        // 检查是否为日期
        if (DateUtil.isCellDateFormatted(cell)) {
            return formatDateValue(cell);
        }

        // 检查单元格的格式
        CellStyle style = cell.getCellStyle();
        short formatIndex = style.getDataFormat();
        String formatString = style.getDataFormatString();

        // 根据格式进行格式化
        if (formatString != null) {
            try {
                DecimalFormat decimalFormat = new DecimalFormat(formatString);
                return decimalFormat.format(value);
            } catch (Exception e) {
                // 格式无效，使用默认格式
            }
        }

        // 默认格式化：去除不必要的.0
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        } else {
            return String.valueOf(value);
        }
    }

    /**
     * 格式化日期值
     */
    private static String formatDateValue(Cell cell) {
        try {
            Date date = cell.getDateCellValue();
            CellStyle style = cell.getCellStyle();
            String formatString = style.getDataFormatString();

            if (formatString != null && !formatString.isEmpty()) {
                try {
                    // 尝试使用单元格的日期格式
                    SimpleDateFormat sdf = new SimpleDateFormat(convertExcelDateFormat(formatString));
                    return sdf.format(date);
                } catch (Exception e) {
                    // 格式无效，使用默认格式
                }
            }

            // 默认日期格式
            SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return defaultFormat.format(date);

        } catch (Exception e) {
            return String.valueOf(cell.getNumericCellValue());
        }
    }

    /**
     * 转换Excel日期格式为SimpleDateFormat
     */
    private static String convertExcelDateFormat(String excelFormat) {
        // 简单的格式转换，实际需要更复杂的处理
        return excelFormat.replace("yyyy", "yyyy")
                .replace("yy", "yy")
                .replace("mm", "MM")
                .replace("dd", "dd")
                .replace("hh", "HH")
                .replace("ss", "ss");
    }

    /**
     * 处理公式单元格的显示值
     */
    private static String getFormulaDisplayValue(Cell cell) {
        try {
            // 先尝试获取计算后的值
            switch (cell.getCachedFormulaResultType()) {
                case STRING:
                    return cell.getStringCellValue().trim();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return formatDateValue(cell);
                    } else {
                        return getFormattedNumericValue(cell);
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                default:
                    return cell.getCellFormula();
            }
        } catch (Exception e) {
            // 公式计算失败，返回公式本身
            return cell.getCellFormula();
        }
    }
    private Path saveFileToDisk(MultipartFile file, String filename) throws IOException {
        InputStream ins = null;
        try{
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(filename);
            ins = file.getInputStream();
            Files.copy(ins, filePath, StandardCopyOption.REPLACE_EXISTING);
            return filePath;
        }catch (Exception e){
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }finally {
            if (ins != null) ins.close();
        }
    }

    private void saveContent(ExcelData data,Long fileId){
        //1. 保存表结构
        List<TabelColumns> tableColumnsList = new ArrayList<>();
        List<String> headers = data.getHeaders();
        String tablename = "table_name_"+String.valueOf(System.currentTimeMillis());

        StringBuilder createTable = new StringBuilder("create table "+tablename+"(");
        long len = 0l;
        for(String header : headers){
            len++;
            TabelColumns tabelColumns = new TabelColumns();
            tabelColumns.setTableName(tablename);
            tabelColumns.setColumn("column_"+len);
            tabelColumns.setColumnName(header);
            tabelColumns.setOrd(len);
            tabelColumns.setFileId(fileId);
            tabelColumnsRepository.saveAndFlush(tabelColumns);

            tableColumnsList.add(tabelColumns);


            createTable.append(tabelColumns.getColumn()+" varchar(100), ");
        }
        createTable.append(" fileId BIGINT )");

        logger.debug("创建表SQL: {"+System.currentTimeMillis()+"}", createTable.toString());
        jdbcTemplate.execute(createTable.toString());





        //2. 将数据保存到表内
        List<Map<String,Object>> dataLists = new ArrayList<>();
        List<Map<String,String>> rowData = data.getRowData();
        for(Map<String,String> rowDataMap : rowData){
            Map<String,Object> tcMap = new HashMap<>();
            tcMap.put("fileId",fileId);
            for(TabelColumns tc : tableColumnsList){
                tcMap.put(tc.getColumn(),rowDataMap.get(tc.getColumnName()));
            }
            dataLists.add(tcMap);
        }

        if (dataLists.isEmpty()) return;

        Map<String, Object> firstRow = dataLists.get(0);
        StringBuilder insertSql = new StringBuilder("INSERT INTO ");
        insertSql.append(tablename).append(" (");

        // 构建列名部分
        for (String columnName : firstRow.keySet()) {
            insertSql.append(columnName).append(", ");
        }
        insertSql.setLength(insertSql.length() - 2); // 移除最后的逗号和空格
        insertSql.append(") VALUES (");

        // 构建值占位符
        for (int i = 0; i < firstRow.size(); i++) {
            insertSql.append("?, ");
        }
        insertSql.setLength(insertSql.length() - 2);
        insertSql.append(")");

        String finalInsertSql = insertSql.toString();
        log.debug("插入SQL: {}", finalInsertSql);

        // 批量插入数据
        jdbcTemplate.batchUpdate(finalInsertSql, dataLists, dataLists.size(),
                (ps, row) -> {
                    int paramIndex = 1;
                    for (Object value : row.values()) {
                        ps.setObject(paramIndex++, value);
                    }
                });





    }

    private FileEntity saveToDatabase(String originalName, String storedName,
                                      String filePath, Long parentId) {
        FileEntity fileEntity = new FileEntity();
        fileEntity.setFileName(originalName);
        fileEntity.setStoredName(storedName);
        fileEntity.setFilePath(filePath);
        fileEntity.setFileType("excel");
        fileEntity.setFileSize(getFileSize(filePath));
        fileEntity.setParentId(parentId);
        fileEntity.setUploadTime(new Date());

        return fileRepository.save(fileEntity);
    }

    private String generateUniqueFilename(String extension) {
        return UUID.randomUUID().toString() + "." + extension;
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private long getFileSize(String filePath) {
        try {
            return Files.size(Paths.get(filePath));
        } catch (IOException e) {
            return 0;
        }
    }

    private String convertToJson(List<ExcelData> data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(data);
        } catch (Exception e) {
            return "[]";
        }
    }

    /**
     * 使用SimpleDateFormat验证YYYY-MM-DD格式
     */
    public boolean isValidYYYYMMDD(String dateStr) throws Exception {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return true;
        }

        String trimmed = dateStr.trim();

        // 基本格式检查
        if (!trimmed.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return false;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false); // 严格模式，非常重要！

        try {
            Date date = sdf.parse(trimmed);
            // 验证格式化后的字符串是否与原始字符串匹配
            String formatted = sdf.format(date);
            return formatted.equals(trimmed);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

}
