package com.lh.linking;

import com.lh.linking.service.SocketClient;
import com.lh.linking.util.PropertiesUtils;

public class LinkingClientApplication {

    public static void main(String[] args) {
        loadParam(args);
        SocketClient.start();
    }
    /**
     * @do 加载启动参数
     * @author liuhua
     * @date 2021/5/27 下午9:53
     */
    private static void loadParam(String[] args){
        if(args!=null&&args.length>0){
            for (String arg : args) {
                String[] split = arg.split("=");
                if(split.length!=2){
                    continue;
                }
                if("server.host".equals(split[0])){
                    PropertiesUtils.setProperty("server.host",split[1]);
                }else if("server.port".equals(split[0])){
                    PropertiesUtils.setProperty("server.port",split[1]);
                }
            }
        }
    }
}
