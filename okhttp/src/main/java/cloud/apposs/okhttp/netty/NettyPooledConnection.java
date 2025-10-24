package cloud.apposs.okhttp.netty;

import cloud.apposs.okhttp.HttpBuilder;
import cloud.apposs.okhttp.pool.AbstractPooledConnection;
import cloud.apposs.okhttp.pool.ReactIoConnection;
import io.netty.channel.EventLoopGroup;

/**
 * 基于Netty框架的连接池实现
 */
public class NettyPooledConnection extends AbstractPooledConnection {
    private final EventLoopGroup loop;

    public NettyPooledConnection(int size, HttpBuilder builder, EventLoopGroup loop) {
        super(size, builder);
        this.loop = loop;
    }

    @Override
    protected ReactIoConnection handleCreateConnection() {
        return new NettyIoConnection(loop, this, builder);
    }

    @Override
    public void close() {
        loop.shutdownGracefully();
    }
}
