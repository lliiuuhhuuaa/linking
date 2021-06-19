# 基于netty5内网穿透linking
- 1.2版本更新如下
 - 优化连接问题
- 1.1版本更新如下
  - 修复连接过多不关闭的问题
- 1.0版本更新如下
  - 增加配置文件动态加载
  - 修复首次请求机率无法响应的问题

## 服务端

- linux运行命令:

```shell
nohup java -jar linking-server.jar >logining.log &
```
- 自定义监听端口启动:

```shell
nohup java -jar linking-server.jar server.port=1234 >logining.log &
```


- 配置文件说明 application.properties

```shell
#服务端连接签名密钥
sign.key=2K2zFC3n6RBet1JtNb55t3CvRQwWrhz2
#服务端ip
server.host=47.10.19.9
#服务端口
server.port=8000
```

## 客户端

- linux运行命令:

```shell
nohup java -jar linking-client.jar >logining.log &
```
- 自定义服务器主机和端口启动:

```shell
nohup java -jar linking-client.jar server.host=192.20.30.12 server.port=1234 >logining.log &
```

- 配置说明

    1.将proxy.conf放到jar包所在目录,配置文件如有变动,会定时进行刷新监听
 
    2.proxy.conf格式如下
 ```shell
 [
 	{
 		remotePort:1022,
 		host:"127.0.0.1",
 		port:22
 	},
 	{
 		remotePort:1080,
 		host:"127.0.0.1",
 		port:80
 	}
 ]
 ```
- 参数说明

    remotePort 服务端监听端口
    host 客户端请求地址
    port 客户端请求端口


- 参考他人优秀作品
   https://github.com/xiaoger-liubu/Netty_Proxy.git
