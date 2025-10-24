package cloud.apposs.util;

import java.net.InetSocketAddress;

/**
 * 网络代理服务类
 */
public class Proxy {
    public enum Type {
        /**
         * HTTP协议代理
         */
        HTTP,
        /**
         * SOCKS5协议代理
         */
        SOCKS,
        /**
         * 基于服务发现的请求
         */
        SERVICE,
        /**
         * 请求转发代理，主要由网关服务反向代理使用
         */
        PROXYPASS,
        /**
         * 直接请求，不做代理
         */
        DIRECT,
        /**
         * 内网穿透代理
         */
        TUNNEL
    };

    private Type type;

    private InetSocketAddress address;

    public Proxy(Type type, InetSocketAddress sa) {
        this.type = type;
        this.address = sa;
    }

    public Type type() {
        return type;
    }

    public InetSocketAddress address() {
        return address;
    }

    public String toString() {
        return type() + " @ " + address();
    }

    public final boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Proxy))
            return false;
        Proxy p = (Proxy) obj;
        if (p.type() == type()) {
            if (address() == null) {
                return (p.address() == null);
            } else
                return address().equals(p.address());
        }
        return false;
    }

    public final int hashCode() {
        if (address() == null)
            return type().hashCode();
        return type().hashCode() + address().hashCode();
    }
}
