package cloud.apposs.bootor.listener.httplog.variable;

import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;
import cloud.apposs.rest.Handler;

/**
 * 请求响应状态码，对应参数：$status
 */
public class HttpStatusVariable extends AbstractVariable {
    @Override
    public String parse(BootorHttpRequest request, BootorHttpResponse response, Handler handler, Throwable t) {
        return String.valueOf(response.getStatus());
    }
}
