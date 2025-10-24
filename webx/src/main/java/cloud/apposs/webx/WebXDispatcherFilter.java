package cloud.apposs.webx;

import cloud.apposs.balance.ping.PingFactory;
import cloud.apposs.balance.rule.RuleFactory;
import cloud.apposs.configure.ConfigurationFactory;
import cloud.apposs.discovery.DiscoveryFactory;
import cloud.apposs.discovery.IDiscovery;
import cloud.apposs.guard.GuardRuleConfig;
import cloud.apposs.guard.GuardRuleManager;
import cloud.apposs.ioc.BeanFactory;
import cloud.apposs.logger.Configuration;
import cloud.apposs.logger.Logger;
import cloud.apposs.okhttp.HttpBuilder;
import cloud.apposs.okhttp.OkHttp;
import cloud.apposs.registry.IRegistry;
import cloud.apposs.registry.RegistryFactory;
import cloud.apposs.registry.ServiceInstance;
import cloud.apposs.rest.*;
import cloud.apposs.util.*;
import cloud.apposs.webx.WebXConfig.GuardRule;
import cloud.apposs.webx.annotation.Crontab;
import cloud.apposs.webx.annotation.Scheduled;
import cloud.apposs.webx.banner.Banner;
import cloud.apposs.webx.banner.WebXBanner;
import cloud.apposs.webx.management.ManagementContext;
import cloud.apposs.webx.management.netty.NettyManagementContext;
import cloud.apposs.webx.resource.Resource;
import cloud.apposs.webx.resource.ResourceManager;
import cloud.apposs.webx.schedule.CronTask;
import cloud.apposs.webx.schedule.QuartzManager;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 前端控制器，在Web容器中都是单例模式，
 * 即Web容器多线程下都是调用该实例
 */
public class WebXDispatcherFilter implements Filter {
    private static final long serialVersionUID = 7594221172926583766L;

    /** WebX全局配置 */
    private WebXConfig config;

    /** 项目名称 */
    private String filterName;
    /** 服务启动开始时间 */
    private long appStartTime;

    private AntPathMatcher patchMatcher;

    /** RESTFUL MVC组件 */
    private Restful<HttpServletRequest, HttpServletResponse> restful;
    private IGuardProcess<HttpServletRequest, HttpServletResponse> guard;

    /** 定时任务执行管理器 */
    private final QuartzManager quartzManager = new QuartzManager();

    /** 异步HTTP请求组件 */
    private OkHttp okHttp;

    /** 服务注册组件 */
    private IRegistry registry;

    // 服务监控指标，主要用于业务自行扩展监控指标
    protected final PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

    private Banner.Mode bannerMode = Banner.Mode.CONSOLE;
    private static final Banner DEFAULT_BANNER = new WebXBanner();

    public WebXDispatcherFilter() {
        this.patchMatcher = new AntPathMatcher();
    }

