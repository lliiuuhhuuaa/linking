package com.lh.linking.handler;

import com.lh.linking.constant.ServerConstant;
import com.lh.linking.entity.SocketMessage;
import com.lh.linking.enums.SocketCodeEnum;
import com.lh.linking.service.TcpServer;
import com.lh.linking.util.PropertiesUtils;
import com.lh.linking.util.SecureUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : LiuMing
 * 2019/11/4 10:50
 * tcp handler
 */
@Slf4j
@ChannelHandler.Sharable
public class TcpServerHandler extends SimpleChannelInboundHandler<SocketMessage> {
    private List<String> authChannel = new ArrayList<>();
    /**
     * 默认读超时上限
     */
    private static final byte DEFAULT_RECONNECTION_LIMIT = 5;
    private static final Map<ChannelHandlerContext, Integer> DEFAULT_COUNT = new ConcurrentHashMap<>();

    @Override
    public void messageReceived(ChannelHandlerContext ctx, SocketMessage socketMessage) {
        SocketCodeEnum codeEnum = socketMessage.getCode();
        String channelId = ctx.channel().id().asLongText();
        if (codeEnum == SocketCodeEnum.AUTH) {
            String sign = SecureUtils.md5(String.format("channelId=%s&code=%s&port=%d&time=%d&key=%s", socketMessage.getChannelId(), socketMessage.getCode(), socketMessage.getPort(), socketMessage.getTime(), PropertiesUtils.getProperty("sign.key")));
            if(sign.equals(socketMessage.getSign())){
                authChannel.add(channelId);
            }
            return;
        }
        if ((codeEnum == SocketCodeEnum.REGISTER || codeEnum == SocketCodeEnum.UNREGISTER) && !authChannel.contains(channelId)) {
            ctx.close();
            return;
        }
        //客户端注册
        if (codeEnum == SocketCodeEnum.REGISTER) {
            processRegister(ctx, socketMessage);
        } else if (codeEnum == SocketCodeEnum.UNREGISTER) {
            processUnRegister(ctx, socketMessage);
        } else if (codeEnum == SocketCodeEnum.DISCONNECTED) {
            processDisconnected(socketMessage);
        } else if (codeEnum == SocketCodeEnum.DATA) {
            processData(socketMessage);
        } else if (codeEnum == SocketCodeEnum.KEEPALIVE) {
            // 心跳包
            DEFAULT_COUNT.put(ctx, 0);
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        authChannel.remove(ctx.channel().id().asLongText());
    }

    /**
     * 处理客户端注册,每个客户端注册成功都会启动一个服务，绑定客户端指定的端口
     *
     * @param ctx 与当前服务端保持连接的内网客户端channel
     */
    private void processRegister(ChannelHandlerContext ctx, SocketMessage socketMessage) {
        boolean alreadyExists = ServerConstant.alreadyExists(ctx, socketMessage.getPort());
        if (!alreadyExists) {
            TcpServer tcpServer = new TcpServer().initTcpServer(socketMessage.getPort(), ctx);
            ServerConstant.addProxyServer(tcpServer);
        }
        socketMessage.setCode(SocketCodeEnum.REGISTER_OK);
        ctx.writeAndFlush(socketMessage);
    }

    /**
     * @do 处理客户端取消注册
     * @author liuhua
     * @date 2021/5/10 下午9:30
     */
    private void processUnRegister(ChannelHandlerContext ctx, SocketMessage socketMessage) {
        for (int i = ServerConstant.PROXY_SERVER_LIST.size() - 1; i >= 0; i--) {
            if (ServerConstant.PROXY_SERVER_LIST.get(i).getPort() == socketMessage.getPort()) {
                TcpServer tcpServer = ServerConstant.PROXY_SERVER_LIST.remove(i);
                tcpServer.getChannel().close();
                log.info("代理端口{}取消注册成功", socketMessage.getPort());
                socketMessage.setCode(SocketCodeEnum.UNREGISTER_OK);
                ctx.writeAndFlush(socketMessage);
                return;
            }
        }
    }

    /**
     * 处理收到转发的内网响应数据包
     */
    private void processData(SocketMessage socketMessage) {
        ChannelHandlerContext userCtx = ServerConstant.USER_CLIENT_MAP.get(socketMessage.getChannelId());
        if (Objects.isNull(userCtx)) {
            log.error("received intranet proxy client message，but the corresponding proxy server was not found! ");
        } else {
            userCtx.writeAndFlush(socketMessage.getData());
        }
    }

    /**
     * 断开,先关闭外网暴露的代理，再关闭连接的客户端
     */
    private void processDisconnected(SocketMessage message) {
        ChannelHandlerContext userCtx = ServerConstant.USER_CLIENT_MAP.get(message.getChannelId());
        if (Objects.nonNull(userCtx)) {
            userCtx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.error("the client({}) is disconnected", ctx.channel().remoteAddress());
        if (ctx.channel().isActive()) {
            ctx.close();
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            Integer count = DEFAULT_COUNT.get(ctx);
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                if (Objects.isNull(count)) {
                    count = 0;
                }
                DEFAULT_COUNT.put(ctx, count++);
                if (count > DEFAULT_RECONNECTION_LIMIT) {
                    DEFAULT_COUNT.remove(ctx);
                    log.error("read idle  will loss connection. retryNum:{}", count);
                    ctx.close();
                }
            }
        } else {
            try {
                super.userEventTriggered(ctx, evt);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
