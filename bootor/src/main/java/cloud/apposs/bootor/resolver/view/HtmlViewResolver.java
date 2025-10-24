package cloud.apposs.bootor.resolver.view;

import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;
import cloud.apposs.bootor.WebUtil;
import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.rest.annotation.Order;
import cloud.apposs.rest.view.AbstractViewResolver;
import cloud.apposs.util.MediaType;

/**
 * 页面Url跳转/转发视图渲染器
 */
@Component
@Order(Integer.MAX_VALUE)
public class HtmlViewResolver extends AbstractViewResolver<BootorHttpRequest, BootorHttpResponse> {
    /**
     * 如果没有其他视图匹配，默认该视图为处理所有业务逻辑返回结果的处理视图
     */
    @Override
    public boolean supports(BootorHttpRequest request, BootorHttpResponse response, Object result) {
        return true;
    }

    @Override
    public void render(BootorHttpRequest request, BootorHttpResponse response, Object result, boolean flush) throws Exception {
        WebUtil.response(response, MediaType.TEXT_HTML, config.getCharset(), result.toString(), flush);
    }
}
