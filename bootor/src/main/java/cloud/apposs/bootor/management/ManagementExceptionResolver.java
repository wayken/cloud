package cloud.apposs.bootor.management;

import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;
import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.rest.WebExceptionResolver;
import cloud.apposs.util.Errno;
import cloud.apposs.util.StandardResult;

@Component
public class ManagementExceptionResolver implements WebExceptionResolver<BootorHttpRequest, BootorHttpResponse> {
    @Override
    public Object resolveHandlerException(BootorHttpRequest request, BootorHttpResponse response, Throwable throwable) {
        return StandardResult.error(Errno.ERROR);
    }
}
