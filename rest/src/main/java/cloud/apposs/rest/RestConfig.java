package cloud.apposs.rest;

import cloud.apposs.util.ReflectUtil;
import cloud.apposs.util.StrUtil;

public class RestConfig {
    public static final String DEFAULT_CHARSET = "utf-8";

    private String charset = DEFAULT_CHARSET;

    /**
     * 扫描基础包，必须配置，框架会自动扫描Action注解类
     */
    private String basePackage;

    /**
     * 访问应用时的 URL 前缀
     */
    private String contextPath = "";

    private Object attachment;

    /**
     * 当前服务是否为只读，
     * 当开启只读模式时，所有Action中注解WriteCmd的指令均不再响应请求，直接抛出ReadOnlyException，
     * 这种一般只应用于线上服务数据迁移场景，保证用户可以展现页面但无法编辑页面
     */
    private boolean readonly = false;

    /**
     * 是否输出请求日志
     */
    protected boolean httpLogEnable = true;

    /**
     * 配置默认线程池数量，配合{@link cloud.apposs.rest.annotation.Executor}注解使用，
     * 被注解的Handler才会使用该线程池执行
     */
    private int workerCount = Runtime.getRuntime().availableProcessors();

    /**
     * 请求日志输出格式
     */
    protected String httpLogFormat =
            "$method $host $uri $status $action.$handler $remote_addr:$remote_port $attr_errno $attr_flow $time(ms)";

    public RestConfig() {
    }

    public RestConfig(Class<?> primarySource) {
        this.basePackage = ReflectUtil.getPackage(primarySource);
    }

    /**
     * 框架过滤不拦截的URL
     */
    protected String excludeUrlPattern;
    protected String[] excludeUrlPatterns;

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public Object getAttachment() {
        return attachment;
    }

    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

    public String getExcludeUrlPattern() {
        return excludeUrlPattern;
    }

    public void setExcludeUrlPattern(String excludeUrlPattern) {
        this.excludeUrlPattern = excludeUrlPattern;
    }

    public String[] getExcludeUrlPatterns() {
        return excludeUrlPatterns;
    }

    public void setExcludeUrlPatterns(String[] excludeUrlPatterns) {
        this.excludeUrlPatterns = excludeUrlPatterns;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public boolean isHttpLogEnable() {
        return httpLogEnable;
    }

    public void setHttpLogEnable(boolean httpLogEnable) {
        this.httpLogEnable = httpLogEnable;
    }

    public String getHttpLogFormat() {
        return httpLogFormat;
    }

    public void setHttpLogFormat(String httpLogFormat) {
        if (!StrUtil.isEmpty(httpLogFormat)) {
            this.httpLogFormat = httpLogFormat;
        }
    }

    public int getWorkerCount() {
        return workerCount;
    }

    public void setWorkerCount(int workerCount) {
        this.workerCount = workerCount;
    }
}
