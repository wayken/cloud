package cloud.apposs.bootor.resolver.view;

import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;
import cloud.apposs.bootor.WebUtil;
import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.rest.view.AbstractViewResolver;
import cloud.apposs.util.MediaType;
import cloud.apposs.util.Table;

/**
 * Json格式输出视图渲染器
 */
@Component
public class JsonTableViewResolver extends AbstractViewResolver<BootorHttpRequest, BootorHttpResponse> {
    @Override
    public boolean supports(BootorHttpRequest request, BootorHttpResponse response, Object result) {
        return (result instanceof Table);
    }

    @Override
    public void render(BootorHttpRequest request, BootorHttpResponse response, Object result, boolean flush) throws Exception {
        WebUtil.response(response, MediaType.APPLICATION_JSON, config.getCharset(), ((Table) result).toJson(), flush);
    }
}
