package cloud.apposs.okhttp;

import cloud.apposs.util.Proxy;

import java.net.SocketAddress;
import java.util.Objects;

/**
 * 当前请求的远程地址，可能是初始远程地址，也可能是代理目标地址，
 * 如果是代理目标地址，则有以下几种代理模式：
 * <pre>
 * 1. HTTP代理远程地址
 * 2. SOCKS5代理远程地址
 * 3. 请求转发代理远程地址
 * 4. 直接请求到的远程地址
 * </pre>
 * 详见{@link Proxy.Type}
 */
public final class RemoteSocketAddress {
    /**
     * 当前采用的通讯协议，目前只支持HTTP/HTTPS
     */
    private final String schema;

    /** 当前代理模式 */
    private final Proxy.Type proxy;

    /** 转发远程地址 */
    private final SocketAddress remoteAddress;

    public RemoteSocketAddress(String schema, Proxy.Type proxy, SocketAddress remoteAddress) {
        this.schema = schema;
        this.proxy = proxy;
        this.remoteAddress = remoteAddress;
    }

    public String schema() {
        return schema;
    }

    public Proxy.Type proxy() {
        return proxy;
    }

    public SocketAddress address() {
        return remoteAddress;
    }

    public boolean isUseSsl() {
        return "https".equalsIgnoreCase(schema);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RemoteSocketAddress address = (RemoteSocketAddress) o;
        return Objects.equals(schema, address.schema)
                && proxy == address.proxy
                && Objects.equals(remoteAddress, address.remoteAddress);
    }

    @Override
    public String toString() {
        return "RemoteSocketAddress{" +
                "address=" + remoteAddress +
                ", proxy='" + proxy + '\'' +
                ", schema='" + schema + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(remoteAddress, schema);
    }

    public static RemoteSocketAddress build(String schema, Proxy.Type proxy, SocketAddress remoteAddress) {
        return new RemoteSocketAddress(schema, proxy, remoteAddress);
    }
}
