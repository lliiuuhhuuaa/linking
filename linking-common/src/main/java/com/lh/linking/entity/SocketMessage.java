package com.lh.linking.entity;


import com.lh.linking.enums.SocketCodeEnum;

import lombok.Getter;
import lombok.Setter;

/**
 * socket交互对象
 */
@Setter
@Getter
public class SocketMessage {
	/**
	 * 编号
	 */
	private SocketCodeEnum code;
	/**
	 * 消息
	 */
	private String msg;
	/**
	 * 端口
	 */
	private Integer port;
	/**
	 * 连接ID
	 */
	private String channelId;
	/**
	 * 时间戳
	 */
	private Long time;
	/**
	 * 签名
	 */
	private String sign;
	/**
	 * 数据
	 */
	private byte[] data;
}
