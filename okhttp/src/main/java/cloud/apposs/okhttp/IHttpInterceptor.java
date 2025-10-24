package cloud.apposs.okhttp;

public interface IHttpInterceptor {
    void preRequest(OkRequest request) throws Exception;
}
