package cloud.apposs.okhttp;

import cloud.apposs.discovery.IDiscovery;
import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.react.React;

/**
 * 客户端异步HTTP请求，
 * 主要对IoHttp的封装，增加了如下功能：
 * <pre>
 * 1、服务发现和故障转移，底层用Discovery组件
 * 2、IoHttp接口的统一封装，供业务开发使用
 * 3、实现代理转发服务
 * </pre>
 * 注意：组件内部维护了IOEngine引擎，其底层实现是基于EventLoop异步线程池轮询器，
 * 每个业务模块HTTP请求只对应一个OkHttp实例，即单例，不能多次创建实例
 */
@Component
public final class OkHttp {
    /**
     * 服务发现模块
     */
    private final HttpBuilder builder;

    public OkHttp(HttpBuilder builder) throws Exception {
        IDiscovery discovery = builder.discovery();
        if (discovery != null) {
            discovery.start();
        }
        this.builder = builder;
    }

    /**
     * HTTP GET异步请求，底层采用EventLoop
     */
    public React<OkResponse> execute(String url) throws Exception {
        return execute(OkRequest.builder().url(url));
    }

    public React<OkResponse> execute(OkRequest request) throws Exception {
        return execute(request, builder.discovery());
    }

    /**
     * HTTP异步请求，底层采用EventLoop
     */
    public React<OkResponse> execute(OkRequest request, IDiscovery discovery) throws Exception {
        OkEngine engine = builder.engine();
        return engine.create(request, discovery);
    }

    public void addInterceptor(IHttpInterceptor interceptor) {
        builder.getInterceptorSupport().addInterceptor(interceptor);
    }

    public void close() {
        builder.shutdown();
    }
}