    /**
     * 初始化操作，Filter属于单例，在Web服务启动时只初始化一次
     */
    @SuppressWarnings("unchecked")
    @Override
    public final void init(FilterConfig filterConfig) throws ServletException {
        // 初始化应用信息
        filterName = filterConfig.getFilterName();
        appStartTime = System.currentTimeMillis();

        // 初始化配置
        config = handleInitConfiguration(filterConfig);

        // 初始化日志
        handleInitLogger(config);

        // 初始化MVC框架，从框架配置扫描包路径中扫描所有Bean实例
        RestConfig restConfig = handleInitRestConfig(config);
        this.restful = new Restful<HttpServletRequest, HttpServletResponse>(restConfig);
        // 将Config配置注入IOC容器中，方便Action直接通过@Autowired来获取Config配置
        BeanFactory beanFactory = restful.getBeanFactory();
        beanFactory.addBean(config);
        // 将指标监控注入IOC容器中，方便业务获取和自定义指标扩展
        beanFactory.addBean(prometheusRegistry);
        // 初始化异步OkHttp组件
        okHttp = handleInitOkHttpConfig(config);
        if (okHttp != null) {
            beanFactory.addBean(okHttp);
        }

        // 输出BANNER信息
        Banner banner = beanFactory.getBeanHierarchy(Banner.class);
        if (banner == null) {
            banner = DEFAULT_BANNER;
        }
        handleInitBanner(banner);
        // 输出系统信息
        handlePrintSysInfomation();

        // 初始化RESTFUL MVC框架
        handleInitRestful();
        // 初始化框架内部监听服务
        if (config.isManagementEnable()) {
            handleManagementActive();
        }
        // 初始化熔断参数解析服务
        guard = beanFactory.getBeanHierarchy(IGuardProcess.class);
        handleInitGuardRuleConfig();

        // 初始化定时任务服务
        handleInitSchedule(beanFactory, quartzManager, config);

        // 初始化资源服务
        Resource resource = beanFactory.getBeanHierarchy(Resource.class);
        if (resource != null) {
            resource.loadResList();
            ResourceManager.setResource(resource);
        }

        // 如果开启服务注册的话，注册服务到配置中心，方便客户端进行服务发现和负载均衡
        ServiceInstance serviceInstance = handleInitServiceInstance(config);
        if (serviceInstance != null) {
            try {
                WebXConfig.RegistryConfig regstConfig = config.getRegistryConfig();
                registry = RegistryFactory.createRegistry(regstConfig.getRegistryType(), regstConfig.getRegistryUrl(), regstConfig.getRegistryPath());
                registry.registInstance(serviceInstance);
            } catch (Exception e) {
                String message = String.format("Initialization WebX registry for filter[%s] error[%s];",
                        config.getFilterName(), e.getMessage());
                throw new ServletException(message, e);
            }
        }

        StringBuilder message = new StringBuilder();
        message.append(!Objects.isNull(serviceInstance) ? serviceInstance.getHost() : WebXConstants.DEFAULT_HOST).append(":")
                .append(TomcatMBeanUtil.getTomcatPort()).append("[Tomcat]");
        if (config.isManagementEnable()) {
            message.append(", ").append(config.getManagementHost()).append(":").append(config.getManagementPort()).append("[Management]");
        }
        Logger.info("%s Framework Startup In %d MilliSeconds, Listen: %s", filterName, System.currentTimeMillis() - appStartTime, message);
    }

    @Override
    public final void doFilter(ServletRequest req, ServletResponse rsp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) rsp;

        // 判断是否配置了特定URL或者特定HTTP REQUEST METHOD不经过过滤器
        if (isUrlExcluded(request) || isMethodExcluded(request)) {
            chain.doFilter(request, response);
            return;
        }

