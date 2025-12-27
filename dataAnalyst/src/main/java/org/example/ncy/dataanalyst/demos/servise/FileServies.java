package org.example.ncy.dataanalyst.demos.servise;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Service
public interface FileServies {
    Map<String,Object> deleteExcel(Map<String,Object> map);

}
