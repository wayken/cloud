package cloud.apposs.bootor.management;

import cloud.apposs.bootor.*;
import cloud.apposs.rest.*;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import java.util.Map;

/**
 * 服务管理接口，负责提供健康检查、指标收集等功能
 */
public class ManagementRestful {
    private final BootorConfig config;

    protected Restful<BootorHttpRequest, BootorHttpResponse> restful;

    public ManagementRestful(ApplicationContext context, BootorConfig config) throws Exception {
        this.config = config;
        PrometheusMeterRegistry meterRegistry = context.getRestful().getBean(PrometheusMeterRegistry.class);
        RestConfig restConfig = handleInitRestConfig();
        this.restful = new Restful<>(restConfig);
        this.restful.getBeanFactory().addBean(meterRegistry);
        this.restful.initialize();
    }

    private RestConfig handleInitRestConfig() {
        RestConfig restConfig = new RestConfig();
        String basePackage = ManagementRestful.class.getPackage().getName();
        restConfig.setBasePackage(basePackage);
        restConfig.setContextPath(config.getManagementContextPath());
        restConfig.setCharset(config.getCharset());
        restConfig.setHttpLogEnable(false);
        restConfig.setWorkerCount(1);
        return restConfig;
    }

    public void route(BootorHttpRequest request, BootorHttpResponse response) {
        restful.renderView(new ManagementHandlerProcess(), request, response);
    }

    private class ManagementHandlerProcess implements IHandlerProcess<BootorHttpRequest, BootorHttpResponse> {
        @Override
        public String getRequestMethod(BootorHttpRequest request, BootorHttpResponse response) {
            return request.getMethod();
        }

        @Override
        public String getRequestPath(BootorHttpRequest request, BootorHttpResponse response) {
            return WebUtil.getRequestPath(request);
        }

        @Override
        public String getRequestHost(BootorHttpRequest request, BootorHttpResponse response) {
            return request.getRemoteHost();
        }

        @Override
        public void processVariable(BootorHttpRequest request, BootorHttpResponse response, Map<String, String> variables) {
            if (variables != null) {
                request.setAttribute(BootorConstants.REQUEST_ATTRIBUTE_VARIABLES, variables);
            }
        }

        @Override
        public void processHandler(BootorHttpRequest request, BootorHttpResponse response, Handler handler) {
            String produces = handler.getProduces();
            if (produces != null && !produces.isEmpty()) {
                response.setContentType(produces);
            }
        }

        @Override
        public IGuardProcess<BootorHttpRequest, BootorHttpResponse> getGuardProcess() {
            return null;
        }

        @Override
        public void markAsync(BootorHttpRequest request, BootorHttpResponse response) {
            // do nothing
        }
    }
}
