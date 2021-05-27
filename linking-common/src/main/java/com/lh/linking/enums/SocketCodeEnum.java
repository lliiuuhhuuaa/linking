package com.lh.linking.enums;

public enum SocketCodeEnum {

    REGISTER(200), // 注册
    AUTH(800), // 认证
    REGISTER_OK(202), // 注册成功
    REGISTER_ERROR(302), // 注册失败
    UNREGISTER(207), // 取消注册
    UNREGISTER_OK(208), // 取消注册成功
    DATA(210), // 数据
    KEEPALIVE(201), // http请求
    CONNECTED(205), // 连接
    DISCONNECTED(300), // 连接断开
    ;
    private Integer value;

    SocketCodeEnum(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    /**
     * 获取元素
     *
     * @param value
     * @return
     */
    public static SocketCodeEnum getEnum(Integer value) {
        SocketCodeEnum[] values = SocketCodeEnum.values();
        for (SocketCodeEnum em : values) {
            if (em.getValue().equals(value)) {
                return em;
            }
        }
        return null;
    }
}
