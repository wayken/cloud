package cloud.apposs.bootor.resolver.view;

import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;
import cloud.apposs.bootor.WebUtil;
import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.util.SseEmitter;
import cloud.apposs.rest.view.AbstractViewResolver;

/**
 * SSE服务器推送视图渲染器
 */
@Component
public class SSEViewResolver extends AbstractViewResolver<BootorHttpRequest, BootorHttpResponse> {
    @Override
    public boolean supports(BootorHttpRequest request, BootorHttpResponse response, Object result) {
        return (result instanceof SseEmitter);
    }

    @Override
    public boolean isCompleted(BootorHttpRequest request, BootorHttpResponse response, Object result) {
        SseEmitter emitter = (SseEmitter) result;
        return emitter.isDone();
    }

    @Override
    public void render(BootorHttpRequest request, BootorHttpResponse response, Object result, boolean flush) throws Exception {
        WebUtil.response(response, config.getCharset(), (SseEmitter) result, flush);
    }
}
