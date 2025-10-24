package cloud.apposs.okhttp.netty;

import cloud.apposs.discovery.IDiscovery;
import cloud.apposs.okhttp.*;
import cloud.apposs.okhttp.pool.IPooledConnection;
import cloud.apposs.okhttp.pool.PoolKey;
import cloud.apposs.okhttp.pool.ReactIoConnection;
import cloud.apposs.util.Proxy;
import cloud.apposs.util.StrUtil;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public class NettyEngine extends AbstractEngine {
    private final EventLoopGroup loop;

    /**
     * HTTP连接池，为空则不使用连接池
     */
    private final IPooledConnection pool;

    public NettyEngine(HttpBuilder builder) {
        super(builder);
        if (builder.isUseLinuxEpoll()) {
            this.loop = new EpollEventLoopGroup(builder.loopSize());
        } else {
            this.loop = new NioEventLoopGroup(builder.loopSize());
        }
        if (builder.poolConnections() > 0) {
            this.pool = new NettyPooledConnection(builder.poolConnections(), builder, loop);
        } else {
            this.pool = null;
        }
    }

    /**
     * 创建HTTP请求连接，
     * 如果连接池不为空则从连接池中获取连接，否则创建新的连接
     */
    @Override
    protected ReactIoConnection doCreateConnection(OkRequest request, IDiscovery discovery) throws Exception {
        // 创建请求客户端
        RemoteSocketAddress remoteAddress = null;
        // 获取连接前需要先通过服务发现获取真正的远程服务地址，方便后续连接池以此远程地址作为Key复用连接
        String serviceId = request.serviceId();
        if (discovery != null && !StrUtil.isEmpty(serviceId)) {
            RemoteSocketAddress remoteIntance = Discovery.chooseInstance(discovery, request);
            if (remoteIntance != null) {
                remoteAddress = remoteIntance;
            }
        } else {
            remoteAddress = RemoteSocketAddress.build(
                    request.uri().getScheme(), Proxy.Type.DIRECT, OkRequest.getRemoteAddress(request));
        }
        request.remoteAddress(remoteAddress);
        // 如果连接池不为空则从连接池中获取连接，否则创建新的连接
        ReactIoConnection connection = null;
        if (pool != null) {
            PoolKey key = PoolKey.build(remoteAddress, request.getPoolKey());
            connection = pool.acquire(key).setRequest(request);
        } else {
            connection = new NettyIoConnection(loop, pool, builder).setRequest(request);
        }
        return connection;
    }

    @Override
    public void shutdown() {
        loop.shutdownGracefully().syncUninterruptibly();
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }
}
