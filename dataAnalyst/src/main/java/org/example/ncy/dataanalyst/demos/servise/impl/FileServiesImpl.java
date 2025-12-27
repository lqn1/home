package org.example.ncy.dataanalyst.demos.servise.impl;

import org.example.ncy.dataanalyst.demos.entity.DirectoryType;
import org.example.ncy.dataanalyst.demos.entity.FileEntity;
import org.example.ncy.dataanalyst.demos.entity.TabelColumns;
import org.example.ncy.dataanalyst.demos.servise.DirectoryTypeRepository;
import org.example.ncy.dataanalyst.demos.servise.FileRepository;
import org.example.ncy.dataanalyst.demos.servise.FileServies;
import org.example.ncy.dataanalyst.demos.servise.TabelColumnsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;

@Service
public class FileServiesImpl implements FileServies {

    private static final Logger logger = LoggerFactory.getLogger(FileServiesImpl.class);

    @Autowired
    private DirectoryTypeRepository directoryTypeRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private TabelColumnsRepository tabelColumnsRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, Object> deleteExcel(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        //获取excel主表数据(DirectoryType)
        Long directoryTypeId = Long.parseLong(map.get("folderId").toString());
        String fileType = map.get("fileType").toString();
        try{
            if("folder".equals(fileType)){
                String sql =
                    "SELECT " +
                    " t.table_name " +
                    " FROM files f  " +
                    "LEFT JOIN tablecolumns t ON f.id = t.file_id " +
                    " WHERE f.parent_id IN (    SELECT id FROM GET_DIRECTORY_TREE("+directoryTypeId+")) " +
                    " group by t.table_name ";
                List<TabelColumns> tabelColumnsLists = jdbcTemplate.query(sql,new BeanPropertyRowMapper<>(TabelColumns.class),null);

                sql = "select f.* from files f where parent_id in(   SELECT id FROM GET_DIRECTORY_TREE("+directoryTypeId+")) " ;
                List<FileEntity> fileEntitiesLists = jdbcTemplate.query(sql,new BeanPropertyRowMapper<>(FileEntity.class),null);
                for(FileEntity fileEntity : fileEntitiesLists){
                    File file = new File(fileEntity.getFilePath());
                    if(file.exists()) file.delete();
                }
                if(tabelColumnsLists.size()>0){
                    for(TabelColumns tabelColumns : tabelColumnsLists){
                        String tablename = tabelColumns.getTableName();
                        //删除excel内容
                        sb.append("drop table IF EXISTS "+tablename);
                        jdbcTemplate.execute(sb.toString());
                        sb.setLength(0);
                    }
                }
                sb.append("delete from tablecolumns where file_id in (select id from files where parent_id in(SELECT id FROM GET_DIRECTORY_TREE("+directoryTypeId+")))");
                jdbcTemplate.execute(sb.toString());
                sb.setLength(0);
                sb.append("delete from files where parent_id in(SELECT id FROM GET_DIRECTORY_TREE("+directoryTypeId+"))");
                jdbcTemplate.execute(sb.toString());
                sb.setLength(0);
                sb.append("delete from directorytype where id in(SELECT id FROM GET_DIRECTORY_TREE("+directoryTypeId+"))");
                jdbcTemplate.execute(sb.toString());
                sb.setLength(0);


            }else if("file".equals(fileType)){
                //获取excel配置信息
                List<FileEntity> fileEntityLists = fileRepository.findByParentId(directoryTypeId);
                Long fileId = fileEntityLists.get(0).getId();
                //获取excel表单信息
                List<TabelColumns> tabelColumnsList = tabelColumnsRepository.findTableColumnsByFileId(fileId);
                String tablename = tabelColumnsList.get(0).getTableName();

                //删除excel内容

                sb.append("drop table IF EXISTS "+tablename);
                jdbcTemplate.execute(sb.toString());
                //删除excel表单信息
                sb.setLength(0);
                sb.append("delete from tablecolumns where file_id=").append(fileId);
                jdbcTemplate.execute(sb.toString());
                //删除excel配置信息
                File file = new File(fileEntityLists.get(0).getFilePath());
                if(file.exists()) file.delete();
                sb.setLength(0);
                sb.append("delete from files where id=" ).append(fileId);
                jdbcTemplate.execute(sb.toString());
                //删除excel主表信息
                sb.setLength(0);
                sb.append("delete from directoryType where id=" ).append(directoryTypeId);
                jdbcTemplate.execute(sb.toString());
                sb.setLength(0);
            }





            map.put("success", true);
            map.put("message", "删除成功");
            return map;
        }catch (Exception e){
            e.printStackTrace();
            map.put("success",false);
            map.put("message",e.getMessage());
            logger.error("删除失败directoryTypeId="+directoryTypeId, e);
        }




        return Map.of();
    }
}
