package com.lh.linking.handler;

import com.lh.linking.constant.ServerConstant;
import com.lh.linking.entity.SocketMessage;
import com.lh.linking.enums.SocketCodeEnum;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
@ChannelHandler.Sharable
public class RemoteProxyHandler extends SimpleChannelInboundHandler {
    private ChannelHandlerContext clientCtx;
    private final int port;

    /**
     * 外部请求外网代理的端口时调用，保存的服务端channel会给内网客户端发送消息 proxyHandler.getCtx().writeAndFlush(message);
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        String channelId = ctx.channel().id().asLongText();
        ServerConstant.USER_CLIENT_MAP.put(channelId, ctx);
        SocketMessage message = new SocketMessage();
        message.setCode(SocketCodeEnum.CONNECTED);
        message.setPort(port);
        message.setChannelId(channelId);
        clientCtx.writeAndFlush(message);
    }

    public RemoteProxyHandler(ChannelHandlerContext clientCtx, int port) {
        this.clientCtx = clientCtx;
        this.port = port;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, Object msg) {
        byte[] data = (byte[]) msg;
        SocketMessage message = new SocketMessage();
        message.setCode(SocketCodeEnum.DATA);
        message.setPort(port);
        message.setChannelId(ctx.channel().id().asLongText());
        message.setData(data);
        clientCtx.writeAndFlush(message);
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String channelId = ctx.channel().id().asLongText();
        ServerConstant.USER_CLIENT_MAP.put(channelId, ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }

}
