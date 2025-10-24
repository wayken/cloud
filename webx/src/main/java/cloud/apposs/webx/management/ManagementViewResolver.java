package cloud.apposs.webx.management;

import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.rest.annotation.Order;
import cloud.apposs.rest.view.AbstractViewResolver;
import cloud.apposs.util.MediaType;

@Component
@Order(Integer.MAX_VALUE)
public class ManagementViewResolver extends AbstractViewResolver<WebXHttpRequest, WebXHttpResponse> {
    @Override
    public boolean supports(WebXHttpRequest request, WebXHttpResponse response, Object result) {
        return true;
    }

    @Override
    public void render(WebXHttpRequest request, WebXHttpResponse response, Object result, boolean flush) throws Exception {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + "; charset=" + config.getCharset());
        response.write(result.toString(), flush);
    }
}
