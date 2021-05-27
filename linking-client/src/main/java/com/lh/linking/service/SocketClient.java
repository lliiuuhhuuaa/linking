package com.lh.linking.service;

import com.lh.linking.handle.ClientHandler;
import com.lh.linking.handle.MessageDecoder;
import com.lh.linking.handle.MessageEncoder;
import com.lh.linking.util.PropertiesUtils;
import com.lh.linking.util.ProxyConfigUtils;

import java.util.Timer;
import java.util.TimerTask;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liuhua
 * @do socket服务
 * @date 2021/4/29 下午9:57
 */
@Slf4j
public class SocketClient {
    private static SocketChannel socketChannel = null;
    private static ClientHandler clientHandler = null;
    /**
     * @do 开始服务
     * @author liuhua
     * @date 2021/4/29 下午9:57
     */
    public static void start() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.TCP_NODELAY, true); // (4)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel channel) {
                    socketChannel = channel;
                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addLast(new IdleStateHandler(20, 10, 0));
                    pipeline.addLast(new MessageDecoder());
                    pipeline.addLast(new MessageEncoder());
                    clientHandler = new ClientHandler();
                    pipeline.addLast(clientHandler);
                }
            });
            Integer port = PropertiesUtils.getPropertyForInteger("server.port");
            ChannelFuture sync = b.connect(PropertiesUtils.getProperty("server.host"),port).sync(); // (5)
            sync.addListener(future -> {
                if (future.isSuccess()) {
                    System.out.println(String.format("服务端口[%d]连接成功",port));
                    clientHandler.registerProxy(ProxyConfigUtils.getConfigs(), true);
                } else {
                    log.error("服务端口[{}]连接失败",port);
                }
            });
            sync.channel().closeFuture().sync();
            System.out.println("服务连接断开");
            //重连
            reconnect();
        } catch (Exception e) {
            log.error("socket服务异常:,{}", e.getMessage());
            //重连
            reconnect();
        } finally {
            try {
                workerGroup.shutdownGracefully().sync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 重连
     */
    private static boolean reconnecting = false;
    private static void reconnect(){
        if(reconnecting){
            return;
        }
        reconnecting = true;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("服务断开,准备重新连接...");
                reconnecting = false;
                SocketClient.start();
            }
        },2000);
    }
    /**
     * @do 开启代理
     * @author liuhua
     * @date 2021/5/10 下午1:14
     */
    public static void reloadProxy(){
        if(socketChannel!=null) {
            clientHandler.registerProxy(ProxyConfigUtils.getConfigs(), true);
        }
    }
}