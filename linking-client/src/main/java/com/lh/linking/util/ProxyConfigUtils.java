package com.lh.linking.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.lh.linking.entity.ProxyConfig;
import com.lh.linking.service.SocketClient;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import lombok.extern.slf4j.Slf4j;

/**
 * @do 代理配置
 * @author liuhua
 * @date 2021/5/10 下午1:29
 */
@Slf4j
public class ProxyConfigUtils {
    /**
     * 配置信息
     */
    private static List<ProxyConfig> proxyConfigs = new ArrayList<>();
    private static long lastModified =  0L;
    static {
        //加载配置
        load(true);
        //每分钟扫描一次配置文件
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                load(false);
            }
        },60000,60000);

    }
    /**
     * @do 加载代理配置
     * @author liuhua
     * @date 2021/5/10 下午8:42
     */
    private static void load(boolean first){
        File file = new File("proxy.conf");
        if(!file.exists()){
            log.error("没有找到代理配置文件");
        }else {
            if(lastModified==file.lastModified()){
                return;
            }
            lastModified = file.lastModified();
            StringBuilder sb = new StringBuilder();
            BufferedReader bufferedReader = null;
            try {
                String line = null;
                bufferedReader = new BufferedReader(new FileReader(file));
                while ((line=bufferedReader.readLine())!=null){
                    sb.append(line);
                }
                if(sb.length()<1){
                    log.error("没有找到代理配置文件");
                    return;
                }
                List<ProxyConfig> configs = JSONArray.parseArray(sb.toString(), ProxyConfig.class);
                proxyConfigs.clear();
                if(configs!=null&&!configs.isEmpty()){
                    for (int i = configs.size() - 1; i >= 0; i--) {
                        ProxyConfig proxyConfig = configs.get(i);
                        if(proxyConfig.getRemotePort()==null||proxyConfig.getPort()==null){
                            configs.remove(i);
                            log.error("移除无效代理配置:{}", JSONObject.toJSONString(proxyConfig));
                            continue;
                        }
                        if(StringUtils.isBlank(proxyConfig.getHost())){
                            proxyConfig.setHost("127.0.0.1");
                        }
                    }
                }
                proxyConfigs.clear();
                if(configs!=null&&!configs.isEmpty()) {
                    proxyConfigs.addAll(configs);
                }
                if(!first) {
                    SocketClient.reloadProxy();
                }
            } catch (JSONException e) {
                log.error("配置文件格式错误:{}",e.getMessage());
            } catch (Exception e) {
                log.error("配置文件加载错误:{}",e.getMessage());
            }finally {
                SystemUtil.closeStream(bufferedReader);
            }
        }
    }
    /**
     * @do 获取配置
     * @author liuhua
     * @date 2021/5/10 下午8:21
     */
    public static List<ProxyConfig> getConfigs() {
        return proxyConfigs;
    }
}
