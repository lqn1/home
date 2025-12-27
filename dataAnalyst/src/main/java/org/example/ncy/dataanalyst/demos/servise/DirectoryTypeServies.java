package org.example.ncy.dataanalyst.demos.servise;

import org.example.ncy.dataanalyst.demos.entity.DirectoryType;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Service
public interface DirectoryTypeServies {
    List<DirectoryType> findTopDircetory();//查询所有一级目录
    List<DirectoryType> findNextDircetoryContents(Map<String,Object> map);//查询目录内所有内容
    List<DirectoryType> findLastDircetoryContents(Map<String,Object> map);//查询上级目录所有内容
    DirectoryType getDirectoryInfoById(Map<String,Object> map);//根据id获取目录信息
    Map<String,Object> createFolder(Map<String,Object> map);//保存文件夹
    Map<String,Object> updateFolder(Map<String,Object> map);//保存文件夹
}
