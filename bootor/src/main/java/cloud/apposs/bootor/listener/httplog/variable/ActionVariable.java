package cloud.apposs.bootor.listener.httplog.variable;

import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;
import cloud.apposs.rest.Handler;

/**
 * 请求远程主机，对应参数：$host
 */
public class ActionVariable extends AbstractVariable {
    @Override
    public String parse(BootorHttpRequest request, BootorHttpResponse response, Handler handler, Throwable t) {
        if (handler == null) {
            return "-";
        }
        return handler.getClazz().getSimpleName();
    }
}
