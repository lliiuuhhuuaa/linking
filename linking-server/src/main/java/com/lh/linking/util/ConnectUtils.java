package com.lh.linking.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.lh.linking.entity.SocketMessage;
import com.lh.linking.entity.SocketUser;
import com.lh.linking.enums.YesNoEnum;

import org.apache.commons.lang3.RandomUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ConnectUtils {
    /**
     * 连接
     */
    private final static Map<Integer, List<SocketUser>> connections = new ConcurrentHashMap<>();
    static {
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        //反序列化连接数据
        //deserialization();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                //序列化连接数据
                serialize();
            }
        },5000,10000);
    }
    /**
     * 存入连接
     * @param ctx
     */
    public static void online(ChannelHandlerContext ctx, SocketMessage socketMessage) {
        NioSocketChannel channel = (NioSocketChannel) ctx.channel();
        SocketUser socketUser = new SocketUser();
      //  socketUser.setMac((String) socketMessage.getData());
        socketUser.setIp(channel.remoteAddress().getHostString());
        socketUser.setPort(channel.localAddress().getPort());
        socketUser.setChannelHandlerContext(ctx);
        online(socketUser);
    }
    /**
     * 存入连接
     * @param socketUser
     */
    public static void online(SocketUser socketUser){
        List<SocketUser> sockets = connections.computeIfAbsent(socketUser.getPort(),key->new ArrayList<>());
        SocketUser socket = null;
        for (SocketUser st : sockets) {
            if (st.equals(socketUser)){
                socket  = st;break;
            }
        }
        socketUser.setLastConnectDate(System.currentTimeMillis());
        if(socket!=null){
            socket.setLastConnectDate(socketUser.getLastConnectDate());
            socket.setState(YesNoEnum.YES.getValue());
            socket.setConnectCount(socket.getConnectCount()+1);
            System.out.println(String.format("%s:%d 连接成功,连接次数:%d",socketUser.getIp(),socketUser.getPort(),socket.getConnectCount()));
            return;
        }
        socketUser.setConnectCount(1);
        socketUser.setFirstConnectDate(socketUser.getLastConnectDate());
        socketUser.setHandleCount(0);
        socketUser.setState(YesNoEnum.YES.getValue());
        sockets.add(socketUser);
        System.out.println(String.format("%s:%d 首次连接成功,当前客户端数量:%d",socketUser.getIp(),socketUser.getPort(),connections.size()));
    }

    public static void offline(ChannelHandlerContext ctx) {
        NioSocketChannel channel = (NioSocketChannel) ctx.channel();
        if(channel.remoteAddress()==null){
            return;
        }
        String ip = channel.remoteAddress().getHostString();
        List<SocketUser> socketUsers = connections.get(channel.localAddress().getPort());
        if(socketUsers==null||socketUsers.isEmpty()){
            return;
        }
        SocketUser socket = null;
        for (SocketUser socketUser : socketUsers) {
            if(socketUser.getIp().equals(ip)){
                socket = socketUser;break;
            }
        }
        if(socket!=null) {
            offline(socket);
        }
    }
    /**
     * 移除连接
     * @param socketUser
     */
    public static void offline(SocketUser socketUser) {
        socketUser.setState(YesNoEnum.NO.getValue());
        System.out.println(String.format("%s:%d 连接断开,连接次数:%d",socketUser.getIp(),socketUser.getPort(),socketUser.getConnectCount()));
    }

      /**
     * 移除连接
     * @param socketUser
     */
    public static void remove(SocketUser socketUser) {
        connections.remove(socketUser.getPort());
        System.out.println(String.format("%s:%d 移除连接,当前客户端数量:%d",socketUser.getIp(),socketUser.getPort(),connections.size()));
    }

    /**
     * 取出指定mac连接
     * @param port
     */
    public static SocketUser get(Integer port){
        List<SocketUser> socketUsers = connections.get(port);
        if(socketUsers==null||socketUsers.isEmpty()){
            return  null;
        }
        List<SocketUser> waits = new ArrayList<>();
        for (SocketUser socketUser : socketUsers) {
            if(YesNoEnum.isYes(socketUser.getState())){
                waits.add(socketUser);
            }
        }
        if(waits.isEmpty()){
            return null;
        }
        return waits.get(RandomUtils.nextInt(0,waits.size()));
    }

    /**
     * 序列化信息
     */
    public static void serialize(){
        if(connections.isEmpty()){
            return;
        }
        File file = new File("./connections.lh");
        //保存描述参数对象文件
        FileOperation.save(file, JSON.toJSONString(connections, SerializerFeature.WriteClassName).getBytes(StandardCharsets.UTF_8), "6oZqGhSoG8jusnLMuWBw00rZvJc0BpBF");
    }
    /**
     * 反序列化信息
     */
    public static void deserialization(){
        File file = new File("./connections.lh");
        if(!file.exists()){
            return;
        }
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            FileOperation.getFile(file, "6oZqGhSoG8jusnLMuWBw00rZvJc0BpBF", byteArrayOutputStream);
            Map<Integer, List<SocketUser>> map = JSONObject.parseObject(byteArrayOutputStream.toString(StandardCharsets.UTF_8), Map.class);
            if (map != null && !map.isEmpty()) {
                connections.putAll(map);
            }
        }finally {
            SystemUtil.closeStream(byteArrayOutputStream);
        }
    }
    /**
     * @do 获取访问端口
     * @author liuhua
     * @date 2021/5/7 下午12:53
     */
    public static int getPort(ChannelHandlerContext channelHandlerContext) {
        NioSocketChannel channel = (NioSocketChannel) channelHandlerContext.channel();
        return channel.localAddress().getPort();
    }
    /**
     * @do 关闭
     * @author liuhua
     * @date 2022/4/16 21:35
     */
    public static void close(ChannelHandlerContext context) {
        try {
            context.deregister();
        }catch (Exception e){

        }
        try {
            context.disconnect();
        }catch (Exception e){

        }
        try {
            context.close();
        }catch (Exception e){

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
