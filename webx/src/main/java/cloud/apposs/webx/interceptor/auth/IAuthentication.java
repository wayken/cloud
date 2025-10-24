package cloud.apposs.webx.interceptor.auth;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface IAuthentication {
    /**
     * 加载获取所有的用户权限列表，
     * 可从配置文件加载，也可从数据库加载，也可以直接在代码定义好，
     * 执行时期：在系统启动时
     * @param request
     */
    List<AuthPermission> getAuthPermissionList(HttpServletRequest request);

    /**
     * 判断指定权限是否合法
     */
    boolean isPermitted(HttpServletRequest request, AuthPermission permission);
}
