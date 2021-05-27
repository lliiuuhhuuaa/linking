package com.lh.linking;

import com.lh.linking.service.SocketServer;
import com.lh.linking.util.PropertiesUtils;

public class LinkingServerApplication {

    public static void main(String[] args) {
        loadParam(args);
        SocketServer server = new SocketServer();
        server.start(PropertiesUtils.getPropertyForInteger("server.port"));
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
                if("server.port".equals(split[0])){
                    PropertiesUtils.setProperty("server.port",split[1]);
                }
            }
        }
    }
}
