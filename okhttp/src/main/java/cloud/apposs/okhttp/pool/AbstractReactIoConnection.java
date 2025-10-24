package cloud.apposs.okhttp.pool;

import cloud.apposs.okhttp.HttpBuilder;
import cloud.apposs.okhttp.OkRequest;
import cloud.apposs.okhttp.RemoteSocketAddress;

public abstract class AbstractReactIoConnection implements ReactIoConnection {
    protected OkRequest request;

    /** HTTP全局配置参数 */
    protected final HttpBuilder builder;

    /**
     * HTTP连接池，如果为空则代表连接不是从连接池获取的
     */
    protected final IPooledConnection pool;

    protected AbstractReactIoConnection(IPooledConnection pool, HttpBuilder builder) {
        this.pool = pool;
        this.builder = builder;
    }

    @Override
    public OkRequest getRequest() {
        return request;
    }

    @Override
    public ReactIoConnection setRequest(OkRequest request) {
        this.request = request;
        return this;
    }

    @Override
    public IPooledConnection getPool() {
        return pool;
    }

    @Override
    public PoolKey getPoolKey() {
        return PoolKey.build(request.remoteAddress(), request.getPoolKey());
    }

    @Override
    public RemoteSocketAddress getRemoteAddress() {
        return request.remoteAddress();
    }

    @Override
    public HttpBuilder getBuilder() {
        return builder;
    }
}
