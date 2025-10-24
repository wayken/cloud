package cloud.apposs.bootor.listener.httplog.variable;

import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;
import cloud.apposs.rest.Handler;
import cloud.apposs.rest.annotation.Request;

/**
 * 请求远程方法，对应参数：$method
 */
public class RequestMethodVariable extends AbstractVariable {
    @Override
    public String parse(BootorHttpRequest request, BootorHttpResponse response, Handler handler, Throwable t) {
        if (handler != null && handler.hasAnnotation(Request.Read.class)) {
            return "READ";
        } else if (handler != null && handler.hasAnnotation(Request.Post.class)) {
            return "POST";
        }
        return request.getMethod().toUpperCase();
    }
}
