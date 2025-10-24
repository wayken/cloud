package cloud.apposs.webx.management;

import cloud.apposs.rest.*;
import cloud.apposs.webx.WebXConfig;
import cloud.apposs.webx.WebXConstants;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ManagementContext {
    protected final WebXConfig config;

    protected Restful<WebXHttpRequest, WebXHttpResponse> restful;

    protected AtomicBoolean shutdown = new AtomicBoolean(false);

    public ManagementContext(Restful<HttpServletRequest, HttpServletResponse> restful, WebXConfig config) throws Exception {
        this.config = config;
        PrometheusMeterRegistry meterRegistry = restful.getBean(PrometheusMeterRegistry.class);
        RestConfig restConfig = handleInitRestConfig();
        this.restful = new Restful<>(restConfig);
        this.restful.getBeanFactory().addBean(meterRegistry);
        this.restful.initialize();
    }

    public ManagementContext startup() throws Exception {
        handleRunApplication(config);
        handleShutdownHookRegister();
        return this;
    }

    /** 启动HTTP服务，由网络内核服务（如Netty/Undertow）根据自身服务特点启动 */
    protected abstract void handleRunApplication(WebXConfig config) throws Exception;

    /** 关闭服务，释放资源 */
    protected abstract void handleApplicationShutdown();

    private RestConfig handleInitRestConfig() {
        RestConfig restConfig = new RestConfig();
        String basePackage = ManagementContext.class.getPackage().getName();
        restConfig.setBasePackage(basePackage);
        restConfig.setContextPath(config.getManagementContextPath());
        restConfig.setCharset(config.getCharset());
        restConfig.setHttpLogEnable(false);
        restConfig.setWorkerCount(1);
        return restConfig;
    }

    public void route(WebXHttpRequest request, WebXHttpResponse response) {
        restful.renderView(new ManagementHandlerProcess(), request, response);
    }

    private void handleShutdownHookRegister() {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                shutdown();
            }
        });
    }

    public void shutdown() {
        if (shutdown.getAndSet(true)) {
            return;
        }
        if (restful != null) {
            restful.destroy();
        }
        handleApplicationShutdown();
    }

    private class ManagementHandlerProcess implements IHandlerProcess<WebXHttpRequest, WebXHttpResponse> {
        @Override
        public String getRequestMethod(WebXHttpRequest request, WebXHttpResponse response) {
            return request.getMethod();
        }

        @Override
        public String getRequestPath(WebXHttpRequest request, WebXHttpResponse response) {
            return request.getUri().getPath();
        }

        @Override
        public String getRequestHost(WebXHttpRequest request, WebXHttpResponse response) {
            return request.getRemoteHost();
        }

        @Override
        public void processVariable(WebXHttpRequest request, WebXHttpResponse response, Map<String, String> variables) {
            if (variables != null) {
                request.setAttribute(WebXConstants.REQUEST_ATTRIBUTE_VARIABLES, variables);
            }
        }

        @Override
        public void processHandler(WebXHttpRequest request, WebXHttpResponse response, Handler handler) {
            String produces = handler.getProduces();
            if (produces != null && !produces.isEmpty()) {
                response.setContentType(produces);
            }
        }

        @Override
        public IGuardProcess<WebXHttpRequest, WebXHttpResponse> getGuardProcess() {
            return null;
        }

        @Override
        public void markAsync(WebXHttpRequest request, WebXHttpResponse response) {
            // do nothing
        }
    }
}
