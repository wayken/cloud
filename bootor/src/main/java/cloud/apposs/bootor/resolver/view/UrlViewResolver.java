package cloud.apposs.bootor.resolver.view;

import cloud.apposs.bootor.BootorConstants;
import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;
import cloud.apposs.bootor.WebUtil;
import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.rest.view.AbstractViewResolver;

/**
 * 页面Url跳转/转发视图渲染器
 */
@Component
public class UrlViewResolver extends AbstractViewResolver<BootorHttpRequest, BootorHttpResponse> {
    public static final String REQUEST_ATTRIBUTE_URL_CONTENT = "_AttrUrlContent";

    @Override
    public boolean supports(BootorHttpRequest request, BootorHttpResponse response, Object result) {
        if (result instanceof String) {
            String content = (String) result;
            if (content.startsWith(BootorConstants.REDIRECT_URL_PREFIX)) {
                // 页面重定向视图
                String urlRequest = content.substring(BootorConstants.REDIRECT_URL_PREFIX.length());
                request.setAttribute(REQUEST_ATTRIBUTE_URL_CONTENT, urlRequest);
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(BootorHttpRequest request, BootorHttpResponse response, Object result, boolean flush) throws Exception {
        String urlRequest = (String) request.getAttribute(REQUEST_ATTRIBUTE_URL_CONTENT);
        String url = createTargetUrl(urlRequest, request);
        WebUtil.sendRedirect301(response, url);
    }

    private String createTargetUrl(String url, BootorHttpRequest request) {
        StringBuilder targetUrl = new StringBuilder();
        if (url.startsWith("/")) {
            targetUrl.append(request.getUri().getPath());
        }
        targetUrl.append(url);
        return targetUrl.toString();
    }
}
