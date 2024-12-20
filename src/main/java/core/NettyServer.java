package core;

import config.KuzkasConfig;
import handler.HttpServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;


public class NettyServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    private static final KuzkasConfig kuzkasConfig = KuzkasConfig.getInstance();

    public void httpServerStart() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
//                    p.addLast(new LoggingHandler(LogLevel.INFO));
                            p.addLast(new HttpServerCodec());
                            // netty默认http请求体最大为64k,这里设置为300m
                            p.addLast(new HttpObjectAggregator(300 * 1024 * 1024));
                            p.addLast(new HttpServerHandler());
                            p.addLast(new IdleStateHandler(60, 0, 0)); // 心跳检测
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = null;
            try {
                f = b.bind(kuzkasConfig.getPort()).sync();
            } catch (InterruptedException e) {
                logger.error("Kuzkas Server start error: ", e);
            }
            logger.info("Kuzkas Server started at port " + kuzkasConfig.getPort());
            try {
                f.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                logger.error("Kuzkas Server closed error: ", e);
            }
            logger.info("Kuzkas Server closed, bye");
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}

