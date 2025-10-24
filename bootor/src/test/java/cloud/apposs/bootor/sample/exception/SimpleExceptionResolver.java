package cloud.apposs.bootor.sample.exception;

import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;
import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.rest.WebExceptionResolver;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 简单异常拦截，仅打印异常堆栈到终端
 */
@Component
public class SimpleExceptionResolver implements WebExceptionResolver<BootorHttpRequest, BootorHttpResponse> {
    @Override
    public Object resolveHandlerException(BootorHttpRequest request, BootorHttpResponse response, Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}
