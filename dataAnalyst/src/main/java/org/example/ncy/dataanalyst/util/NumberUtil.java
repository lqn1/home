package org.example.ncy.dataanalyst.util;

import java.text.DecimalFormat;
import java.text.ParsePosition;

public class NumberUtil {

    //是否是数字
    public static boolean isNumericFormat(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        // 创建DecimalFormat实例，可根据需要设置格式模式
        DecimalFormat df = new DecimalFormat();
        // 设置严格解析模式
        ParsePosition pos = new ParsePosition(0);
        Number number = df.parse(str, pos);
        // 检查是否成功解析并且整个字符串都被消费
        return number != null && pos.getIndex() == str.length();
    }
}
