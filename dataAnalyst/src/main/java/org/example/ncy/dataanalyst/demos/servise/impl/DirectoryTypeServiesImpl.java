package org.example.ncy.dataanalyst.demos.servise.impl;

import org.example.ncy.dataanalyst.demos.entity.DirectoryType;
import org.example.ncy.dataanalyst.demos.servise.DirectoryTypeRepository;
import org.example.ncy.dataanalyst.demos.servise.DirectoryTypeServies;
import org.hibernate.annotations.Source;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DirectoryTypeServiesImpl implements DirectoryTypeServies {

    @Autowired
    private DirectoryTypeRepository directoryTypeRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    //查询所有一级目录数据
    public List<DirectoryType> findTopDircetory(){
        List<DirectoryType> lists = new ArrayList<>();
        try{
            lists = directoryTypeRepository.findTopDircetory();
            return lists;
        }catch (Exception e){
            e.printStackTrace();
            return lists;
        }

    }//查询所有一级目录

    //根据导航栏查询目录内的内容
    public List<DirectoryType> findNextDircetoryContents(Map<String,Object> map){
        List<DirectoryType> lists = new ArrayList<>();
        try{
            Object fatherid = map.get("folderId");
            Long id = fatherid == null ? null :Long.parseLong(fatherid.toString().trim());
            lists = directoryTypeRepository.findNextDircetoryContents(id);
            return lists;
        }catch (Exception e){
            e.printStackTrace();
            return lists;
        }

    }
    //向上查询目录内容
    public List<DirectoryType> findLastDircetoryContents(Map<String,Object> map){
        List<DirectoryType> lists = new ArrayList<>();
        try{
            Object fatherid = map.get("folderId");
            Long id = fatherid == null ? null :Long.parseLong(fatherid.toString().trim());
            lists = directoryTypeRepository.findLastDircetoryContents(id);
            return lists;
        }catch (Exception e){
            e.printStackTrace();
            return lists;
        }

    }



    //根据id获取当前目录内容
    public DirectoryType getDirectoryInfoById(Map<String,Object> map){
        try{
            Object fatherid = map.get("folderId");
            Long id = fatherid == null ? null :Long.parseLong(fatherid.toString().trim());

            DirectoryType dt = directoryTypeRepository.findDircetoryById(id);

            return dt;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    //保存文件夹
    public Map<String,Object> createFolder(Map<String,Object> map){
        Map<String,Object> result = new HashMap<>();
        try{
            //文件夹名称
            String folderName = map.containsKey("name") ? map.get("name").toString() : null;
            //文件类型
            String parentType = map.containsKey("parentType") ? map.get("parentType").toString() : null;
            //父级文件夹id
            String parentId = map.containsKey("parentId")&&map.get("parentId")!=null ? map.get("parentId").toString() : null;

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String nowTime = now.format(formatter);
            DirectoryType dt = new DirectoryType();
            dt.setIcon("fa-folder");
            dt.setTranslate(folderName);
            dt.setCreatetime(nowTime);
            dt.setUpdatetime(nowTime);
            if(parentType.equals("current")){
                dt.setFatherid(Long.parseLong(parentId));
            }
            directoryTypeRepository.saveAndFlush(dt);
            result.put("success","ok");
        }catch (Exception e){
            e.printStackTrace();
            result.put("success","no");
            result.put("message",e.getMessage());
        }
        return result;
    }

    //修改文件名称
    public Map<String,Object> updateFolder(Map<String,Object> map){
        Map<String,Object> result = new HashMap<>();
        try{
            //文件夹名称
            String newName = map.containsKey("newName") ? map.get("newName").toString() : null;
            Long folderId = map.containsKey("folderId")&&map.get("folderId")!=null ? Long.parseLong(map.get("folderId").toString()) : null;
            DirectoryType dt = directoryTypeRepository.findDircetoryById(folderId);
            String icon = dt.getIcon();

            StringBuilder sb = new StringBuilder();
            sb.append("update directorytype set translate = '"+newName+"' where id = "+folderId);
            jdbcTemplate.execute(sb.toString());
            sb.setLength(0);

            if("fa-file-excel".equals(icon)){
                sb.append("update files set file_name = '"+newName+"' where parent_id = "+folderId);
                jdbcTemplate.execute(sb.toString());
                sb.setLength(0);
            }
            result.put("success","ok");
        }catch (Exception e){
            e.printStackTrace();
            result.put("success","no");
            result.put("message",e.getMessage());
        }
        return result;
    }
}
