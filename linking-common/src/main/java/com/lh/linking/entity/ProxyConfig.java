package com.lh.linking.entity;

import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProxyConfig {
    /**
     * 远程外网开放端口
     */
    private Integer remotePort;
    /**
     * 内网主机
     */
    private String host;
    /**
     * 内网端口
     */
    private Integer port;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProxyConfig that = (ProxyConfig) o;
        return Objects.equals(remotePort, that.remotePort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(remotePort);
    }
}
