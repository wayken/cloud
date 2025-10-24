package cloud.apposs.webx.interceptor.session;

import cloud.apposs.util.Param;

import javax.servlet.http.HttpServletRequest;

public abstract class AbstractSessionHandler implements ISessionHandler {
    @Override
    public boolean isUrlPass(HttpServletRequest request) {
        return false;
    }

    @Override
    public boolean checkSessionValid(Param sessionInfo, HttpServletRequest request) {
        return true;
    }
}
