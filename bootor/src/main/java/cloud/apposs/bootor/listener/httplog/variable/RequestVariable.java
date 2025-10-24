package cloud.apposs.bootor.listener.httplog.variable;

import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;
import cloud.apposs.rest.Handler;

/**
 * 请求远程方法+URL，对应参数：$request
 */
public class RequestVariable extends AbstractVariable {
    @Override
    public String parse(BootorHttpRequest request, BootorHttpResponse response, Handler handler, Throwable t) {
        String method = request.getMethod().toUpperCase();
        String uri = request.getUri().getPath();
        return method + " " + uri;
    }
}
