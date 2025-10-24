package cloud.apposs.okhttp;

import cloud.apposs.discovery.IDiscovery;
import cloud.apposs.registry.ServiceInstance;
import cloud.apposs.util.Proxy;
import cloud.apposs.util.StrUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public final class Discovery {
    /**
     * 通过负载均衡选择一个服务实例用于请求
     * 需要远程服务代理转发有可能有以下情况：
     * <pre>
     *     1. 通过Socks5代理访问远程服务
     *     2. 通过Http代理访问远程服务
     *     3. 通过代理转发访问远程服务
     * </pre>
     *
     * @param  discovery 服务发现
     * @param  request   请求参数
     * @return 返回服务实例地址，如果不走服务发现或者没有可用的服务实例则返回null
     */
    public static RemoteSocketAddress chooseInstance(IDiscovery discovery, OkRequest request) throws IOException {
        String serviceId = request.serviceId();
        if (discovery == null || StrUtil.isEmpty(serviceId)) {
            return null;
        }
        Object key = request.key();
        ServiceInstance instance = discovery.choose(serviceId, key);
        if (instance == null) {
            throw new IOException("no avaiable service '" + serviceId + "' of uri '"+ request.uri() + "'");
        }
        String serviceHost = instance.getHost();
        int servicePort = instance.getPort();
        String schema = instance.getMetadata().getString(Metadata.SCHEMA);
        SocketAddress address = new InetSocketAddress(serviceHost, servicePort);
        Proxy.Type proxy = request.proxyMode();
        return RemoteSocketAddress.build(schema, proxy, address);
    }
}
