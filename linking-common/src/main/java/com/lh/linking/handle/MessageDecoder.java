package com.lh.linking.handle;

import com.alibaba.fastjson.JSON;
import com.lh.linking.entity.SocketMessage;

import java.net.http.HttpRequest;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

/**
 * @do 对像处理
 * @author liuhua
 * @date 2021/5/7 下午10:51
 */
public class MessageDecoder extends ReplayingDecoder<Void> {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        try {
            int dataLen = byteBuf.readInt();
            byte[] data = new byte[dataLen];
            byteBuf.readBytes(data);
            SocketMessage message = JSON.parseObject(data, SocketMessage.class);
            list.add(message);
        }catch (Exception e){
        }
    }
}