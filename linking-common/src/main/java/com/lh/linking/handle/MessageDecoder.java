package com.lh.linking.handle;

import com.alibaba.fastjson.JSON;
import com.lh.linking.entity.SocketMessage;

import java.net.http.HttpRequest;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @do 对像处理
 * @author liuhua
 * @date 2021/5/7 下午10:51
 */
@Slf4j
public class MessageDecoder extends ReplayingDecoder<Void> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        try {
            log.error("byte length:"+byteBuf.readInt());
            System.out.println(byteBuf.readInt());
            byte[] data = new byte[byteBuf.readInt()];
            byteBuf.readBytes(data);
            list.add(JSON.parseObject(data, SocketMessage.class));
        }catch (Exception e){
        }

    }
}