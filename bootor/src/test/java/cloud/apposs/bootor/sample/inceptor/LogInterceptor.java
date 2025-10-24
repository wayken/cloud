package cloud.apposs.bootor.sample.inceptor;

import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;
import cloud.apposs.bootor.interceptor.BooterInterceptorAdaptor;
import cloud.apposs.logger.Logger;
import cloud.apposs.react.IoEmitter;
import cloud.apposs.react.React;
import cloud.apposs.rest.Handler;

public class LogInterceptor extends BooterInterceptorAdaptor {
    private static final String LOG_INTERCEPTOR_ATTR = "_AttrLogInterceptor";

    @Override
    public React<Boolean> preHandle(BootorHttpRequest request, BootorHttpResponse response, Handler handler) throws Exception {
        return React.emitter(new IoEmitter<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Logger.info(Thread.currentThread() + " request1 log interceptor in:" + handler);
                request.setAttribute(LOG_INTERCEPTOR_ATTR, System.currentTimeMillis());
                if (handler.getPath().contains("pay5")) {
                    return false;
                }
                return true;
            }
        });
    }

    @Override
    public void afterCompletion(BootorHttpRequest request, BootorHttpResponse response, Handler handler, Object result, Throwable throwable) {
        Object time = request.getAttribute(LOG_INTERCEPTOR_ATTR);
        if (time == null) {
            return;
        }
        long startTime = (long) time;
        Logger.info(Thread.currentThread() + " request log interceptor finish:" + (System.currentTimeMillis() - startTime));
    }
}
