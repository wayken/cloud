package cloud.apposs.okhttp.netty;

import cloud.apposs.okhttp.HttpBuilder;
import cloud.apposs.okhttp.OkRequest;
import cloud.apposs.okhttp.OkResponse;
import cloud.apposs.okhttp.RemoteSocketAddress;
import cloud.apposs.okhttp.pool.AbstractReactIoConnection;
import cloud.apposs.okhttp.pool.IPooledConnection;
import cloud.apposs.react.SafeIoSubscriber;
import cloud.apposs.util.Proxy;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * 基于 Netty 的 HTTP 响应式异步IO连接，若开启连接池则归属连接池管理
 */
public class NettyIoConnection extends AbstractReactIoConnection {
    public static final String CONTEXT_SUBSCRIBE = "CONTEXT_SUBSCRIBE";
    public static final String CONTEXT_RESPONSE = "CONTEXT_RESPONSE";

    private final Bootstrap bootstrap;

    /**
     * 和远程服务建立连接句柄，
     * 如果为空则代表响应式连接第一次从连接池获取时，此时并未建立连接，
     * 需要在调用call方法时建立连接后对此字段进行赋值
     */
    private ChannelHandlerContext context;

    public NettyIoConnection(EventLoopGroup loop, IPooledConnection pool, HttpBuilder builder) {
        super(pool, builder);
        // 创建客户端引导
        // 初始化网络连接句柄
        Class<? extends SocketChannel> channelClass = null;
        if (builder.isUseLinuxEpoll()) {
            channelClass = EpollSocketChannel.class;
        } else {
            channelClass = NioSocketChannel.class;
        }
        this.bootstrap = new Bootstrap();
        this.bootstrap.group(loop)
                .channel(channelClass)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, builder.connectTimeout())
                .handler(new NettyChannelInitializer(builder, this));
    }

    @Override
    public void call(SafeIoSubscriber<? super OkResponse> t) throws Exception {
        IoConnectionSubscriber subscriber = new IoConnectionSubscriber(t, this);
        request.setAttribute(CONTEXT_SUBSCRIBE, subscriber);

        // 如果是首次从连接池获取连接则需要进行连接操作，否则直接发送请求数据
        if (context == null) {
            InetSocketAddress remoteAddress = null;
            RemoteSocketAddress proxyAddress = request.remoteAddress();
            Proxy.Type proxyType = proxyAddress.proxy();
            if (proxyType == Proxy.Type.SERVICE) {
                // 走服务发现模式会改造HOST为对应的ServiceID，需要改成获取服务发现后的远程地址
                remoteAddress = (InetSocketAddress) proxyAddress.address();
            } else {
                remoteAddress = (InetSocketAddress) OkRequest.getRemoteAddress(request);
            }
            bootstrap.connect(remoteAddress).addListener(future -> {
                if (!future.isSuccess()) {
                    t.onError(future.cause());
                }
            });
        } else {
            // 直接发送请求数据
            NettyUtil.sendRequest(this, context);
        }
    }

    @Override
    public void close() {
        if (context != null) {
            try {
                context.close();
            } catch (Exception e) {
                // ignore
            }
        }
        if (pool != null) {
            try {
                pool.remove(this);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    public void setContext(ChannelHandlerContext context) {
        this.context = context;
    }

    @Override
    public String toString() {
        return "NettyIoConnection{" +
                "Addr=" + (context == null ? "" : context.channel()) +
                '}';
    }
}
