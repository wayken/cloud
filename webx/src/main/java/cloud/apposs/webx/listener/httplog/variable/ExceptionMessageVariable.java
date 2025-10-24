package cloud.apposs.webx.listener.httplog.variable;

import cloud.apposs.rest.Handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 异常消息解析，对应参数：$expm
 */
public class ExceptionMessageVariable extends AbstractVariable {
    @Override
    public String parse(HttpServletRequest request, HttpServletResponse response, Handler handler, Throwable t) {
        if (t == null) {
            return "-";
        }
        return t.getMessage();
    }
}
