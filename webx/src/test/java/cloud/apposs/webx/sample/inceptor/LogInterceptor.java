package cloud.apposs.webx.sample.inceptor;

import cloud.apposs.logger.Logger;
import cloud.apposs.react.IoEmitter;
import cloud.apposs.react.React;
import cloud.apposs.rest.Handler;
import cloud.apposs.webx.interceptor.WebXInterceptorAdaptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LogInterceptor extends WebXInterceptorAdaptor {
    private static final String LOG_INTERCEPTOR_ATTR = "_AttrLogInterceptor";

    @Override
    public React<Boolean> preHandle(HttpServletRequest request, HttpServletResponse response, Handler handler) throws Exception {
        return React.emitter(new IoEmitter<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Logger.info("request log interceptor in:" + handler);
                request.setAttribute(LOG_INTERCEPTOR_ATTR, System.currentTimeMillis());
                return true;
            }
        });
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Handler handler, Object value, Throwable throwable) {
        if (throwable != null) {
            return;
        }
        Object time = request.getAttribute(LOG_INTERCEPTOR_ATTR);
        long startTime = (long) time;
        Logger.info("request log interceptor finish:" + (System.currentTimeMillis() - startTime));
    }
}
