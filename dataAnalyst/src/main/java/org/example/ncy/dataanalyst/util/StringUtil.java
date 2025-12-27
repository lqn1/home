package org.example.ncy.dataanalyst.util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 类:  <code> StringUtil </code>
 * 功能描述: String字符串操作工具栏
 * 创建人:lqn
 * 创建日期: Apr 1, 2013 7:21:47 PM
 * 开发环境: JDK7.0
 */
public class StringUtil implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private static final String reg = "\\[.*\\d+.*\\]";

    /**
     * 功能描述:传入一个对象判断是否为空
     * @param obj
     * @return boolean为空返回true
     */
    public static boolean isNull(Object obj) {
        return obj == null;
    }

    /**
     * 判断对象是否不为空
     * @param obj
     * @return
     */
    public static boolean notNull(Object obj) {
        return obj != null;
    }

    /**
     * 判断字符串是否为空
     * @param obj
     * @return
     */
    public static boolean isNull(String obj) {
        return obj == null || obj.isEmpty();
    }

    /**
     * 判断字符串是否不为空
     * @param obj
     * @return
     */
    public static boolean notNull(String obj) {
        return obj != null && !obj.isEmpty();
    }

    /**
     * 判断字符串是否有为空格值
     * @param text
     * @return
     */
    public static boolean hasText(String text) {
        return !StringUtil.isNull(text.trim());
    }

    /**
     * 判断set集合是否为空
     */
    public static boolean setIsNull(Set<?> mains) {
        return mains == null || mains.isEmpty();
    }

    /**
     * 判断map集合是否为空
     * @param mapSet
     * @return
     */
    public static boolean mapIsNull(Map<?, ?> mapSet) {
        return mapSet == null || mapSet.isEmpty();
    }

    /**
     * 判断map是否不为空
     * @param map
     * @return
     */
    public static boolean mapNotNull(Map map) {
        return map != null && !map.isEmpty();
    }

    /**
     * 判断list是否为空
     * @param mains
     * @return
     */
    public static boolean listIsNull(List<?> mains) {
        return mains == null || mains.isEmpty();
    }

    public static boolean listNotNull(List<?> mains) {
        return mains != null && !mains.isEmpty();
    }

    /**
     * 判断对象数组是否不为空
     * @param mains
     * @return
     */
    public static boolean arrNotNull(Object[] mains) {
        return mains != null && mains.length > 0;
    }

    /**
     * 判断对象数组是否为空
     * @param mains
     * @return
     */
    public static boolean arrayIsNull(Object[] mains) {
        return mains == null || mains.length == 0;
    }

    /**
     * 判断字符串数组是否为空
     * @param strs
     * @return
     */
    public static boolean isNull(String[] strs) {
        return (strs == null || strs.length < 1);
    }

    /**
     * 从map中获取一个值
     * @param mapSet map集合
     * @param key map的键
     * @param <T> 泛型
     * @return
     */
    public static <T> T mapValue(Map<?, ?> mapSet, String key) {
        if (mapSet != null && mapSet.containsKey(key)) {
            return (T) mapSet.get(key);
        }
        return null;
    }

    /**
     * 从map中获取一个字符串值
     * @param mapSet map集合
     * @param key map键
     * @return
     */
    public static String mapValueString(Map<?, ?> mapSet, String key) {
        if (mapSet != null && mapSet.containsKey(key)) {
            return StringUtil.valueOf(mapSet.get(key));
        }
        return "";
    }

    /**
     * 从map中获取一个字符串值 为空返回一个默认值
     * @param mapSet map集合
     * @param key 键
     * @param defaultValue 默认值
     * @return 值
     */
    public static String mapValueString(Map<?, ?> mapSet, String key, String defaultValue) {
        if (mapSet != null && mapSet.containsKey(key)) {
            return StringUtil.valueOf(mapSet.get(key), defaultValue);
        }
        return defaultValue;
    }

    /**
     * 输出字符串 为空时返回空字符串
     * @param value
     * @return
     */
    public static String valueOf(String value) {
        return (isNull(value) ? "" : value);
    }

    /**
     * 将int转换为字符串输出 为空返回空字符串
     * @param value
     * @return
     */
    public static String valueOf(Integer value) {
        return (isNull(value) ? "" : String.valueOf(value));
    }

    /**
     * 将一个对象以字符串形式输出 为空返回空字符串
     * @param value
     * @return
     */
    public static String valueOf(Object value) {
        return (isNull(value) ? "" : String.valueOf(value));
    }

    /**
     * 输出一个字符串 为空返回默认值
     * @param value
     * @param defaults
     * @return
     */
    public static String valueOf(String value, String defaults) {
        if (StringUtil.isNull(value)) {
            return valueOf(defaults);
        }
        return valueOf(value);
    }

    /**
     * 将一个对象转为字符串输出 为空返回默认值
     * @param value
     * @param defaults
     * @return
     */
    public static String valueOf(Object value, String defaults) {
        if (StringUtil.isNull(value)) {
            return valueOf(defaults);
        }
        return valueOf(value);
    }

    /**
     * 返回数组中第一个非空对象
     * @param values
     * @param <T>
     * @return
     */
    public static <T> T valueOf(T... values) {
        for (T value : values) {
            if (StringUtil.notNull(value)) {
                return value;
            }
        }
        return null;
    }

    public static Integer integerValueOf(Integer value, Integer defaults) {
        if (value == null) {
            return defaults;
        }
        return value;
    }

    public static Integer integerValueOf(String value, Integer defaults) {
        if (StringUtil.isNull(value)) {
            return defaults;
        }
        return Integer.parseInt(value);
    }

    public static Double doubleValueOf(Double value, Double defaults) {
        if (value == null) {
            return defaults;
        }
        return value;
    }

    public static Double doubleValueOf(String value, Double defaults) {
        if (StringUtil.isNull(value)) {
            return defaults;
        }
        return Double.parseDouble(value);
    }

    public static Long longValueOf(Long value, Long defaults) {
        if (value == null) {
            return defaults;
        }
        return value;
    }

    public static Boolean booleanValueOf(Boolean value, Boolean defaults) {
        if (value == null) {
            return defaults;
        }
        return value;
    }

    /**
     * 截取指定字符串（判断长度）
     * @param str
     * @param length
     * @return
     */
    public static String subString(String str, int length) {
        int strLength = str.length();
        if (strLength > length) {
            return str.substring(0, length);
        }
        return str;
    }

    /**
     * 将多个字符串拼接成一个字符串
     * @param strings
     * @return
     */
    public static String append(Object... strings) {
        StringBuilder sb = new StringBuilder();
        for (Object s : strings) {
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     * 指定分隔符拼接字符串
     * @param sep 分隔符
     * @param str 要拼接的字符串
     * @return
     */
    public static String join(String sep, String... str) {
        StringBuilder sb = new StringBuilder();
        if (arrayIsNull(str)) {
            return "";
        }
        int num = str.length;
        int size = num - 1;
        int i = 0;
        for (; i < size; i++) {
            sb.append(str[i]).append(sep);
        }
        sb.append(str[i]);
        return sb.toString();
    }

    /**
     * 指定分隔符拼接字符串列表
     * @param sep 分隔符
     * @param str 字符串列表
     * @return
     */
    public static String join(String sep, List<String> str) {
        if (listIsNull(str)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int num = str.size();
        int size = num - 1;
        int i = 0;
        for (; i < size; i++) {
            sb.append(str.get(i)).append(sep);
        }
        sb.append(str.get(i));
        return sb.toString();
    }

    /**
     * 将字符串中 ${xxx} 替换为参数
     * @param content
     * @param kvs
     * @return
     */
    public static String parse$(String content, Map<String, String> kvs) {
        Pattern p = Pattern.compile("(\\$\\{)([\\w]+)(\\})");
        return parse(p, content, kvs);
    }

    /**
     * 将字符串中 {} 大括号括起来的部分替换为map中的值
     * @param content
     * @param kvs
     * @return
     */
    public static String parse(String content, Map<String, String> kvs) {
        Pattern p = Pattern.compile("(\\{)([\\w]+)(\\})");
        return parse(p, content, kvs);
    }

    private static String parse(Pattern p, String content, Map<String, String> kvs) {
        Matcher m = p.matcher(content);
        StringBuffer sr = new StringBuffer();
        while (m.find()) {
            String group = m.group();
            m.appendReplacement(sr, String.valueOf(kvs.get(group)));
        }
        m.appendTail(sr);
        return sr.toString();
    }

    /**
     * 创建一个set集合
     * @param vars
     * @param <T>
     * @return
     */
    public static <T> Set<T> createSet(T... vars) {
        Set<T> set = new HashSet<>();
        for (T obj : vars) {
            set.add(obj);
        }
        return set;
    }

    /**
     * 创建一个list集合
     * @param vars
     * @param <T>
     * @return
     */
    public static <T> List<T> createList(T... vars) {
        List<T> set = new ArrayList<>();
        for (T obj : vars) {
            set.add(obj);
        }
        return set;
    }
}