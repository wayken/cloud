package cloud.apposs.okhttp.pool;

/**
 * HTTP连接池接口
 */
public interface IPooledConnection {
    /**
     * 获取连接池中的 HTTP 连接
     *
     * @param key 连接池Key，连接池以此作为标识建立 PoolKey->Queue<ReactIoConnection> 映射
     *      PoolKey 由远程地址+自定义Key组成(主要为OkRequest生成的Key)，用于区分不同的连接池
     */
    ReactIoConnection acquire(PoolKey key) throws Exception;

    /**
     * 释放连接池中的 HTTP 连接
     */
    void release(ReactIoConnection connection);

    /**
     * 关闭连接池
     */
    void close();

    /**
     * 移除连接池中的 HTTP 连接，一般在连接句柄出现异常时触发
     */
    void remove(ReactIoConnection connection);
}
