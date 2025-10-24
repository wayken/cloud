package cloud.apposs.bootor.listener.httplog.variable;

import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;
import cloud.apposs.rest.Handler;

/**
 * 异常解析，对应参数：$exp
 */
public class ExceptionVariable extends AbstractVariable {
    @Override
    public String parse(BootorHttpRequest request, BootorHttpResponse response, Handler handler, Throwable t) {
        if (t == null) {
            return "-";
        }
        return t.toString();
    }
}
