package cloud.apposs.webx.interceptor.limit;

public class LimitRateException extends Exception {
    private static final long serialVersionUID = 3186168042454956272L;

    private final String limitKey;

    public LimitRateException(String limitKey) {
        this.limitKey = limitKey;
    }
}
