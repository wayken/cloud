package cloud.apposs.webx.interceptor.limit;

import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.webx.WebUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * 默认限流策略
 */
@Component
public class DefaultLimitRate implements ILimitRate {
    @Override
    public String getLimitKey(HttpServletRequest request) {
        return "IP:" + WebUtil.getRealIp(request);
    }
}
