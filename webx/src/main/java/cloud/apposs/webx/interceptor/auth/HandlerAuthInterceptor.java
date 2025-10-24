package cloud.apposs.webx.interceptor.auth;

import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.react.IoEmitter;
import cloud.apposs.react.React;
import cloud.apposs.rest.ApplicationContextHolder;
import cloud.apposs.rest.Handler;
import cloud.apposs.webx.interceptor.WebXInterceptorAdaptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 权限校验拦截器
 */
@Component
public class HandlerAuthInterceptor extends WebXInterceptorAdaptor {
    private IAuthentication authentication;

    @Override
    public void initialize(ApplicationContextHolder context) {
        this.authentication = context.getBeanHierarchy(IAuthentication.class);
    }

    @Override
    public React<Boolean> preHandle(HttpServletRequest request, HttpServletResponse response, Handler handler) throws Exception {
        return React.emitter(new IoEmitter<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                // 如果业务没有将实现的IAuthentication注入到框架中，则不做权限校验
                if (authentication == null) {
                    return true;
                }
                boolean isAuth = handler.getClazz().isAnnotationPresent(Auth.class) || handler.hasAnnotation(Auth.class);
                // Action没有配置Auth认证注解，直接跳过权限验证
                if (!isAuth) {
                    return true;
                }
                // 获取当前会话用户的权限列表，可通过HTTP请求权限列表或者通过配置文件读取
                // 注意当前方法是同步阻塞读取，为了提升性能，数据获取后可以存储一段时间缓存
                List<AuthPermission> authPermissionList = authentication.getAuthPermissionList(request);
                if (authPermissionList == null) {
                    throw new AuthenticationException();
                }
                for (AuthPermission permission : authPermissionList) {
                    if (authentication.isPermitted(request, permission)) {
                        return true;
                    }
                }
                throw new AuthenticationException();
            }
        });
    }
}
