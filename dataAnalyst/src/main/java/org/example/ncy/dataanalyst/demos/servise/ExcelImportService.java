package org.example.ncy.dataanalyst.demos.servise;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
public interface ExcelImportService {

    public Map<String,Object> importExcel(MultipartFile file, Long parentId, HttpServletRequest request);
}