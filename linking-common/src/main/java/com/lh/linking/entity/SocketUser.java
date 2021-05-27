package com.lh.linking.entity;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Objects;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;

/**
 * @author lh
 * @do
 * @date 2019-12-25 10:48
 */
@Setter
@Getter
public class SocketUser{
    /**
     * 连接用户
     */
    private String ip;
    /**
     * mac
     */
    private String mac;
    /**
     * mac地址
     */
    private Integer port;
    /**
     * 响应速度
     */
    private Integer speed;
    /**
     * 处理请求数量
     */
    private Integer handleCount;
    /**
     * 首次接入时间
     */
    private Long firstConnectDate;
    /**
     * 最近一次接入时间
     */
    private Long lastConnectDate;
    /**
     * 连接次数
     */
    private Integer connectCount;
    /**
     * 状态
     */
    private Integer state;
    /**
     * 连接信息
     */
    @JSONField(serialize = false)
    private ChannelHandlerContext channelHandlerContext;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SocketUser that = (SocketUser) o;
        return Objects.equals(mac, that.mac);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mac);
    }
}
