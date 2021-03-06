package com.lh.linking.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @do 属性配置
 * @author liuhua
 * @date 2021/5/10 下午12:47
 */
public class PropertiesUtils{
    /**
     * 配置
     */
    private static Properties properties = new Properties();
    static{
        InputStream resourceAsStream = PropertiesUtils.class.getResourceAsStream("/application.properties");
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            SystemUtil.closeStream(resourceAsStream);
        }
    }

    /**
     * 获取配置
     * @param key
     * @return
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
    /**
     * 获取配置
     * @param key
     * @return
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    /**
     * 获取配置
     * @param key
     * @return
     */
    public static Integer getPropertyForInteger(String key, Integer defaultValue) {
        String property = getProperty(key);
        if(property==null){
            return defaultValue;
        }
        return Integer.valueOf(property);
    }
    /**
     * 获取配置
     * @param key
     * @return
     */
    public static Integer getPropertyForInteger(String key) {
        return getPropertyForInteger(key,null);
    }
    /**
     * @do 覆盖配置
     * @author liuhua
     * @date 2021/5/27 下午9:54
     */
    public static void setProperty(String key, String value) {
        properties.setProperty(key,value);
    }
}