        // 开始对请求进行业务逻辑操作
        handleRequest(request, response);
    }

    /**
     * 开始对请求进行业务逻辑操作
     */
    private void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        handleInitRequest(request, config);
        // 调用RESTFUL框架进行Handler处理和视图渲染
        restful.renderView(new WebXHandlerProcess(), request, response);
    }

    /**
     * 判断是否配置了特定Url不经过过滤器
     */
    private boolean isUrlExcluded(HttpServletRequest request) {
        String[] excludeUrlPatterns = config.getExcludeUrlPatterns();
        if (excludeUrlPatterns == null || excludeUrlPatterns.length <= 0) {
            return false;
        }
        String urlPath = WebUtil.getRequestPath(request);
        for (int i = 0; i < excludeUrlPatterns.length; i++) {
            String excludePattern = excludeUrlPatterns[i];
            if (patchMatcher.match(excludePattern, urlPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否配置了特定HTTP REQUEST METHOD不经过过滤器
     */
    private boolean isMethodExcluded(HttpServletRequest request) {
        String[] excludeMethods = config.getExcludeMethods();
        if (excludeMethods == null || excludeMethods.length <= 0) {
            return false;
        }
        String method = request.getMethod();
        for (int i = 0; i < excludeMethods.length; i++) {
            String excludeMethod = excludeMethods[i];
            if (method.equalsIgnoreCase(excludeMethod)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 设置框架全局属性到Request请求中，以便于业务需要的话可以取出
     */
    private static void handleInitRequest(HttpServletRequest request, WebXConfig config) {
        // 设置全局配置到请求会话中
        request.setAttribute(WebXConstants.REQUEST_ATTRIBUTE_WEBXCONFIG, config);
    }

    /**
     * 加载并初始化配置
     */
    private static WebXConfig handleInitConfiguration(FilterConfig filterConfig) throws ServletException {
        // 定位WebX框架Xml配置文件
        String configLocation = filterConfig.getInitParameter(WebXConstants.INIT_CONFIG_LOCATION);
        String configClass = filterConfig.getInitParameter(WebXConstants.INIT_CONFIG_CLASS);
        String filterName = filterConfig.getFilterName();
        if (StrUtil.isEmpty(configLocation)) {
            configLocation = WebXConstants.WEB_PATH + filterName + WebXConstants.CONFIG_FILE_SUFFIX;
        }

        // 加载WebX框架Xml配置文件，初始化配置
        // 框架配置支持从XML文件读取，或者业务方自己实现配置类
        WebXConfig configuration = null;
        try {
            if (!StrUtil.isEmpty(configClass)) {
                // 优先实例化业务方实现的配置类
                configuration = (WebXConfig) ClassUtil.loadClass(configClass).newInstance();
            } else {
                // 从XML文件加载配置
                InputStream stream = filterConfig.getServletContext().getResourceAsStream(configLocation);
                if (stream == null) {
                    throw new FileNotFoundException(configLocation);
                }
                configuration = new WebXConfig(stream, ConfigurationFactory.XML);
            }
            configuration.setFilterName(filterName);
            // 读取设置要过滤的拦截URL
            String excludeUrlPattern = configuration.getExcludeUrlPattern();
            if (!StrUtil.isEmpty(excludeUrlPattern)) {
                String[] excludeUrlPatterns = excludeUrlPattern.split(",");
                for (int i = 0; i < excludeUrlPatterns.length; i++) {
                    excludeUrlPatterns[i] = excludeUrlPatterns[i].trim();
                }
                configuration.setExcludeUrlPatterns(excludeUrlPatterns);
            }
            // 读取设置要过滤的拦截URL
            String excludeMethod = configuration.getExcludeMethod();
            if (!StrUtil.isEmpty(excludeMethod)) {
                String[] excludeMethods = excludeMethod.split(",");
                for (int i = 0; i < excludeMethods.length; i++) {
                    excludeMethods[i] = excludeMethods[i].trim();
                }
                configuration.setExcludeMethods(excludeMethods);
            }
        } catch (Exception e) {
            String message = String.format("Initialization webx config '%s' for filter[%s] error '%s';",
                    configLocation, filterName, e.getMessage());
            throw new ServletException(message, e);
        }
        return configuration;
    }

    /**
     * 初始化日志
     */
    private static void handleInitLogger(WebXConfig config) {
        Properties properties = new Properties();
        properties.put(Configuration.Prefix.APPENDER, config.getLogAppender());
        properties.put(Configuration.Prefix.LEVEL, config.getLogLevel());
        properties.put(Configuration.Prefix.FORMAT, config.getLogFormat());
        properties.put(Configuration.Prefix.FILE, config.getLogPath());
        Logger.config(properties);
    }

    public static OkHttp handleInitOkHttpConfig(WebXConfig config) throws ServletException {
        WebXConfig.OkHttpConfig okHttpConfig = config.getOkHttpConfig();
        if (!okHttpConfig.isEnable()) {
            return null;
        }
        // 初始化服务发现组件
        try {
            IDiscovery discovery = null;
            String discoveryType = okHttpConfig.getDiscoveryType();
            List<String> discoveryArgList = okHttpConfig.getDiscoveryArgs();
            if (discoveryArgList != null) {
                String[] discoveryArgs = new String[discoveryArgList.size()];
                discoveryArgList.toArray(discoveryArgs);
                discovery = DiscoveryFactory.createDiscovery(discoveryType, discoveryArgs);
                // 初始化轮询策略和检测策略
                Map<String, WebXConfig.BalanceMode> balancerRules = okHttpConfig.getBalancer();
                for (String serviceId : balancerRules.keySet()) {
                    WebXConfig.BalanceMode balanceMode = balancerRules.get(serviceId);
                    discovery.setRule(serviceId, RuleFactory.createRule(balanceMode.getRule()));
                    discovery.setPing(serviceId, PingFactory.createPing(balanceMode.getPing()));
                }
            }
            // 创建异步HTTP组件并注入到IOC容器中供业务直接使用
            OkHttp okHttp = HttpBuilder.builder()
                    .loopSize(okHttpConfig.getLoopSize())
                    .ioMode(okHttpConfig.getIoMode())
                    .poolConnections(okHttpConfig.getPoolConnections())
                    .retryCount(okHttpConfig.getRetryCount())
                    .retrySleepTime(okHttpConfig.getRetrySleepTime())
                    .discovery(discovery).build();
            return okHttp;
        } catch (Exception e) {
            String message = String.format("Initialization WebX component[okhttp] for filter[%s] error[%s];",
                    config.getFilterName(), e.getMessage());
            throw new ServletException(message, e);
        }
    }

    /**
     * 初始化BANNER输出
     */
    private void handleInitBanner(Banner banner) {
        if (bannerMode != Banner.Mode.OFF) {
            if (bannerMode == Banner.Mode.CONSOLE) {
                banner.printBanner(System.out);
            } else {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                banner.printBanner(new PrintStream(baos));
                try {
                    Logger.info(baos.toString(config.getCharset()));
                } catch (UnsupportedEncodingException e) {
                }
            }
        }
    }

    /**
     * 输出系统信息
     */
    private void handlePrintSysInfomation() {
        if (config.isShowSysInfo()) {
            SystemInfo OS = SystemInfo.getInstance();
            Logger.info("OS Name: %s", OS.getOsName());
            Logger.info("OS Arch: %s", OS.getOsArch());
            Logger.info("Java Home: %s", OS.getJavaHome());
            Logger.info("Java Version: %s", OS.getJavaVersion());
            Logger.info("Java Vendor: %s", OS.getJavaVendor());
            List<String> jvmArguments = OS.getJvmArguments();
            for (String argument : jvmArguments) {
                Logger.info("Jvm Argument: [%s]", argument);
            }
        }
    }

    /**
     * 初始化REST配置
     */
    private static RestConfig handleInitRestConfig(WebXConfig config) {
        RestConfig restConfig = new RestConfig();
        restConfig.setAttachment(config);
        restConfig.setCharset(config.getCharset());
        String basePackage = config.getBasePackage();
        // 是否配置中不存在WebX框架中的包，则需要配置进去，方便扫描WebX框架中的各种组件包
        if (!StrUtil.isEmpty(basePackage)) {
            String webxPackage = ReflectUtil.getPackage(WebXDispatcherFilter.class);
            String[] basePackageSplit = basePackage.split(",");
            List<String> basePackageList = new ArrayList<String>(basePackageSplit.length);
            for (int i = 0; i < basePackageSplit.length; i++) {
                basePackageList.add(basePackageSplit[i].trim());
            }
            if (!basePackageList.contains(webxPackage + ".interceptor")) {
                basePackageList.add(webxPackage + ".interceptor");
            }
            if (!basePackageList.contains(webxPackage + ".listener")) {
                basePackageList.add(webxPackage + ".listener");
            }
            if (!basePackageList.contains(webxPackage + ".resolver")) {
                basePackageList.add(webxPackage + ".resolver");
            }
            basePackage = StrUtil.joinArrayString(basePackageList, ",");
        }
        restConfig.setBasePackage(basePackage);
        restConfig.setContextPath(config.getContextPath());
        restConfig.setReadonly(config.isReadonly());
        restConfig.setHttpLogEnable(config.isHttpLogEnable());
        restConfig.setHttpLogFormat(config.getHttpLogFormat());
        restConfig.setWorkerCount(config.getWorkerCount());
        return restConfig;
    }

    /**
     * 初始化RESTFUL MVC框架
     */
    private void handleInitRestful() throws ServletException {
        try {
            restful.initialize();
        } catch (Exception e) {
            String message = String.format("Initialization WebX restful for filter[%s] error[%s];",
                    config.getFilterName(), e.getMessage());
            throw new ServletException(message, e);
        }
    }

    /**
     * 启用内部框架管理监听服务
     */
    private void handleManagementActive() throws ServletException {
        try {
            ManagementContext context = new NettyManagementContext(restful, config);
            context.startup();
        } catch (Exception e) {
            String message = String.format("Initialization WebX management for filter[%s] error[%s];",
                    config.getFilterName(), e.getMessage());
            throw new ServletException(message, e);
        }
    }

    /**
     * 初始化熔断配置
     */
    private void handleInitGuardRuleConfig() {
        List<GuardRule> guardRuleList = config.getRules();
        if (guardRuleList == null || guardRuleList.isEmpty()) {
            return;
        }

        for (GuardRule guardRule : guardRuleList) {
            GuardRuleConfig ruleConfig = new GuardRuleConfig();
            ruleConfig.setType(guardRule.getType());
            ruleConfig.setResource(guardRule.getResource());
            ruleConfig.setThreshold(guardRule.getThreshold());
            GuardRuleManager.loadRule(ruleConfig);
        }
    }

    /**
     * 初始化定时任务
     */
    private static void handleInitSchedule(BeanFactory beanFactory, QuartzManager quartzManager, WebXConfig config) throws ServletException {
        try {
            List<Class<?>> scheduleList = beanFactory.getClassAnnotationList(Scheduled.class);
            for (Class<?> scheduleClass : scheduleList) {
                Method[] scheduleMethods = scheduleClass.getDeclaredMethods();
                for (Method scheduleMethod : scheduleMethods) {
                    // 获取方法参数类型和参数名称
                    Crontab crontab = scheduleMethod.getAnnotation(Crontab.class);
                    if (crontab == null) {
                        continue;
                    }
                    String taskCron = crontab.value();
                    if (StrUtil.isEmpty(taskCron)) {
                        continue;
                    }
                    String taskName = crontab.name();
                    if (StrUtil.isEmpty(taskName)) {
                        taskName = scheduleMethod.getName();
                    }
                    Object scheduleObject = beanFactory.getBean(scheduleClass);
                    CronTask task = new CronTask(taskName, scheduleObject, scheduleMethod, taskCron);
                    quartzManager.addTask(task);
                }
            }
        } catch (Exception e) {
            String message = String.format("Initialization schedule for filter[%s] error[%s];",
                    config.getFilterName(), e.getMessage());
            throw new ServletException(message, e);
        }
    }

    /**
     * 如果开启服务注册的话，注册服务到配置中心，方便客户端进行服务发现和负载均衡
     */
    private static ServiceInstance handleInitServiceInstance(WebXConfig config) {
        WebXConfig.RegistryConfig regstConfig = config.getRegistryConfig();
        boolean enabelRegistry = regstConfig.isEnableRegistry() &&
                !StrUtil.isEmpty(regstConfig.getRegistryType()) &&
                !StrUtil.isEmpty(regstConfig.getRegistryInterface()) &&
                !StrUtil.isEmpty(regstConfig.getServiceId());
        if (!enabelRegistry) {
            return null;
        }

        Map<String, NetUtil.NetInterface> interfaces = NetUtil.getLocalAddressInfo();
        if (interfaces.isEmpty()) {
            throw new IllegalStateException("No network interface found");
        }
        String registryInterface = regstConfig.getRegistryInterface();
        NetUtil.NetInterface netInterface = interfaces.get(registryInterface);
        if (netInterface == null) {
            throw new IllegalStateException("No network interface found for " + registryInterface);
        }

        String serviceId = regstConfig.getServiceId();
        return new ServiceInstance(serviceId, netInterface.getLocalAddress().getHostAddress(), TomcatMBeanUtil.getTomcatPort());
    }

    private class WebXHandlerProcess implements IHandlerProcess<HttpServletRequest, HttpServletResponse> {
        @Override
        public String getRequestMethod(HttpServletRequest request, HttpServletResponse response) {
            return request.getMethod();
        }

        @Override
        public String getRequestPath(HttpServletRequest request, HttpServletResponse response) {
            return WebUtil.getRequestPath(request);
        }

        @Override
        public String getRequestHost(HttpServletRequest request, HttpServletResponse response) {
            return request.getRemoteHost();
        }

        @Override
        public void processVariable(HttpServletRequest request, HttpServletResponse response, Map<String, String> variables) {
            if (variables != null) {
                request.setAttribute(WebXConstants.REQUEST_ATTRIBUTE_VARIABLES, variables);
            }
        }

        @Override
        public void processHandler(HttpServletRequest request, HttpServletResponse response, Handler handler) {
            String produces = handler.getProduces();
            if (produces != null && !produces.isEmpty()) {
                response.setContentType(produces);
            }
        }

        @Override
        public IGuardProcess<HttpServletRequest, HttpServletResponse> getGuardProcess() {
            return guard;
        }

        @Override
        public void markAsync(HttpServletRequest request, HttpServletResponse response) {
            AsyncContext asyncContext = request.startAsync();
            asyncContext.setTimeout(config.getAsyncTimeout());
            request.setAttribute(WebXConstants.REQUEST_ATTRIBUTE_ASYNC, asyncContext);
        }
    }

    /**
     * 销毁操作，在WEB容器关闭时调用
     */
    @Override
    public void destroy() {
        restful.destroy();
        if (okHttp != null) {
            okHttp.close();
        }
        if (registry != null) {
            registry.release();
        }
        Logger.info("%s Framework Destroy. Running %s",
                filterName, StrUtil.formatTimeOutput(System.currentTimeMillis() - appStartTime));
        Logger.close(true);
    }
}
