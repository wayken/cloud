package cloud.apposs.webx.management;

import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.rest.WebExceptionResolver;
import cloud.apposs.util.Errno;
import cloud.apposs.util.StandardResult;

@Component
public class ManagementExceptionResolver implements WebExceptionResolver<WebXHttpRequest, WebXHttpResponse> {
    @Override
    public Object resolveHandlerException(WebXHttpRequest request, WebXHttpResponse response, Throwable throwable) {
        return StandardResult.error(Errno.ERROR);
    }
}
