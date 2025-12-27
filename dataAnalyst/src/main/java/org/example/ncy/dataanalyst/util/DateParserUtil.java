package org.example.ncy.dataanalyst.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateParserUtil {

    /**
     * 解析英文格式日期字符串
     */
    public static Date parseEnglishDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        // 尝试多种可能的格式
        String[] patterns = {
                "EEE MMM dd HH:mm:ss zzz yyyy", // Fri Aug 29 12:21:42 CST 2014
                "EEE MMM dd HH:mm:ss z yyyy",    // 另一种时区格式
                "MMM dd, yyyy HH:mm:ss zzz",     // Aug 29, 2014 12:21:42 CST
                "yyyy-MM-dd HH:mm:ss zzz"        // 2014-08-29 12:21:42 CST
        };

        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                sdf.setLenient(false); // 严格模式
                return sdf.parse(dateStr);
            } catch (ParseException e) {
                // 继续尝试下一种格式
                continue;
            }
        }

        throw new IllegalArgumentException("无法解析日期格式: " + dateStr);
    }

    /**
     * 安全解析，返回null而不是抛出异常
     */
    public static Date safeParseEnglishDate(String dateStr) {
        try {
            return parseEnglishDate(dateStr);
        } catch (Exception e) {
            return null;
        }
    }


    //是否是日期格式
    public static boolean isDateFormat(String str, String dateFormatPattern) {
        if (str == null || dateFormatPattern == null) {
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormatPattern);
        sdf.setLenient(false); // 设置为严格模式，这一点很重要[1](@ref) 避免无效日期（如 "2023-02-30"）被错误接受
        try {
            Date date = sdf.parse(str);
            return true; // 如果能成功解析，则符合该日期格式
        } catch (Exception e) {
            return false; // 解析出错，则不符合
        }
    }


}
