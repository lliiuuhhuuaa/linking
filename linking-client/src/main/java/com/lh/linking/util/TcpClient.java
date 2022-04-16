package com.lh.linking.util;

import com.lh.linking.constant.ClientConstant;
import com.lh.linking.handle.LocalProxyHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 内网代理客户端
 */
@Getter
@Slf4j
public class TcpClient {
    private ChannelFuture future = null;
    /**
     * 开启内网代理客户端
     * @param host          内网代理客户端的host
     * @param port          内网代理客户端的port
     * @param serverChannel 与服务端交互的channel
     * @param channelId     外网代理服务端的channel id
     * @return 内网代理客户端channel
     */
    public TcpClient connect(String host, int port, ChannelHandlerContext serverChannel, String channelId) {
        try {
            Bootstrap b = new Bootstrap();
            b.group(ClientConstant.nioEventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true) // (4)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            LocalProxyHandler localProxyHandler = new LocalProxyHandler(serverChannel, channelId);
                            ch.pipeline().addLast(new ByteArrayDecoder(), new ByteArrayEncoder(), localProxyHandler);
                        }
                    });
            future = b.connect(host, port).sync();
            future.addListener(f -> {
                if (f.isSuccess()) {
                    log.debug("connect {}:{} success", host, port);
                } else {
                    log.error("connect client proxy fail，host:{} port:{}", host, port);
                }
            });
            future.channel().closeFuture();
        }catch (Exception e){
            close();
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * 关闭
     */
    public void close(){
        if(future!=null){
            close(future.channel());
        }
    }
    /**
     * @do 关闭
     * @author liuhua
     * @date 2022/4/16 21:35
     */
    public static void close(Channel channel) {
        try {
            channel.deregister();
        }catch (Exception e){

        }
        try {
            channel.disconnect();
        }catch (Exception e){

        }
        try {
            channel.close();
        }catch (Exception e){

        }
    }
}