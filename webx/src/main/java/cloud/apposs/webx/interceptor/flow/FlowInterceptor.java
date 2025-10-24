package cloud.apposs.webx.interceptor.flow;

import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.react.IoEmitter;
import cloud.apposs.react.React;
import cloud.apposs.rest.Handler;
import cloud.apposs.util.Parser;
import cloud.apposs.util.StandardResult;
import cloud.apposs.webx.WebXConstants;
import cloud.apposs.webx.interceptor.WebXInterceptorAdaptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 每个请求链生成递增的流水号方便追踪排查
 */
@Component
public class FlowInterceptor extends WebXInterceptorAdaptor {
    /**
     * 每个请求的流水号生成器
     */
    private final AtomicLong flow = new AtomicLong(0);

    @Override
    public React<Boolean> preHandle(HttpServletRequest request, HttpServletResponse response, Handler handler) throws Exception {
        AtomicLong webFlow = this.flow;
        // 生成流水号
        // 判断网站是否有传递_flow流水号，
        // 设计此在于如果业务方需要调试时可自己自定义流水号来调试问题
        long flow = Parser.parseLong(request.getParameter(WebXConstants.REQUEST_PARAMETER_FLOW), -1);
        if (flow < 0) {
            if (webFlow.incrementAndGet() >= Long.MAX_VALUE) {
                webFlow.set(1);
            }
            flow = webFlow.get();
        }
        request.setAttribute(WebXConstants.REQUEST_PARAMETRIC_FLOW, flow);
        return React.emitter(new IoEmitter<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return true;
            }
        });
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Handler handler, Object result, Throwable throwable) {
        // 根据响应结果设置Errno错误码，便于日志拦截输出
        if (result instanceof StandardResult) {
            try {
                request.setAttribute(WebXConstants.REQUEST_ATTRIBUTE_ERRNO, ((StandardResult) result).getErrno().value());
            } catch (Exception ignore) {
            }
        }
    }
}
