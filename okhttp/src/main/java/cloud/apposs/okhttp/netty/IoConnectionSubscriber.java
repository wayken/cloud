package cloud.apposs.okhttp.netty;

import cloud.apposs.logger.Logger;
import cloud.apposs.okhttp.ChannelClosedException;
import cloud.apposs.okhttp.OkResponse;
import cloud.apposs.okhttp.pool.IPooledConnection;
import cloud.apposs.react.IoSubscriber;

/**
 * HTTP请求数据处理器，封装请求参数，每次请求都是一个新的处理器
 */
public class IoConnectionSubscriber implements IoSubscriber<OkResponse> {
    private final IoSubscriber<? super OkResponse> actual;

    private final NettyIoConnection connection;

    public IoConnectionSubscriber(IoSubscriber<? super OkResponse> actual, NettyIoConnection connection) {
        this.actual = actual;
        this.connection = connection;
    }

    @Override
    public void onNext(OkResponse value) throws Exception {
        // 在获取到所有响应数据后，如果是在连接池的则进行回收
        if (value.isCompleted()) {
            final IPooledConnection pool = connection.getPool();
            if (pool != null) {
                pool.release(connection);
            }
        }
        actual.onNext(value);
    }

    @Override
    public void onCompleted() {
        actual.onCompleted();
    }

    @Override
    public void onError(Throwable cause) {
        // 触发了异常，有可能是远程关闭了连接，同时从连接池中释放对应的资源
        connection.close();
        // 如果是远程关闭连接则不需要通知上层业务
        if (!(cause instanceof ChannelClosedException)) {
            actual.onError(cause);
        } else {
            Logger.debug(cause, "IoConnectionSubscriber socket " + connection.getRemoteAddress() + " closed");
        }
    }
}
