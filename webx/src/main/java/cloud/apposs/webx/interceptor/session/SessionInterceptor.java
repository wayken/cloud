package cloud.apposs.webx.interceptor.session;

import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.react.IoEmitter;
import cloud.apposs.react.React;
import cloud.apposs.rest.ApplicationContextHolder;
import cloud.apposs.rest.Handler;
import cloud.apposs.util.Param;
import cloud.apposs.util.StrUtil;
import cloud.apposs.webx.WebXConstants;
import cloud.apposs.webx.interceptor.WebXInterceptorAdaptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户会话登录校验拦截器，如果业务有实现{@link ISessionHandler}接口则默认开启拦截
 */
@Component
public class SessionInterceptor extends WebXInterceptorAdaptor {
    private ISessionHandler sessionHandler;

    @Override
    public void initialize(ApplicationContextHolder context) {
        sessionHandler = context.getBeanHierarchy(ISessionHandler.class);
    }

    @Override
    public React<Boolean> preHandle(HttpServletRequest request, HttpServletResponse response, Handler handler) throws Exception {
        return React.emitter(new IoEmitter<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                // 如果业务没有将实现的ISessionHandler注入到框架中，则不做会话校验
                if (sessionHandler == null) {
                    return true;
                }
                // 判断该URL请求是否需要进行会话校验
                if (sessionHandler.isUrlPass(request)) {
                    return true;
                }
                // 获取传递的会话ID，可以从Cookie获取，也可以通过表单参数获取，视业务自己实现
                String sessionId = sessionHandler.getSessionId(request);
                if (StrUtil.isEmpty(sessionId)) {
                    return false;
                }
                // 获取用户的登录信息
                Param sessionInfo = sessionHandler.getSessionInfo(sessionId, request);
                if (sessionInfo == null || sessionInfo.isEmpty()) {
                    return false;
                }
                // 校验用户会话
                if (!sessionHandler.checkSessionValid(sessionInfo, request)) {
                    return false;
                }
                // 保存用户会话登录信息到当前请求中，后续ModelParameter对象可以获取用户登录的AID信息
                // 注意：将AID打进去的业务如果是属于登录的一定要有此类来拦截是否有登录，避免前端直接传递AID进来
                long aid = sessionInfo.getLong(WebXConstants.REQUEST_PARAMETRIC_AID, -1L);
                if (aid > 0) {
                    request.setAttribute(WebXConstants.REQUEST_PARAMETRIC_AID, aid);
                }
                return true;
            }
        });
    }
}
