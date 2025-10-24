package cloud.apposs.bootor.netty;

import cloud.apposs.bootor.ApplicationContext;
import cloud.apposs.bootor.BootorConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerKeepAliveHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.FutureListener;

import java.io.IOException;
import java.net.InetSocketAddress;

public final class NettyApplicationContext extends ApplicationContext {
    public static final String HTTP_REQUEST_DECODER = "httpDecoder";
    public static final String HTTP_ENCODER = "httpEncoder";
    public static final String HTTP_AGGREGATOR = "httpAggregator";
    public static final String HTTP_KEEPALIVE = "httpKeepAlive";
    public static final String HTTP_CHUNKED = "httpChunked";
    public static final String HTTP_SERVER = "httpServer";

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public NettyApplicationContext() {
        super(new BootorConfig());
    }

    public NettyApplicationContext(BootorConfig config) {
        super(config);
    }

    @Override
    protected void handleStartHttpServer(BootorConfig config) throws Exception {
        Class<? extends ServerChannel> channelClass = null;
        if (config.isUseLinuxEpoll()) {
            this.bossGroup = new EpollEventLoopGroup(config.getNumOfGroup());
            this.workerGroup = new EpollEventLoopGroup(config.getWorkerCount());
            channelClass = EpollServerSocketChannel.class;
        } else {
            this.bossGroup = new NioEventLoopGroup(config.getNumOfGroup());
            this.workerGroup = new NioEventLoopGroup(config.getWorkerCount());
            channelClass = NioServerSocketChannel.class;
        }
        handleHttpServerInitialize(channelClass);
        if (config.isManagementEnable()) {
            handleManagementServerInitialize(channelClass);
        }
    }

    // 启动HTTP接口服务
    private void handleHttpServerInitialize(Class<? extends ServerChannel> channelClass) throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap();
        handleBootstrapInitialize(bootstrap, channelClass);
        InetSocketAddress address = new InetSocketAddress(config.getHost(), config.getPort());
        bootstrap.bind(address).addListener((FutureListener<Void>) future -> {
            if (!future.isSuccess()) {
                throw new IOException(future.cause());
            }
        }).sync();
    }

    // 启动管理服务
    private void handleManagementServerInitialize(Class<? extends ServerChannel> channelClass) throws Exception {
        NettyManagementContext managementContext = new NettyManagementContext(this, config);
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(channelClass)
                .option(ChannelOption.SO_BACKLOG, config.getBacklog())
                .option(ChannelOption.SO_REUSEADDR, config.isReuseAddress())
                .childOption(ChannelOption.TCP_NODELAY, config.isTcpNoDelay())
                .childOption(ChannelOption.SO_KEEPALIVE,false)
                .childOption(ChannelOption.SO_REUSEADDR,true)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(HTTP_REQUEST_DECODER, new HttpRequestDecoder());
                        pipeline.addLast(HTTP_ENCODER, new HttpResponseEncoder());
                        pipeline.addLast(HTTP_AGGREGATOR, new HttpObjectAggregator(65535));
                        pipeline.addLast(HTTP_CHUNKED, new ChunkedWriteHandler());
                        pipeline.addLast(HTTP_SERVER, managementContext.newApplicationHandler());
                    }
                });
        InetSocketAddress address = new InetSocketAddress(config.getManagementHost(), config.getManagementPort());
        bootstrap.bind(address).addListener((FutureListener<Void>) future -> {
            if (!future.isSuccess()) {
                throw new IOException(future.cause());
            }
        }).sync();
    }

    private ServerBootstrap handleBootstrapInitialize(ServerBootstrap bootstrap, Class<? extends ServerChannel> channelClass) {
        bootstrap.group(bossGroup, workerGroup)
                .channel(channelClass)
                .option(ChannelOption.SO_BACKLOG, config.getBacklog())
                .option(ChannelOption.SO_REUSEADDR, config.isReuseAddress())
                .childOption(ChannelOption.TCP_NODELAY, config.isTcpNoDelay())
                .childOption(ChannelOption.SO_KEEPALIVE,false)
                .childOption(ChannelOption.SO_REUSEADDR,true)
                .childHandler(new ChannelHandlerInitializer());
        return bootstrap;
    }

    private class ChannelHandlerInitializer extends ChannelInitializer<Channel> {
        @Override
        protected void initChannel(Channel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            // Server端接收到的是HttpRequest，所以要使用HttpRequestDecoder进行解码
            pipeline.addLast(HTTP_REQUEST_DECODER, new HttpRequestDecoder());
            // Server端发送的是httpResponse，所以要使用HttpResponseEncoder进行编码
            pipeline.addLast(HTTP_ENCODER, new HttpResponseEncoder());
            // 将多个消息转换为单一的BootorHttpRequest或FullHttpResponse对象，配置最大数据包大小
            pipeline.addLast(HTTP_AGGREGATOR, new HttpObjectAggregator(
                    config.getMaxFileSize() > 0 ? (int) config.getMaxFileSize() : Integer.MAX_VALUE
            ));
            // 添加keepalive支持
            pipeline.addLast(HTTP_KEEPALIVE, new HttpServerKeepAliveHandler());
            // 解决大数据包传输问题，用于支持异步写大量数据流并且不需要消耗大量内存也不会导致内存溢出错误( OutOfMemoryError )。
            // 仅支持ChunkedInput类型的消息。也就是说，仅当消息类型是ChunkedInput时才能实现ChunkedWriteHandler提供的大数据包传输功能
            pipeline.addLast(HTTP_CHUNKED, new ChunkedWriteHandler());
            pipeline.addLast(HTTP_SERVER, new ApplicationHandler(config, NettyApplicationContext.this));
        }
    }

    @Override
    protected void handleCloseHttpServer() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully().syncUninterruptibly();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully().syncUninterruptibly();
        }
    }
}
