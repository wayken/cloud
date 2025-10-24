package cloud.apposs.okhttp.netty;

import cloud.apposs.okhttp.HttpBuilder;
import cloud.apposs.okhttp.RemoteSocketAddress;
import cloud.apposs.okhttp.pool.ReactIoConnection;
import cloud.apposs.util.Proxy;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Netty Channel初始化器，用于初始化Channel的Handler链
 */
public class NettyChannelInitializer extends ChannelInitializer<Channel> {
    private final HttpBuilder builder;

    private final ReactIoConnection connection;

    /**
     * 请求数据处理器，
     * 每次请求链接参数都不一样，所以该处理器在每次请求均会重新设置Connection参数
     */
    private final HttpClientProcessor processor;

    public NettyChannelInitializer(HttpBuilder builder, ReactIoConnection connection) {
        this.builder = builder;
        this.connection = connection;
        this.processor = new HttpClientProcessor((NettyIoConnection) connection, builder.poolConnections() > 0);
    }

    protected void initChannel(Channel channel) throws Exception {
        RemoteSocketAddress address = connection.getRemoteAddress();
        Proxy.Type proxyType = address.proxy();
        // 判断是否是改成走代理，如果是则根据代理类型生成不同的代理Handler并添加到Pipeline中
        if (proxyType == Proxy.Type.SOCKS) {
            // 使用SOCKS5代理
            ProxyHandler proxyHandler = new Socks5ProxyHandler(address.address(), true);
            channel.pipeline().addLast(proxyHandler);
        } else if (proxyType == Proxy.Type.HTTP) {
            // 使用HTTP代理
            ProxyHandler proxyHandler = new HttpProxyHandler(address.address());
            channel.pipeline().addLast(proxyHandler);
        }
        // 判断是否是改HTTPS
        if (address.isUseSsl()) {
            URI request = connection.getRequest().uri();
            String host = request.getHost();
            int port = request.getPort() == -1 ? 443 : request.getPort();
            SslContext sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            channel.pipeline().addLast(sslContext.newHandler(channel.alloc(), host, port));
        }
        // HTTP解码链
        channel.pipeline().addLast(new ReadTimeoutHandler(builder.socketTimeout(), TimeUnit.MILLISECONDS));
        channel.pipeline().addLast(new HttpClientCodec());
        // 在SSE开启的情况就不要注册聚合器，因为聚合器是聚合成一个 FullHttpResponse 再下发到 channelRead，SSE场景下就无法持续触发数据接收
        if (!connection.getRequest().sse()) {
            channel.pipeline().addLast(new HttpObjectAggregator(655360));
        }
        channel.pipeline().addLast(processor);
    }

    public HttpClientProcessor getProcessor() {
        return processor;
    }
}
