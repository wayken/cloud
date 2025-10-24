package cloud.apposs.webx.management.netty;

import cloud.apposs.logger.Logger;
import cloud.apposs.rest.Restful;
import cloud.apposs.webx.WebXConfig;
import cloud.apposs.webx.management.ManagementContext;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.FutureListener;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;

public class NettyManagementContext extends ManagementContext {
    public static final String HTTP_REQUEST_DECODER = "httpDecoder";
    public static final String HTTP_ENCODER = "httpEncoder";
    public static final String HTTP_AGGREGATOR = "httpAggregator";
    public static final String HTTP_KEEPALIVE = "httpKeepAlive";
    public static final String HTTP_CHUNKED = "httpChunked";
    public static final String HTTP_SERVER = "httpServer";

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public NettyManagementContext(Restful<HttpServletRequest, HttpServletResponse> restful, WebXConfig config) throws Exception {
        super(restful, config);
    }

    @Override
    protected void handleRunApplication(WebXConfig config) throws Exception {
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup(1);
        Class<? extends ServerChannel> channelClass = NioServerSocketChannel.class;
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(channelClass)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
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
                        pipeline.addLast(HTTP_SERVER, new ApplicationHandler());
                    }
                });
        InetSocketAddress address = new InetSocketAddress(config.getManagementHost(), config.getManagementPort());
        bootstrap.bind(address).addListener((FutureListener<Void>) future -> {
            if (!future.isSuccess()) {
                throw new IOException(future.cause());
            }
        }).sync();
    }

    public class ApplicationHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        @Override
        protected void channelRead0(ChannelHandlerContext context, FullHttpRequest fullHttpRequest) throws Exception {
            NettyHttpRequest request = new NettyHttpRequest(context.channel().remoteAddress(), fullHttpRequest);
            NettyHttpResponse response = new NettyHttpResponse(context);
            // 解析请求参数
            NettyUtil.parseRequestParameter(fullHttpRequest, request, config);
            // 处理请求
            route(request, response);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
            Logger.error(cause, "Server Management Transport From %s Internal Error by %s,", context.channel().remoteAddress(), cause.getMessage());
            context.close();
        }
    }

    @Override
    protected void handleApplicationShutdown() {
        if (Objects.nonNull(bossGroup)) {
            bossGroup.shutdownGracefully().syncUninterruptibly();
        }
        if (Objects.nonNull(workerGroup)) {
            workerGroup.shutdownGracefully().syncUninterruptibly();
        }
    }
}
