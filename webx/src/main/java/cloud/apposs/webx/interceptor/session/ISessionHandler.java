package cloud.apposs.webx.interceptor.session;

import cloud.apposs.util.Param;

import javax.servlet.http.HttpServletRequest;

/**
 * 会议拦截接口实现
 * 各业务需要实现接口流程说明如下：
 * <pre>
 * 1. 通过{@link #isUrlPass(HttpServletRequest)}判断该请求URL是否需要做会话校验
 * 2. 通过{@link #getSessionId(HttpServletRequest)}获取前端传递过来的SessionId，可能在Cookie，可能在请求URL，也可能在HTTP Body，业务自己关心
 * 3. 通过{@link #getSessionInfo(String, HttpServletRequest)}获取会话信息，一般是通过token从SessionSvr来获取对应的用户会话信息
 * 4. 通过{@link #checkSessionValid(Param, HttpServletRequest)}校验上面方法的会话信息是否与当前场景合法，例如登录IP是否合法等
 * 5. 将会话信息中的AID打进ModelParameter对象中
 * </pre>
 * 详细流程参考{@link SessionInterceptor}
 */
public interface ISessionHandler {
    /**
     * 判断该请求的URL是否可放行，像登录、注册功能本身就不需要会话校验，
     * 默认为所有请求都要进行会话校验
     */
    boolean isUrlPass(HttpServletRequest request);

    /**
     * 获取前端传递的登录会话ID，可以从Cookie获取，也可以从表单传递参数中获取
     */
    String getSessionId(HttpServletRequest request);

    /**
     * 获取用户登录会话信息
     */
    Param getSessionInfo(String sessionId, HttpServletRequest request);

    /**
     * 判断该会话是否合法，默认可以直接返回true，因为在{@link #getSessionInfo(String, HttpServletRequest)}中就已经判断是否有会话，
     * 主要应用场景是在于当业务需要判断用户登录IP是否也合法时用到
     */
    boolean checkSessionValid(Param sessionInfo, HttpServletRequest request);
}
