package cloud.apposs.bootor.filter.common;

import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;
import cloud.apposs.bootor.filter.IFilter;

/**
 * 默认跨域请求处理过滤器，
 * 业务可继承该类实现自定义的跨域请求处理逻辑，
 * 同时添加{@link cloud.apposs.ioc.annotation.Component} 注解交由Bootor框架管理
 */
public class CorsFilter implements IFilter {
    public boolean enable = true;
    private String allowOrigin = "*";
    private String allowMethods = "POST, GET, PUT, OPTIONS, DELETE";
    private String allowHeaders = "Origin, X-Requested-With, X-Auth-Token, Content-Type, Accept";

    public CorsFilter() {
    }

    public CorsFilter(boolean enable, String allowOrigin, String allowMethods, String allowHeaders) {
        this.enable = enable;
        this.allowOrigin = allowOrigin;
        this.allowMethods = allowMethods;
        this.allowHeaders = allowHeaders;
    }

    @Override
    public boolean filter(BootorHttpRequest request, BootorHttpResponse response) throws Exception {
        if (!enable) {
            return true;
        }
        String requestHost = request.getRemoteHost();
        String requestOrigin = request.getHeader("Origin");
        if (requestOrigin == null) {
            requestOrigin = "*";
        }
        if (allowOrigin.equals("*") || allowOrigin.contains(requestHost)) {
            response.putHeader("Access-Control-Max-Age", "3600");
            response.putHeader("Access-Control-Allow-Methods", allowMethods);
            response.putHeader("Access-Control-Allow-Origin", requestOrigin);
            response.putHeader("Access-Control-Allow-Credentials", "true");
            response.putHeader("Access-Control-Allow-Headers", allowHeaders);
        }
        // 如果请求方法是OPTIONS则直接返回，不再继续执行后续的请求处理
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.write("", true);
            return false;
        }
        return true;
    }
}
