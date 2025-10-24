package cloud.apposs.okhttp;

import cloud.apposs.util.Proxy;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP请求包装器
 */
public class OkRequest {
    /**
     * HTTP请求URL
     */
    private String url;
    private URI uri;

    /**
     * 服务注册实例ID，若为空则不走反向代理负载均衡
     */
    private String serviceId;

    /**
     * 是否采用SSE请求，在SSE通讯场景下，数据是不断返回数据直接请求结束
     */
    private boolean sse = false;

    /**
     * 服务的请求KEY，Discovery负载均衡组件会通过此KEY来实现不同的负载均衡算法
     */
    private Object key;

    /**
     * 当前请求的代理模式
     */
    private Proxy.Type proxyMode = Proxy.Type.DIRECT;

    /**
     * 当前请求的远程地址，
     * 可能是初始远程地址，也可能是代理目标地址，详见{@link RemoteSocketAddress}
     */
    private RemoteSocketAddress remoteAddress = null;

    /**
     * HTTP请求方法
     */
    private OkMethod method = OkMethod.GET;

    /**
     * HTTP表单数据
     */
    private FormEntity formEntity;

    /**
     * 当前会话请求存储的一些状态值
     */
    private final Map<Object, Object> attributes = new HashMap<Object, Object>(1);

    /**
     * HTTP请求HEADERS数据
     */
    protected final Map<String, String> headers = new HashMap<String, String>();

    public static OkRequest builder() {
        return new OkRequest();
    }

    private OkRequest()  {
    }

    public String url() {
        return url;
    }

    public OkRequest url(String url) {
        this.url = url;
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        this.uri = URI.create(url);
        return this;
    }

    public URI uri() {
        return uri;
    }

    public String serviceId() {
        return serviceId;
    }

    public OkRequest serviceId(String serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    public boolean sse() {
        return sse;
    }

    public OkRequest sse(boolean sse) {
        this.sse = sse;
        return this;
    }

    public Object key() {
        return this.key;
    }

    public OkRequest key(Object key) {
        this.key = key;
        return this;
    }

    public OkMethod method() {
        return method;
    }

    public Proxy.Type proxyMode() {
        return this.proxyMode;
    }

    public OkRequest proxyMode(Proxy.Type proxyMode) {
        this.proxyMode = proxyMode;
        return this;
    }

    public RemoteSocketAddress remoteAddress() {
        return remoteAddress;
    }

    /**
     * 设置HTTP代理地址，由Discovery负载均衡组件调用
     */
    public void remoteAddress(RemoteSocketAddress proxyAddress) {
        this.remoteAddress = proxyAddress;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public OkRequest header(String key, String value) {
        headers.put(key, value);
        return this;
    }

    public String header(String key) {
        return headers.get(key);
    }

    public OkRequest headers(Map<String, String> headers) {
        headers.putAll(headers);
        return this;
    }

    public FormEntity formEntity() {
        return formEntity;
    }

    public OkRequest get(FormEntity formEntity) {
        return request(OkMethod.GET, formEntity);
    }

    public OkRequest put(FormEntity formEntity) {
        return request(OkMethod.PUT, formEntity);
    }

    public OkRequest post(FormEntity formEntity) {
        return request(OkMethod.POST, formEntity);
    }

    public OkRequest delete(FormEntity formEntity) {
        return request(OkMethod.DELETE, formEntity);
    }

    public OkRequest request(OkMethod method, FormEntity formEntity) {
        this.method = method;
        this.formEntity = formEntity;
        return this;
    }

    public Object getAttribute(Object key) {
        return getAttribute(key, null);
    }

    public Object getAttribute(Object key, Object defaultVal) {
        Object attr = attributes.get(key);
        if (attr == null && defaultVal != null) {
            attr = defaultVal;
            attributes.put(key, attr);
        }
        return attr;
    }

    public Map<Object, Object> getAttributes() {
        return attributes;
    }

    public Object setAttribute(Object key, Object value) {
        return attributes.put(key, value);
    }

    public boolean removeAttribute(Object key) {
        return attributes.remove(key) != null;
    }

    /**
     * 获取连接池的KEY，主要用于区分不同的连接池，详见{@link cloud.apposs.okhttp.pool.PoolKey}，
     * 之所以要区分不同的连接池是因为不同的服务可能会走不同的代理模式、服务发现ID、是否SSE等
     */
    public String getPoolKey() {
        return (serviceId == null ? "" : serviceId) + ":" + sse;
    }

    /**
     * 计算获取请求的远程地址
     */
    public static SocketAddress getRemoteAddress(OkRequest request) {
        URI url = request.uri();
        int port = url.getPort();
        if (port <= 0) {
            String schema = url.getScheme();
            if (schema.equalsIgnoreCase("http")) {
                port = 80;
            } else if (schema.equalsIgnoreCase("https")) {
                port = 443;
            }
        }
        String host = WebUtil.getHost(url);
        if (host == null) {
            throw new IllegalArgumentException(url + " hostname is null");
        }
        return new InetSocketAddress(host, port);
    }
}
