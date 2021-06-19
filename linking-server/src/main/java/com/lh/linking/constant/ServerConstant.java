package com.lh.linking.constant;

import com.lh.linking.service.TcpServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * @do 服务端
 * @author liuhua
 * @date 2021/5/10 下午10:36
 */
@Slf4j
public final class ServerConstant {

    /**
     * 与当前外网代理服务端连接的用户客户端channel，使用其channel id作为msg的头信息进行传递
     */
    public static final Map<String, ChannelHandlerContext> USER_CLIENT_MAP = new ConcurrentHashMap<>();
    /**
     * 所有已开启的代理服务端
     */
    public static final List<TcpServer> PROXY_SERVER_LIST = Collections.synchronizedList(new ArrayList<>());
    public static final EventLoopGroup PARENT_GROUP = new NioEventLoopGroup();
    public static final EventLoopGroup CHILD_GROUP = new NioEventLoopGroup();

    /**
     * 判断当前端口的代理服务端是否存在且存活
     *
     * @param port 代理端口
     * @return 是否存在且存活
     */
    public static boolean alreadyExists(ChannelHandlerContext ctx, int port) {
        for (TcpServer tcpServer : PROXY_SERVER_LIST) {
            if (tcpServer.getPort() == port && tcpServer.getChannel().isActive()) {
                tcpServer.getInitializer().getRemoteProxyHandler().setClientCtx(ctx);
                log.info("代理端口[{}]已经注册过,", port);
                return true;
            }
        }
        return false;
    }
    public static void addProxyServer(TcpServer tcpServer) {
        PROXY_SERVER_LIST.add(tcpServer);
    }
}