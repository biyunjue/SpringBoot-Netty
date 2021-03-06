package com.yunfy.demo.netty.server;


import com.yunfy.demo.netty.codec.PacketCodecHandler;
import com.yunfy.demo.netty.codec.Spliter;
import com.yunfy.demo.netty.handler.IMIdleStateHandler;
import com.yunfy.demo.netty.server.handler.AuthHandler;
import com.yunfy.demo.netty.server.handler.HeartBeatRequestHandler;
import com.yunfy.demo.netty.server.handler.IMHandler;
import com.yunfy.demo.netty.server.handler.LoginRequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Date;


/**
 * @author yunfy
 * @create 2018-10-13 17:00
 **/
public class NettyServer {
    private static final int PORT = 8000;

    public static void main(String[] args) {
        final ServerBootstrap serverBootstrap = new ServerBootstrap();

        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        serverBootstrap
                .group(boss, worker)
                .channel(NioServerSocketChannel.class)
                //表示系统用于临时存放已完成三次握手的请求的队列的最大长度，如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                .option(ChannelOption.SO_BACKLOG, 1024)
                //开启TCP底层心跳机制
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                //是否开启Nagle算法，true表示关闭，false表示开启
                //如果要求高实时性，有数据发送时就马上发送，就关闭，如果需要减少发送次数减少网络交互，就开启。
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel channel) {
                        // 空闲检测
                        channel.pipeline().addLast(new IMIdleStateHandler());
                        channel.pipeline().addLast(new Spliter());
                        channel.pipeline().addLast(PacketCodecHandler.INSTANCE);
                        channel.pipeline().addLast(LoginRequestHandler.INSTANCE);
                        channel.pipeline().addLast(HeartBeatRequestHandler.INSTANCE);
                        channel.pipeline().addLast(AuthHandler.INSTANCE);
                        channel.pipeline().addLast(IMHandler.INSTANCE);
                    }
                });

        bind(serverBootstrap, PORT);
    }

    private static void bind(final ServerBootstrap serverBootstrap, final int port) {
        serverBootstrap.bind(port).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println(new Date() + ": 端口[" + port + "]绑定成功!");
            } else {
                System.err.println("端口[" + port + "]绑定失败!");
                bind(serverBootstrap, port + 1);
            }
        });
    }
}
