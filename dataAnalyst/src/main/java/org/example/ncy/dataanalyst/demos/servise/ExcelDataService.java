package org.example.ncy.dataanalyst.demos.servise;

import org.example.ncy.dataanalyst.demos.entity.PageRequest;
import org.example.ncy.dataanalyst.demos.entity.PageResponse;
import org.example.ncy.dataanalyst.demos.entity.TabelColumns;
import org.example.ncy.dataanalyst.util.NumberUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ExcelDataService {

    @Autowired
    private ExcelDataRepository excelDataRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private TabelColumnsRepository tabelColumnsRepository;




    //根据folderId获取表结构
    public List<TabelColumns> getTableColumns(String fileId){
        return tabelColumnsRepository.findTableColumnsByFolderId(Long.parseLong(fileId));
    }



    /**
     * 获取表数据（分页）
     */
    public PageResponse<Map<String, Object>> getTableData(String fileId, String globalSearch, PageRequest pageRequest) {
        try {
            String tableName = getTableNameByFileId(fileId);

            // 获取数据
            List<Map<String, Object>> data = excelDataRepository.findTableData(tableName, globalSearch, pageRequest);

            // 获取总记录数
            Integer totalRecords = excelDataRepository.countTableData(tableName, globalSearch, pageRequest);

            return PageResponse.success(data, totalRecords, pageRequest.getPage(), pageRequest.getPageSize());

        } catch (Exception e) {
            e.printStackTrace();
            return PageResponse.error("查询失败: " + e.getMessage());
        }
    }



    //获取字段的统计值
    public Map<String, Object> getColumnInfos(Map<String, Object> request,PageRequest pageRequest) {
        Map<String,Object> result = new HashMap<>();
        result.put("success",true);
        result.put("flag","200");
        try {

            String fileId = request.get("fileId")!=null?String.valueOf(request.get("fileId")):null;
            String column = request.get("column")!=null?String.valueOf(request.get("column")):null;
            String globalSearch = request.get("globalSearch")!=null?String.valueOf(request.get("globalSearch")):null;

            if (!StringUtils.hasText(fileId)) {
                result.put("success",false);
                result.put("message","fileId不能为空");
                return result;
            }

            //获取表名
            String tableName = getTableNameByFileId(fileId);

            // 构建查询SQL
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append(" SELECT ");
            sqlBuilder.append("  COUNT(*) AS category_count, ");
            sqlBuilder.append(" SUM(CASE WHEN "+column+" REGEXP '^[0-9]+(\\.[0-9]+)?$' THEN CAST("+column+" AS DECIMAL(15, 2))  ELSE 0 END) AS category_sum, ");
            sqlBuilder.append(" AVG(CASE WHEN "+column+" REGEXP '^[0-9]+(\\.[0-9]+)?$' THEN CAST("+column+" AS DECIMAL(15, 2))  ELSE NULL END) AS category_avg, ");
            sqlBuilder.append(" MAX(CASE WHEN "+column+" REGEXP '^[0-9]+(\\.[0-9]+)?$' THEN CAST("+column+" AS DECIMAL(15, 2))  ELSE NULL END) AS category_max, ");
            sqlBuilder.append(" MIN(CASE WHEN "+column+" REGEXP '^[0-9]+(\\.[0-9]+)?$' THEN CAST("+column+" AS DECIMAL(15, 2))  ELSE NULL END) AS category_min ");
            sqlBuilder.append(" FROM ");
            sqlBuilder.append(tableName);

            sqlBuilder.append(" WHERE ");
            sqlBuilder.append(" "+column+" IS NOT NULL AND "+column+" <> '' ");



            // 添加WHERE条件
            List<Object> params = new ArrayList<>();
            if (!pageRequest.getFilters().isEmpty()) {
                sqlBuilder.append(" AND ( 1=1 ");
                Object oj = pageRequest.getFilters().get("column_7");
                for (Map.Entry<String, LinkedHashMap<String,String>> entry : pageRequest.getFilters().entrySet()) {
                    String key = entry.getKey();
                    Map<String,String> val = entry.getValue();
                    String type = val.get("type");
                    if("range".equals(type)){
                        String min = val.get("min");
                        String max = val.get("max");
                        if(StringUtils.hasText(min)){
                            sqlBuilder.append(" AND ").append(key).append(" >= ").append(NumberUtil.isNumericFormat(min)?Double.parseDouble(min):min);
                        }
                        if(StringUtils.hasText(max)){
                            sqlBuilder.append(" AND ").append(key).append(" <= ").append(NumberUtil.isNumericFormat(max)?Double.parseDouble(max):max);
                        }
                    }
                    String value = val.get("value");
                    if (StringUtils.hasText(value)) {
                        sqlBuilder.append(" AND ").append(entry.getKey()).append(" LIKE '%").append(value).append("%' ");
                    }
                }
                sqlBuilder.append(" ) ");
            }
            if(!org.thymeleaf.util.StringUtils.isEmpty(globalSearch)){
                List<TabelColumns> lists = excelDataRepository.findTableColumns(tableName);
                sqlBuilder.append(" and ( ");
                for (TabelColumns tabelColumns : lists) {
                    if("FILEID".equals(tabelColumns.getColumn())){continue;}
                    sqlBuilder.append(" ").append(tabelColumns.getColumn()).append(" like '%").append(globalSearch).append("%' or");
                }
                sqlBuilder.delete(sqlBuilder.length()-2,sqlBuilder.length());
                sqlBuilder.append(" ) ");
            }

            Map<String,Object> map = jdbcTemplate.queryForMap(sqlBuilder.toString(), null);
            if(map.get("CATEGORY_MAX")==null){
                result.put("flag","201");
                return result;
            }else{
                BigDecimal sum = new BigDecimal(map.get("CATEGORY_SUM").toString()).setScale(2,BigDecimal.ROUND_HALF_UP);
                BigDecimal max = new BigDecimal(map.get("CATEGORY_MAX").toString()).setScale(2,BigDecimal.ROUND_HALF_UP);
                BigDecimal min = new BigDecimal(map.get("CATEGORY_MIN").toString()).setScale(2,BigDecimal.ROUND_HALF_UP);
                BigDecimal avg = new BigDecimal(map.get("CATEGORY_AVG").toString()).setScale(2,BigDecimal.ROUND_HALF_UP);
                BigDecimal total = new BigDecimal(map.get("CATEGORY_COUNT").toString()).setScale(0,BigDecimal.ROUND_HALF_UP);
                result.put("sum",sum.toString());
                result.put("max",max.toString());
                result.put("min",min.toString());
                result.put("avg",avg.toString());
                result.put("total",total.toString());
            }


            return result;

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success",false);
            result.put("message",e.getMessage());
            return result;
        }
    }

    /**
     * 根据fileId获取表名（这里需要根据您的业务逻辑实现）
     */
    private String getTableNameByFileId(String fileId) {
        List<TabelColumns> lists = getTableColumns(fileId);
        return lists.get(0).getTableName();
    }

    /**
     * 获取所有可用的表
     */
    public List<String> getAvailableTables() {
        return excelDataRepository.findAllTableNames();
    }
}
