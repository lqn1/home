package org.example.ncy.dataanalyst.demos.servise;

import org.example.ncy.dataanalyst.demos.entity.PageRequest;
import org.example.ncy.dataanalyst.demos.entity.TabelColumns;
import org.example.ncy.dataanalyst.util.NumberUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class ExcelDataRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取表结构信息
     */
    public List<TabelColumns> findTableColumns(String tableName) {
        // 根据数据库类型选择不同的SQL
        String sql = """
            SELECT 
                column_name as column,
                remarks as columnName
            FROM information_schema.columns 
            WHERE table_name = ? 
            ORDER BY ordinal_position
            """;
        tableName = tableName.toUpperCase();
        return jdbcTemplate.query(sql, new Object[]{tableName}, new RowMapper<TabelColumns>() {
            @Override
            public TabelColumns mapRow(ResultSet rs, int rowNum) throws SQLException {
                TabelColumns column = new TabelColumns();
                column.setColumn(rs.getString("column"));
                column.setColumnName(rs.getString("columnName"));
                return column;
            }
        });
    }

    /**
     * 获取表数据（分页+筛选+排序）
     */
    public List<Map<String, Object>> findTableData(String tableName, String globalSearch, PageRequest pageRequest) throws Exception {

        try {
            StringBuilder sqlBuilder = new StringBuilder(" SELECT * FROM ");
            Map<String,Object> map = getSelectSql(tableName,sqlBuilder,globalSearch,pageRequest);
            sqlBuilder = (StringBuilder) map.get("sql");
            List<Object> params = (List<Object>) map.get("params");
            // 添加排序
            if (StringUtils.hasText(pageRequest.getSortColumn())) {
                sqlBuilder.append(" ORDER BY ")
                        .append(pageRequest.getSortColumn())
                        .append(" ")
                        .append(pageRequest.getSortDirection());
            }

            // 添加分页
            sqlBuilder.append(" LIMIT ? OFFSET ?");
            params.add(pageRequest.getPageSize());
            params.add((pageRequest.getPage() - 1) * pageRequest.getPageSize());

            return jdbcTemplate.queryForList(sqlBuilder.toString(), params.toArray());
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }

    }

    /**
     * 获取总记录数
     */
    public Integer countTableData(String tableName, String globalSearch, PageRequest pageRequest) throws Exception {

        try{
            StringBuilder sqlBuilder = new StringBuilder(" SELECT count(*) FROM ");
            Map<String,Object> map = getSelectSql(tableName,sqlBuilder,globalSearch,pageRequest);
            sqlBuilder = (StringBuilder) map.get("sql");
            List<Object> params = (List<Object>) map.get("params");
            return jdbcTemplate.queryForObject(sqlBuilder.toString(), params.toArray(), Integer.class);
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }


    }



    public Map<String,Object> getSelectSql(String tableName, StringBuilder sqlBuilder,  String globalSearch, PageRequest pageRequest) throws Exception {
        List<Object> params = new ArrayList<>();
        try{
            StringBuilder filtersBuilder = new StringBuilder();
            if (!pageRequest.getFilters().isEmpty()) {
                filtersBuilder.append(" WHERE ");
                List<String> conditions = new ArrayList<>();

                for (Map.Entry<String, LinkedHashMap<String,String>> entry : pageRequest.getFilters().entrySet()) {
                    String key = entry.getKey();
                    Map<String,String> val = entry.getValue();
                    String type = val.get("type");
                    String value = val.get("value");
                    if("range".equals(type)){
                        String min = val.get("min");
                        String max = val.get("max");
                        if(StringUtils.hasText(min)){
                            conditions.add(key.replace("_start","") +" >= ?");
                            params.add(NumberUtil.isNumericFormat(min)?Double.parseDouble(min):min);
                        }
                        if(StringUtils.hasText(max)){
                            conditions.add(key.replace("_end","") +" <= ?");
                            params.add(NumberUtil.isNumericFormat(max)?Double.parseDouble(max):max);
                        }
                    }else if (StringUtils.hasText(value)){
                        conditions.add(entry.getKey() + " LIKE ?");
                        params.add("%" + value + "%");
                    }

                }
                filtersBuilder.append(String.join(" AND ", conditions));
            }

            StringBuilder globalSearchBuilder = new StringBuilder();
            if(StringUtils.hasText(globalSearch)){
                if(filtersBuilder.length()==0){
                    globalSearchBuilder.append(" WHERE (");
                }else{
                    globalSearchBuilder.append(" AND ( ");
                }
                List<TabelColumns> lists = findTableColumns(tableName);
                List<String> conditionTwo = new ArrayList<>();
                for (TabelColumns tabelColumns : lists) {
                    if("FILEID".equals(tabelColumns.getColumn())){continue;}
                    conditionTwo.add(tabelColumns.getColumn() + " LIKE ?");
                    params.add("%" + globalSearch + "%");
                }

                globalSearchBuilder.append(String.join(" or ", conditionTwo));
                globalSearchBuilder.append(" ) ");
            }


            sqlBuilder.append(tableName).append(filtersBuilder.toString()).append(globalSearchBuilder.toString());
            Map<String,Object>  result = new HashMap<>();
            result.put("sql", sqlBuilder);
            result.put("params",params);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }

    }


    /**
     * 获取所有表名
     */
    public List<String> findAllTableNames() {
        String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    /**
     * 验证表是否存在
     */
    public boolean tableExists(String tableName) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ? AND table_schema = DATABASE()";
            Integer count = jdbcTemplate.queryForObject(sql, new Object[]{tableName}, Integer.class);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }






}
