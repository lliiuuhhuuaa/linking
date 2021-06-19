package com.lh.linking.service;

import com.lh.linking.constant.ServerConstant;
import com.lh.linking.handle.MessageDecoder;
import com.lh.linking.handle.MessageEncoder;
import com.lh.linking.handler.TcpServerHandler;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liuhua
 * @do socket服务
 * @date 2021/4/29 下午9:57
 */
@Slf4j
public class SocketServer {
    /**
     * @do 开始服务
     * @author liuhua
     * @date 2021/4/29 下午9:57
     */
    public void start(int port) {
        try {
            //创建启动类
            ServerBootstrap b = new ServerBootstrap();
            //配置各组件
            b.group(ServerConstant.PARENT_GROUP,ServerConstant.CHILD_GROUP)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 2048)
                        .option(ChannelOption.SO_SNDBUF, 2048*1024)
                    .option(ChannelOption.SO_RCVBUF, 2048*1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)//维持连接的活跃，清除死连接
                    .childOption(ChannelOption.TCP_NODELAY, true)//关闭延迟发送
                    .childHandler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel channel) {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast(new IdleStateHandler(20, 10, 0, TimeUnit.SECONDS));
                            pipeline.addLast("decode",new MessageDecoder());
                            pipeline.addLast("encode",new MessageEncoder());
                            pipeline.addLast("businessHandler", new TcpServerHandler());
                        }
                    });
            ChannelFuture sync = b.bind(port).sync();
            sync.addListener(future -> {
                if (future.isSuccess()) {
                    System.out.println(port + "端口监听成功");
                } else {
                    System.out.println(port + "端口监听失败");
                }
            });
            sync.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("socket服务异常:,{}", e.getMessage());
        }
    }
}