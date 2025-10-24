package cloud.apposs.bootor.resolver.view;

import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;
import cloud.apposs.bootor.WebUtil;
import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.rest.view.AbstractViewResolver;
import cloud.apposs.util.MediaType;
import cloud.apposs.util.StandardResult;

/**
 * Json格式输出视图渲染器
 */
@Component
public class StandardResultViewResolver extends AbstractViewResolver<BootorHttpRequest, BootorHttpResponse> {
    @Override
    public boolean supports(BootorHttpRequest request, BootorHttpResponse response, Object result) {
        return (result instanceof StandardResult);
    }

    @Override
    public void render(BootorHttpRequest request, BootorHttpResponse response, Object result, boolean flush) throws Exception {
        WebUtil.response(response, MediaType.APPLICATION_JSON, config.getCharset(), ((StandardResult) result).toJson(), flush);
    }
}
