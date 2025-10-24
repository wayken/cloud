package cloud.apposs.bootor.management;

import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;
import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.rest.annotation.Order;
import cloud.apposs.rest.view.AbstractViewResolver;
import cloud.apposs.util.MediaType;

@Component
@Order(Integer.MAX_VALUE)
public class ManagementViewResolver extends AbstractViewResolver<BootorHttpRequest, BootorHttpResponse> {
    @Override
    public boolean supports(BootorHttpRequest request, BootorHttpResponse response, Object result) {
        return true;
    }

    @Override
    public void render(BootorHttpRequest request, BootorHttpResponse response, Object result, boolean flush) throws Exception {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + "; charset=" + config.getCharset());
        response.write(result.toString(), flush);
    }
}
