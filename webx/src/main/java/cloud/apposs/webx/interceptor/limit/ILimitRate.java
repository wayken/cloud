package cloud.apposs.webx.interceptor.limit;

import javax.servlet.http.HttpServletRequest;

/**
 * 限流策略定制类
 */
public interface ILimitRate {
    /**
     * 获取限流Key，可以是AID+IP等组合，默认是IP
     */
    String getLimitKey(HttpServletRequest request);
}
