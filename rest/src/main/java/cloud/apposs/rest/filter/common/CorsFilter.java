package cloud.apposs.rest.filter.common;

import cloud.apposs.rest.filter.IFilter;

/**
 * 抽象跨域请求处理过滤器，
 * 业务可继承该类实现自定义的跨域请求处理逻辑
 */
public abstract class CorsFilter<R, P> implements IFilter<R, P> {
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
    public boolean filter(R request, P response) throws Exception {
        if (!enable) {
            return true;
        }
        String requestHost = getRemoteHost(request);
        String requestOrigin = getHeader(request, "Origin");
        if (requestOrigin == null) {
            requestOrigin = "*";
        }
        if (allowOrigin.equals("*") || allowOrigin.contains(requestHost)) {
            putHeader(response, "Access-Control-Max-Age", "3600");
            putHeader(response, "Access-Control-Allow-Methods", allowMethods);
            putHeader(response, "Access-Control-Allow-Origin", requestOrigin);
            putHeader(response, "Access-Control-Allow-Credentials", "true");
            putHeader(response, "Access-Control-Allow-Headers", allowHeaders);
        }
        // 如果请求方法是OPTIONS则直接返回，不再继续执行后续的请求处理
        if ("OPTIONS".equalsIgnoreCase(getMethod(request))) {
            writeEmptyResponse(response);
            return false;
        }
        return true;
    }

    protected abstract String getRemoteHost(R request);

    protected abstract String getHeader(R request, String name);

    protected abstract String getMethod(R request);

    protected abstract void putHeader(P response, String name, String value);

    protected abstract void writeEmptyResponse(P response) throws Exception;
}
