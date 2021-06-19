package com.lh.linking.service;

import com.lh.linking.constant.ServerConstant;
import com.lh.linking.handler.RemoteProxyHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @do tcp代理
 * @author liuhua
 * @date 2021/5/9 下午4:17
 */
@Data
@Slf4j
public class TcpServer {
    private TcpServerInitializer initializer;

    /**
     * 当前代理的serverChannel
     */
    private Channel channel;
    private int port;
    public TcpServer initTcpServer(int port, ChannelHandlerContext clientCtx) {
        this.port = port;
        this.initializer = new TcpServerInitializer(clientCtx,port);
        ServerBootstrap b = new ServerBootstrap();
        b.group(ServerConstant.PARENT_GROUP,ServerConstant.CHILD_GROUP)
                .channel(NioServerSocketChannel.class)
                .childHandler(initializer)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        ChannelFuture future = b.bind(port);
        future.addListener(f -> {
            if (f.isSuccess()) {
                log.info("代理端口{}注册成功", port);
            } else {
                log.error("代理端口{}注册失败", port);
            }
        });
        this.channel = future.channel();
        return this;
    }

    public static class TcpServerInitializer extends ChannelInitializer<SocketChannel> {
        @Getter
        private final RemoteProxyHandler remoteProxyHandler;

        public TcpServerInitializer(ChannelHandlerContext clientCtx, int port) {
            this.remoteProxyHandler =  new RemoteProxyHandler(clientCtx, port);
        }

        @Override
        protected void initChannel(SocketChannel ch) {
            ch.pipeline().addLast(new ByteArrayDecoder(), new ByteArrayEncoder());
            ch.pipeline().addLast("remoteHandler", remoteProxyHandler);
        }
    }
}
