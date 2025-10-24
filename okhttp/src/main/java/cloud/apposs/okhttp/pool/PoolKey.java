package cloud.apposs.okhttp.pool;

import cloud.apposs.okhttp.RemoteSocketAddress;

import java.util.Objects;

/**
 * 连接池的Key，将远程连接地址+自定义Key作为线程池唯一Key，用于区分不同的连接池
 */
public final class PoolKey {
    /** 远程地址，此作为第一层Key */
    private final RemoteSocketAddress holder;

    /**
     * 连接池Key，此作为第二层Key，
     * 之所以再加第二层Key，是因为客户端即使请求的是同一远程地址，但请求走的代理模式、请求协议、服务发现ID也会不一样，
     * 即远程地址服务同时提供HTTP、WebSocket、TCP协议等
     */
    private final String key;

    public PoolKey(RemoteSocketAddress holder, String key) {
        this.holder = holder;
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PoolKey poolKey = (PoolKey) o;
        return Objects.equals(holder, poolKey.holder) && Objects.equals(key, poolKey.key);
    }

    @Override
    public String toString() {
        return "PoolKey{" +
                "holder=" + holder +
                ", key='" + key + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(holder, key);
    }

    public static PoolKey build(RemoteSocketAddress holder, String key) {
        return new PoolKey(holder, key);
    }
}
