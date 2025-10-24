package cloud.apposs.okhttp;

import cloud.apposs.discovery.IDiscovery;
import cloud.apposs.okhttp.netty.NettyEngine;
import cloud.apposs.util.CharsetUtil;

import java.nio.charset.Charset;

/**
 * HTTP请求编辑器，用于构建HTTP请求，全局配置
 */
public final class HttpBuilder {
    public static final String IO_MODE_NETTY = "netty";

    public static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    public static final int DEFAULT_SOCKET_TIMEOUT = 30 * 1000;
    public static final int DEFAULT_RETRY_COUNT = 3;
    public static final int DEFAULT_RETRY_SLEEP_TIME = 200;

    /**
     * 异步请求引擎组件
     */
    private OkEngine engine;

    /**
     * 服务发现组件
     */
    private IDiscovery discovery;

    /**
     * 底层网格模型，默认是采用NETTY
     */
    private String ioMode = IO_MODE_NETTY;

    /**
     * 异步IO轮询池数量
     */
    private int loopSize = Runtime.getRuntime().availableProcessors();

    /**
     * 是否采用Linux底层Epoll网络模型，针对底层为NETTY
     * Netty底层会通过Native方法为调用底层Epoll函数，可以提升性能，减少GC
     */
    private boolean useLinuxEpoll = false;

    /**
     * 请求连接超时时间，默认5S
     */
    private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;

    /**
     * 数据网络读写超时时间，默认60S
     */
    private int socketTimeout = DEFAULT_SOCKET_TIMEOUT;

    /**
     * HTTP连接池大小，<=0则不使用连接池
     */
    private int poolConnections = 0;

    /**
     * HTTP请求失败后的重试次数，为0则不重试
     */
    private int retryCount = DEFAULT_RETRY_COUNT;

    /**
     * HTTP请求失败后重试的休眠失败，避免雪崩
     */
    private int retrySleepTime = DEFAULT_RETRY_SLEEP_TIME;

    /**
     * HTTP请求/响应编码
     */
    private Charset charset = CharsetUtil.UTF_8;

    private final HttpInterceptorSupport interceptorSupport = new HttpInterceptorSupport();

    public static HttpBuilder builder() {
        return new HttpBuilder();
    }

    public OkEngine engine() {
        return engine;
    }

    public void engine(OkEngine engine) {
        this.engine = engine;
    }

    public IDiscovery discovery() {
        return discovery;
    }

    public HttpBuilder discovery(IDiscovery discovery) {
        this.discovery = discovery;
        return this;
    }

    /**
     * 建立异步HTTP请求服务
     */
    public OkHttp build() throws Exception {
        if (ioMode.equalsIgnoreCase(IO_MODE_NETTY)) {
            engine = new NettyEngine(this);
        } else {
            throw new IllegalArgumentException("Unsupported Http IO Engine Mode: " + ioMode);
        }
        return new OkHttp(this);
    }

    public String ioMode() {
        return ioMode;
    }

    public HttpBuilder ioMode(String ioMode) {
        this.ioMode = ioMode;
        return this;
    }

    public int loopSize() {
        return loopSize;
    }

    public HttpBuilder loopSize(int loopSize) {
        this.loopSize = loopSize;
        return this;
    }

    public boolean isUseLinuxEpoll() {
        return useLinuxEpoll;
    }

    public void setUseLinuxEpoll(boolean useLinuxEpoll) {
        this.useLinuxEpoll = useLinuxEpoll;
    }

    public int connectTimeout() {
        return this.connectTimeout;
    }

    public HttpBuilder connectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public int socketTimeout() {
        return this.socketTimeout;
    }

    public HttpBuilder socketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
        return this;
    }

    public int poolConnections() {
        return poolConnections;
    }

    public HttpBuilder poolConnections(int poolConnections) {
        this.poolConnections = poolConnections;
        return this;
    }

    public int retryCount() {
        return retryCount;
    }

    public HttpBuilder retryCount(int retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    public int retrySleepTime() {
        return retrySleepTime;
    }

    public HttpBuilder retrySleepTime(int retrySleepTime) {
        this.retrySleepTime = retrySleepTime;
        return this;
    }

    public Charset charset() {
        return charset;
    }

    public HttpBuilder charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public HttpInterceptorSupport getInterceptorSupport() {
        return interceptorSupport;
    }

    public void shutdown() {
        if (discovery != null) {
            discovery.shutdown();
        }
        if (engine != null) {
            engine.shutdown();
        }
    }
}
