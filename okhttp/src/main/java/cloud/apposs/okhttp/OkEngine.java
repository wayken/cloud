package cloud.apposs.okhttp;

import cloud.apposs.discovery.IDiscovery;
import cloud.apposs.react.React;

/**
 * OKHttp IO引擎，由业务初始化并附带到OKHttp进行HTTP IO请求
 */
public interface OkEngine {
    /**
     * 创建响应式异步IO
     */
    React<OkResponse> create(OkRequest request, IDiscovery discovery) throws Exception;

    /**
     * 关闭IO引擎，用于资源释放，如关闭EventLoop等
     */
    void shutdown();
}
