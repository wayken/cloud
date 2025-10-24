package cloud.apposs.bootor.interceptor.flow;

import cloud.apposs.bootor.BootorConstants;
import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;
import cloud.apposs.bootor.interceptor.BooterInterceptorAdaptor;
import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.react.IoEmitter;
import cloud.apposs.react.React;
import cloud.apposs.rest.Handler;
import cloud.apposs.util.Parser;
import cloud.apposs.util.StandardResult;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class FlowInterceptor extends BooterInterceptorAdaptor {
    /**
     * 每个请求的流水号生成器
     */
    private final AtomicLong flow = new AtomicLong(0);

    @Override
    public React<Boolean> preHandle(BootorHttpRequest request, BootorHttpResponse response, Handler handler) throws Exception {
        AtomicLong webFlow = this.flow;
        // 生成流水号
        // 判断网站是否有传递_flow流水号，
        // 设计此在于如果业务方需要调试时可自己自定义流水号来调试问题
        Object flowValue = request.getParameter(BootorConstants.REQUEST_PARAMETER_FLOW);
        if (flowValue == null) {
            flowValue = request.getParam().get(BootorConstants.REQUEST_PARAMETER_FLOW);
        }
        long flow = flowValue != null ? Parser.parseLong(flowValue.toString(), -1) : -1;
        if (flow < 0) {
            if (webFlow.incrementAndGet() >= Long.MAX_VALUE) {
                webFlow.set(1);
            }
            flow = webFlow.get();
        }
        request.setAttribute(BootorConstants.REQUEST_PARAMETRIC_FLOW, flow);
        return React.emitter(new IoEmitter<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return true;
            }
        });
    }

    @Override
    public void afterCompletion(BootorHttpRequest request, BootorHttpResponse response,
                                Handler handler, Object result, Throwable throwable) {
        // 根据响应结果设置Errno错误码，便于日志拦截输出
        if (result instanceof StandardResult) {
            request.setAttribute(BootorConstants.REQUEST_ATTRIBUTE_ERRNO, ((StandardResult) result).getErrno().value());
        }
    }
}
