package com.lh.linking.handle;

import com.lh.linking.constant.ClientConstant;
import com.lh.linking.entity.ProxyConfig;
import com.lh.linking.entity.SocketMessage;
import com.lh.linking.enums.SocketCodeEnum;
import com.lh.linking.util.PropertiesUtils;
import com.lh.linking.util.SecureUtils;
import com.lh.linking.util.TcpClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @do 客户端处理
 * @author liuhua
 * @date 2021/5/10 下午10:35
 */
@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<SocketMessage> {
    /**
     * 代理配置
     */
    private final List<ProxyConfig> proxyConfigs = Collections.synchronizedList(new ArrayList<>());
    /**
     * 服务通信连接
     */
    private ChannelHandlerContext channelHandlerContext;
    /**
     * 默认重新拉起客户端的起始秒数
     */
    private static final int DEFAULT_TRY_SECONDS = 5;
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.channelHandlerContext = ctx;
        SocketMessage socketMessage = new SocketMessage();
        socketMessage.setChannelId(ctx.channel().id().asLongText());
        socketMessage.setPort(PropertiesUtils.getPropertyForInteger("server.port"));
        socketMessage.setCode(SocketCodeEnum.AUTH);
        socketMessage.setTime(System.currentTimeMillis());
        String sign = SecureUtils.md5(String.format("channelId=%s&code=%s&port=%d&time=%d&key=%s", socketMessage.getChannelId(), socketMessage.getCode(), socketMessage.getPort(), socketMessage.getTime(), PropertiesUtils.getProperty("sign.key")));
        socketMessage.setSign(sign);
        ctx.writeAndFlush(socketMessage);
    }
    /**
     * @do 获取代理配置
     * @author liuhua
     * @date 2021/5/10 下午8:29
     */
    private ProxyConfig get(int port){
        for (ProxyConfig proxyConfig : this.proxyConfigs) {
            if (proxyConfig.getRemotePort().equals(port)) {
                return proxyConfig;
            }
        }
        return null;
    }
    /**
     * @do 注册代理
     * @author liuhua
     * @param reload 强制刷新
     * @date 2021/5/10 下午8:16
     */
    public void registerProxy(List<ProxyConfig> proxyConfigs, boolean reload){
        //移除无效
        for (int i = this.proxyConfigs.size() - 1; i >= 0; i--) {
            if(!proxyConfigs.contains(this.proxyConfigs.get(i))){
                ProxyConfig proxyConfig = this.proxyConfigs.remove(i);
                SocketMessage message = new SocketMessage();
                message.setCode(SocketCodeEnum.UNREGISTER);
                message.setPort(proxyConfig.getRemotePort());
                channelHandlerContext.writeAndFlush(message);
            }
        }
        //添加新的
        for (int i = proxyConfigs.size() - 1; i >= 0; i--) {
            ProxyConfig proxyConfig = proxyConfigs.get(i);
            boolean exist = false;
            for (ProxyConfig config : this.proxyConfigs) {
                if(proxyConfig.equals(config)){
                    config.setHost(proxyConfig.getHost());
                    config.setPort(proxyConfig.getPort());
                    exist = true;break;
                }
            }
            if(!exist||reload){
                SocketMessage message = new SocketMessage();
                message.setCode(SocketCodeEnum.REGISTER);
                message.setPort(proxyConfig.getRemotePort());
                channelHandlerContext.writeAndFlush(message);
                this.proxyConfigs.add(proxyConfig);
            }
        }
    }
    @Override
    public void messageReceived(ChannelHandlerContext ctx, SocketMessage socketMessage) {
        if (socketMessage.getCode() == SocketCodeEnum.KEEPALIVE) {
            System.out.println("心跳");
            return;
        }
        if (socketMessage.getCode() == SocketCodeEnum.REGISTER_OK||socketMessage.getCode() == SocketCodeEnum.REGISTER_ERROR) {
            processRegisterResult(socketMessage);
        }if (socketMessage.getCode() == SocketCodeEnum.UNREGISTER_OK) {
            processUnRegisterResult(socketMessage);
        } else if (socketMessage.getCode() == SocketCodeEnum.CONNECTED) {
            processConnected(ctx, socketMessage);
        } else if (socketMessage.getCode() == SocketCodeEnum.DATA) {
            processData(ctx,socketMessage,0);
        } else if (socketMessage.getCode() == SocketCodeEnum.DISCONNECTED) {
            processDisconnected(ctx, socketMessage);
        }

    }


    /**
     * 重连
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                registerProxy(proxyConfigs,true);
                timer.cancel();
                log.info("重新连接成功");
                return;
            }
        },1000,DEFAULT_TRY_SECONDS*1000);
    }

    /**
     * 处理在服务端注册结果
     */
    private void processRegisterResult(SocketMessage message) {
        if (SocketCodeEnum.REGISTER_OK.equals(message.getCode())) {
            System.out.println(String.format("代理服务[%d]注册成功",message.getPort()));
        } else {
            log.error("代理服务[{}]注册失败:{}",message.getPort(),message.getMsg());
            //System.exit(0);
        }
    }
    /**
     * 处理在服务端取消注册结果
     */
    private void processUnRegisterResult(SocketMessage message) {
        System.out.println(String.format("代理服务[%d]已取消注册",message.getPort()));
    }


    /**
     * 该请求来源于，请求外网暴露的端口，外网通过netty服务端转发来到这里的
     * 请求内部代理服务，建立netty客户端，请求访问本地的服务，获取返回结果
     */
    private void processConnected(ChannelHandlerContext ctx, SocketMessage socketMessage) {
        Channel channel = ctx.channel();
        String channelId = socketMessage.getChannelId();
        int port = socketMessage.getPort();
        ProxyConfig proxyConfig = get(port);
        if(proxyConfig==null){
            SocketMessage message = new SocketMessage();
            message.setMsg(String.format("没有找到端口[%d]代理",port));
            message.setCode(SocketCodeEnum.DISCONNECTED);
            message.setChannelId(socketMessage.getChannelId());
            channel.writeAndFlush(message);
            return;
        }
        TcpClient tcpClient = null;
        try {
            tcpClient = new TcpClient().connect(proxyConfig.getHost(),proxyConfig.getPort(), ctx, channelId);
            ClientConstant.addChannel(channel, tcpClient);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            SocketMessage message = new SocketMessage();
            message.setMsg(e.getMessage());
            message.setCode(SocketCodeEnum.DISCONNECTED);
            message.setChannelId(socketMessage.getChannelId());
            channel.writeAndFlush(message);
            if(tcpClient!=null){
                tcpClient.close();
            }
        }
    }

    /**
     * 转发代理消息到内网
     */
    public void processData(ChannelHandlerContext ctx,SocketMessage message,int reply) {
        String channelId = message.getChannelId();
        ChannelHandlerContext context = ClientConstant.ID_SERVICE_CHANNEL_MAP.get(channelId);
        if (Objects.isNull(context)) {
                if(reply<1) {
                    processConnected(ctx, message);
                }
                if(reply<5) {
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            processData(ctx, message, reply + 1);
                        }
                    }, 200);
                }
        } else {
            context.writeAndFlush(message.getData());
        }
    }

    /**
     * 与代理服务端连接的用户客户端断开连接，处理资源，以及断开内网代理客户端
     */
    private void processDisconnected(ChannelHandlerContext ctx, SocketMessage message) {
        ChannelHandlerContext context = ClientConstant.ID_SERVICE_CHANNEL_MAP.remove(message.getChannelId());
        if (Objects.nonNull(context)) {
            context.close();
            ClientConstant.removeChannelByProxyClient(ctx.channel(), message.getChannelId());
        }
        ctx.close();
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.WRITER_IDLE) {
                SocketMessage message = new SocketMessage();
                message.setCode(SocketCodeEnum.KEEPALIVE);
                ctx.writeAndFlush(message);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 关闭
     */
    public void close(){
        if(channelHandlerContext!=null) {
            channelHandlerContext.close();
        }
    }
}
