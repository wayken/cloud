package cloud.apposs.bootor.filter;

import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;
import cloud.apposs.rest.filter.common.CorsFilter;

/**
 * 默认跨域请求处理过滤器，
 * 业务可继承该类实现自定义的跨域请求处理逻辑，
 * 同时添加{@link cloud.apposs.ioc.annotation.Component} 注解交由Bootor框架管理
 */
public class BootorCorsFilter extends CorsFilter<BootorHttpRequest, BootorHttpResponse> {
    public BootorCorsFilter() {
    }

    public BootorCorsFilter(boolean enable, String allowOrigin, String allowMethods, String allowHeaders) {
        super(enable, allowOrigin, allowMethods, allowHeaders);
    }

    @Override
    protected String getRemoteHost(BootorHttpRequest request) {
        return request.getRemoteHost();
    }

    @Override
    protected String getHeader(BootorHttpRequest request, String name) {
        return request.getHeader(name);
    }

    @Override
    protected String getMethod(BootorHttpRequest request) {
        return request.getMethod();
    }

    @Override
    protected void putHeader(BootorHttpResponse response, String name, String value) {
        response.putHeader(name, value);
    }

    @Override
    protected void writeEmptyResponse(BootorHttpResponse response) throws Exception {
        response.write("", true);
    }
}
