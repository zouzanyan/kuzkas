package core;

import config.KuzkasConfig;
import handler.HttpServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final KuzkasConfig kuzkasConfig = KuzkasConfig.getInstance();

    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                    ChannelPipeline p = ch.pipeline();
//                    p.addLast(new LoggingHandler(LogLevel.WARN));
                    p.addLast(new HttpRequestDecoder());
                    p.addLast(new HttpObjectAggregator(65536));
                    p.addLast(new HttpResponseEncoder());
                    p.addLast(new HttpServerHandler());
                    p.addLast(new IdleStateHandler(60, 0, 0)); // 心跳检测
                }
            }).option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = b.bind(kuzkasConfig.getPort()).sync();
            logger.info("Kuzkas Server started at port " + kuzkasConfig.getPort());
            f.channel().closeFuture().sync();
            logger.info("Kuzkas Server closed, bye");
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}

