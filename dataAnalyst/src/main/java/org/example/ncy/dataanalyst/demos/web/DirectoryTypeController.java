/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.example.ncy.dataanalyst.demos.web;

import org.example.ncy.dataanalyst.demos.entity.DirectoryType;
import org.example.ncy.dataanalyst.demos.servise.DirectoryTypeServies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@Controller
public class DirectoryTypeController {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryTypeController.class);
    @Resource
    private DirectoryTypeServies directoryTypeServies;
    /*
     *   打开首页
     *   http://127.0.0.1:8080/ncy/home
     */
    @RequestMapping("/ncy/home")
    public String html() {
        return "forward://data/html/dataAnalyst.html";
    }


    @RequestMapping("/ncy/static/home")
    public String statichome() {
        return "forward://data/html/index.html";
    }


    /**************************************************************************************
     * 查询左侧导航栏
     **************************************************************************************/
    @PostMapping("/ncy/topDirectory")
    public ResponseEntity findTopDircetory(HttpServletRequest request, HttpServletResponse response) {
        List<DirectoryType> allDirectoryType = directoryTypeServies.findTopDircetory();
        // 直接返回列表，Spring会自动转换为JSON数组
        return ResponseEntity.ok(allDirectoryType);
    }
    /**************************************************************************************
     * 获取当前目录下所有内容
     *    复用：所有目录下所有内容，包括左侧导航栏和右侧展示栏内的目录
     **************************************************************************************/
    @PostMapping("/ncy/nextDirectory/contents")
    public ResponseEntity findNextDircetoryContents(@RequestBody Map<String,Object> map) {
        List<DirectoryType> allDirectoryType = directoryTypeServies.findNextDircetoryContents(map);
        // 直接返回列表，Spring会自动转换为JSON数组
        return ResponseEntity.ok(allDirectoryType);
    }

    /**************************************************************************************
     * 获取上层级的目录所有内容
     **************************************************************************************/
    @PostMapping("/ncy/lastDirectory/contents")
    public ResponseEntity findLastDircetoryContents(@RequestBody Map<String,Object> map) {
        List<DirectoryType> allDirectoryType = directoryTypeServies.findLastDircetoryContents(map);
        // 直接返回列表，Spring会自动转换为JSON数组
        return ResponseEntity.ok(allDirectoryType);
    }

    /**************************************************************************************
     * 根据id获取文件对象内容
     **************************************************************************************/
    @PostMapping("/ncy/getDirectoryInfoById/")
    public ResponseEntity getDirectoryInfoById(@RequestBody Map<String,Object> map) {
        DirectoryType dt = directoryTypeServies.getDirectoryInfoById(map);
        List<DirectoryType> list = new ArrayList<>();
        list.add(dt);
        // 直接返回列表，Spring会自动转换为JSON数组
        return ResponseEntity.ok(list);
    }


    /**************************************************************************************
     * 根据id获取文件对象内容
     **************************************************************************************/
    @PostMapping("/ncy/createFolder/")
    public ResponseEntity createFolder(@RequestBody Map<String,Object> map) {
        Map<String,Object> result = directoryTypeServies.createFolder(map);
        // 直接返回列表，Spring会自动转换为JSON数组
        return ResponseEntity.ok(result);
    }


    //修改文件名称
    @PostMapping("/ncy/updateFolder")
    public ResponseEntity<Map<String, Object>> updateFolder(@RequestBody Map<String,Object> map) {

        Map<String, Object> result = new HashMap<>();

        try {
            result = directoryTypeServies.updateFolder(map);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("删除失败"+e.getMessage(), e);
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

}
