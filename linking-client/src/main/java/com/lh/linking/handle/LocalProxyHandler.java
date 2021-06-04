package com.lh.linking.handle;


import com.lh.linking.constant.ClientConstant;
import com.lh.linking.entity.SocketMessage;
import com.lh.linking.enums.SocketCodeEnum;
import com.lh.linking.util.TcpClient;

import java.util.Iterator;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @do 外部请求到公网服务器，公网服务器将请求转发到当前服务器，当前服务器建立客户端，访问本地服务
 * @author liuhua
 * @date 2021/5/27 下午8:56
 */
@Slf4j
public class LocalProxyHandler extends SimpleChannelInboundHandler<Object> {

    /**
     * 本机的netty客户端，该客户端和公网的netty服务端有一个长链接，使用该channel发送消息到公网netty服务端，
     * 之后服务端再将结果响应给外部的请求
     */
    private final ChannelHandlerContext serverChannel;
    private final String remoteChannelId;

    public LocalProxyHandler(ChannelHandlerContext serverChannel, String remoteChannelId) {
        this.serverChannel = serverChannel;
        this.remoteChannelId = remoteChannelId;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ClientConstant.ID_SERVICE_CHANNEL_MAP.put(remoteChannelId, ctx);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, Object msg) {
        byte[] data = (byte[]) msg;
        SocketMessage message = new SocketMessage();
        message.setCode(SocketCodeEnum.DATA);
        message.setData(data);
        message.setChannelId(remoteChannelId);
        serverChannel.writeAndFlush(message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ChannelHandlerContext remove = ClientConstant.ID_SERVICE_CHANNEL_MAP.remove(remoteChannelId);
        if(remove!=null) {
            remove.close();
        }
        ClientConstant.CHANNEL_MAP.values().forEach(intranetClients -> {
            Iterator<TcpClient> iterator = intranetClients.iterator();
            while (iterator.hasNext()) {
                TcpClient next = iterator.next();
                if (next.getFuture().channel() == ctx.channel()) {
                    iterator.remove();
                    next.close();
                }
            }
        });
    }
}
