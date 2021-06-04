package com.lh.linking.constant;

import com.lh.linking.util.TcpClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * @do 连接
 * @author liuhua
 * @date 2021/5/10 下午10:13
 */
@Slf4j
public class ClientConstant {

    /**
     * key 与外网代理服务端连接的channelid
     * value 连接到内网开启的服务端的客户端channel
     */
    public static final Map<String, ChannelHandlerContext> ID_SERVICE_CHANNEL_MAP = new ConcurrentHashMap<>();

    /**
     * key 为与外网服务端连接的channel
     * value 内网客户端开启的内网客户端，会连接到指定的内网服务，可以是多个客户端
     */
    public static final ConcurrentHashMap<Channel, List<TcpClient>> CHANNEL_MAP = new ConcurrentHashMap<>();

    public static void addChannel(Channel channel, TcpClient target) {
        List<TcpClient> tcpClients = CHANNEL_MAP.computeIfAbsent(channel, key -> new ArrayList<>());
        tcpClients.add(target);
    }
    /**
     * 根据channel id，移除channel映射
     */
    public static void removeChannelByProxyClient(Channel channel, String channelId) {
        List<TcpClient> intranetClients = CHANNEL_MAP.get(channel);
        if (Objects.nonNull(intranetClients)) {
            intranetClients.removeIf(intranetClient -> {
                if(intranetClient.getFuture().channel().id().asLongText().equals(channelId)){
                    intranetClient.close();
                    return true;
                 }
                return false;
            });
        }
        if(intranetClients.isEmpty()){
            CHANNEL_MAP.remove(channel);
        }
    }
}