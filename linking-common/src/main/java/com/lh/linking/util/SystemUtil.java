package com.lh.linking.util;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;

/**
 * @author lh
 * @do
 * @date 2020-03-31 13:55
 */
public class SystemUtil {
    /**
     * @do 关闭流
     * @author lh
     * @date 2020/10/22 10:14
     */
    public static void closeStream(Closeable... closeables) {
        if(closeables==null||closeables.length<1){
            return;
        }
        for (Closeable closeable : closeables) {
            if(closeable!=null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * @do 获取APP前缀
     * @author lh
     * @date 2020/10/29 9:42
     */
    public static String getAppPrefix(){
        return System.getProperty("app.prefix");
    }
    /**
     * @do 获取本机mac地址
     * @author liuhua
     * @date 2021/4/30 下午8:46
     */
    public static String getLocalMac(){
        try {
            InetAddress ia = InetAddress.getLocalHost();
            // TODO Auto-generated method stub
            //获取网卡，获取地址
            byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < mac.length; i++) {
                if (i != 0) {
                    sb.append("-");
                }
                //字节转换为整数
                int temp = mac[i] & 0xff;
                String str = Integer.toHexString(temp);
                if (str.length() == 1) {
                    sb.append("0" + str);
                } else {
                    sb.append(str);
                }
            }
            return sb.toString().toUpperCase();
        }catch (Exception e){}
        return null;
    }
}
