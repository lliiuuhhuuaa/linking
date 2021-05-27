package com.lh.linking.handle;

import com.alibaba.fastjson.JSON;
import com.lh.linking.entity.SocketMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @do 对像处理
 * @author liuhua
 * @date 2021/5/7 下午10:51
 */
public class MessageEncoder extends MessageToByteEncoder<SocketMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, SocketMessage msg, ByteBuf byteBuf) {
        byte[] data = JSON.toJSONBytes(msg);
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);
    }
}