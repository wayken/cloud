package cloud.apposs.webx.resolver.view;

import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.rest.view.AbstractViewResolver;
import cloud.apposs.util.SseEmitter;
import cloud.apposs.webx.WebUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;

/**
 * SSE服务器推送视图渲染器
 */
@Component
public class SSEViewResolver extends AbstractViewResolver<HttpServletRequest, HttpServletResponse> {
    @Override
    public boolean supports(HttpServletRequest request, HttpServletResponse response, Object result) {
        return (result instanceof SseEmitter);
    }

    @Override
    public boolean isCompleted(HttpServletRequest request, HttpServletResponse response, Object result) {
        SseEmitter emitter = (SseEmitter) result;
        return emitter.isDone();
    }

    @Override
    public void render(HttpServletRequest request, HttpServletResponse response, Object result, boolean flush) throws Exception {
        WebUtil.response(request, response, Charset.forName(config.getCharset()), (SseEmitter) result, flush);
    }
}
