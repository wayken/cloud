package cloud.apposs.okhttp.pool;

import cloud.apposs.okhttp.HttpBuilder;
import cloud.apposs.okhttp.OkRequest;
import cloud.apposs.okhttp.OkResponse;
import cloud.apposs.okhttp.RemoteSocketAddress;
import cloud.apposs.react.React;

import java.net.SocketAddress;

/**
 * HTTP 响应式异步IO连接，
 * 客户端发送请求前需要先获取连接，发送请求，最后释放连接回连接池
 */
public interface ReactIoConnection extends React.OnSubscribe<OkResponse> {
    /**
     * 获取连接池的Key，用于区分不同的连接池
     */
    PoolKey getPoolKey();

    /**
     * 获取远程目标地址
     */
    RemoteSocketAddress getRemoteAddress();

    /**
     * 获取请求对象
     */
    OkRequest getRequest();

    /**
     * 设置请求对象，
     * 因为请求对象在连接池中是共享的，而同一连接每次请求参数可能不同，因此需要在发送请求前设置请求对象
     */
    ReactIoConnection setRequest(OkRequest request);

    /**
     * 获取全局配置
     */
    HttpBuilder getBuilder();

    /**
     * 获取连接所属连接池
     */
    IPooledConnection getPool();

    /**
     * 释放响应式异步IO连接资源，一般是关闭底层Channel管道
     */
    void close();
}
