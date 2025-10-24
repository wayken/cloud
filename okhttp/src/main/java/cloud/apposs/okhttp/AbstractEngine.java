package cloud.apposs.okhttp;

import cloud.apposs.discovery.IDiscovery;
import cloud.apposs.logger.Logger;
import cloud.apposs.okhttp.pool.ReactIoConnection;
import cloud.apposs.react.IoFunction;
import cloud.apposs.react.React;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

public abstract class AbstractEngine implements OkEngine {
    protected final HttpBuilder builder;

    /**
     * 主要服务于是HTTP异步请求重试时的异步休眠
     */
    protected final ScheduledExecutorService scheduler;

    protected AbstractEngine(HttpBuilder builder) {
        this.builder = builder;
        if (builder.retryCount() > 0) {
            this.scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    return new Thread(runnable, "OkHttp-Scheduler");
                }
            });
        } else {
            this.scheduler = null;
        }
    }

    @Override
    public React<OkResponse> create(OkRequest request, IDiscovery discovery) throws Exception {
        // 请求前置拦截
        builder.getInterceptorSupport().preRequest(request);
        // 创建异步连接请求
        ReactIoConnection connection = doCreateConnection(request, discovery);
        // 如果没有配置重试则不用创建异步重试接口，否则创建
        if (builder.retryCount() <= 0) {
            return React.create(connection);
        } else {
            return React.create(connection).retry(new HttpRetry(request, discovery));
        }
    }

    protected abstract ReactIoConnection doCreateConnection(OkRequest request, IDiscovery discovery) throws Exception;

    private class HttpRetry implements IoFunction<Throwable, React<OkResponse>> {
        private final OkRequest request;

        private final IDiscovery discovery;

        private int current = 0;

        public HttpRetry(OkRequest request, IDiscovery discovery) {
            this.request = request;
            this.discovery = discovery;
        }
        @Override
        public React<OkResponse> call(Throwable throwable) throws Exception {
            // 超过最大重试次数，不再重试
            if (++current > builder.retryCount()) {
                return null;
            }
            int sleepTime = (int) (Math.random() * (current * builder.retrySleepTime()));
            String message = String.format("remote address '%s' transmission fail", request.uri());
            if (request.proxyMode() != null) {
                message += " with proxy " + request.remoteAddress();
            }
            String cause = throwable.getMessage();
            if (cause == null) {
                cause = throwable.toString();
            }
            message += " cause by: " + cause;
            Logger.warn("ok http %s, retry %d in %d milliseconds", message, current, sleepTime);
            ReactIoConnection connection = doCreateConnection(request, discovery);
            return React.create(connection).sleep(scheduler, sleepTime);
        }
    }
}
