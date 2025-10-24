package cloud.apposs.bootor;

import cloud.apposs.balance.ping.PingFactory;
import cloud.apposs.balance.rule.RuleFactory;
import cloud.apposs.bootor.BootorConfig.GuardRule;
import cloud.apposs.bootor.BootorConfig.RegistryConfig;
import cloud.apposs.bootor.banner.Banner;
import cloud.apposs.bootor.banner.BootorBanner;
import cloud.apposs.bootor.filter.FilterChain;
import cloud.apposs.bootor.filter.IFilter;
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
import cloud.apposs.rest.IGuardProcess;
import cloud.apposs.rest.RestConfig;
import cloud.apposs.rest.Restful;
import cloud.apposs.rest.annotation.Order;
import cloud.apposs.util.NetUtil;
import cloud.apposs.util.NetUtil.NetInterface;
import cloud.apposs.util.ReflectUtil;
import cloud.apposs.util.StrUtil;
import cloud.apposs.util.SystemInfo;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 服务启动接口，由NETTY实现具体的网络服务绑定和请求参数转换功能
 */
public abstract class ApplicationContext {
    // 全局配置
    protected BootorConfig config;

    protected Class<?> primarySource;

    protected AtomicBoolean shutdown = new AtomicBoolean(false);

    protected FilterChain filterChain;

    // RESTFUL MVC组件
    protected Restful<BootorHttpRequest, BootorHttpResponse> restful;

    // 异步HTTP请求组件
    protected OkHttp okHttp;

    // 服务注册组件
    protected IRegistry registry;

    // 服务限流隔断组件
    protected IGuardProcess<BootorHttpRequest, BootorHttpResponse> guard;

    protected Banner.Mode bannerMode = Banner.Mode.CONSOLE;
    protected static final Banner DEFAULT_BANNER = new BootorBanner();
    protected Banner banner = DEFAULT_BANNER;

    // 服务启动开始时间
    protected long appStartTime;

    // 服务监控指标，主要用于业务自行扩展监控指标
    protected final PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

    public ApplicationContext() {
        this(new BootorConfig());
    }

    public ApplicationContext(BootorConfig config) {
        this.config = config;
    }

    /**
     * 启动HTTP服务
     */
    public ApplicationContext run(Class<?> primarySource, String... args) throws Exception {
        this.primarySource = primarySource;
        this.appStartTime = System.currentTimeMillis();
        try {
            handleRunApplication();
            StringBuilder message = new StringBuilder();
            message.append(config.getHost()).append(":").append(config.getPort()).append("[Plain]");
            if (config.isManagementEnable()) {
                message.append(", ").append(config.getManagementHost()).append(":").append(config.getManagementPort()).append("[Management]");
            }
            Logger.info("%s Bootstrap Server %s:%s Startup In %d MilliSeconds, Listen: %s", primarySource.getSimpleName(),
                    config.getHost(), config.getPort(), (System.currentTimeMillis() - appStartTime), message);
        } catch (Exception cause) {
            Logger.error(cause, "%s Bootstrap Server Startup Fail @%s:%s", primarySource.getSimpleName(),
                    config.getHost(), config.getPort());
            shutdown();
        }
        return this;
    }

    public ApplicationContext setBanner(Banner banner) {
        this.banner = banner;
        return this;
    }

    public BootorConfig getConfig() {
        return config;
    }

    public FilterChain getFilterChain() {
        return filterChain;
    }

    public Restful<BootorHttpRequest, BootorHttpResponse> getRestful() {
        return restful;
    }

    public IGuardProcess<BootorHttpRequest, BootorHttpResponse> getGuard() {
        return guard;
    }

    private void handleRunApplication() throws Exception {
        // 初始化日志
        handleInitLogger(config);

        // 初始化RESTFUL MVC框架
        RestConfig restConfig = handleInitRestConfig(config);
        restful = new Restful<BootorHttpRequest, BootorHttpResponse>(restConfig);
        // 注入框架本身的BEAN组件方便业务获取
        BeanFactory beanFactory = restful.getBeanFactory();
        // 将Config配置注入IOC容器中，方便Action直接通过@Autowired来获取Config配置
        beanFactory.addBean(config);
        // 将指标监控注入IOC容器中，方便业务获取和自定义指标扩展
        beanFactory.addBean(prometheusRegistry);
        // 初始化并注入异步OkHttp组件
        okHttp = handleInitOkHttpConfig(config);
        if (okHttp != null) {
            beanFactory.addBean(okHttp);
        }

        // 输出BANNER信息
        handleInitBanner(bannerMode, banner, config.getCharset());
        handlePrintSysInfomation();

        // 初始化MVC框架，从框架配置扫描包路径中扫描所有Bean实例
        restful.initialize();

        // 初始化过滤器链
        filterChain = new FilterChain();
        List<IFilter> filterList = beanFactory.getBeanHierarchyList(IFilter.class);
        handleOrderAnnotationSort(filterList);
        for (IFilter filter : filterList) {
            filterChain.addFilter(filter);
        }

        // 初始化熔断参数解析服务
        guard = beanFactory.getBeanHierarchy(IGuardProcess.class);
        handleIinitGuardRuleConfig(config);

        // 开始启动HTTP服务
        handleStartHttpServer(config);

        // 如果开启服务注册的话，注册服务到配置中心，方便客户端进行服务发现和负载均衡
        ServiceInstance serviceInstance = handleInitServiceInstance(config);
        if (serviceInstance != null) {
            RegistryConfig regstConfig = config.getRegistryConfig();
            registry = RegistryFactory.createRegistry(regstConfig.getRegistryType(), regstConfig.getRegistryUrl(), regstConfig.getRegistryPath());
            registry.registInstance(serviceInstance);
        }

        // 注册服务被kill时的回调
        registerShutdownHook();
    }

    /**
     * 初始化日志
     */
    private void handleInitLogger(BootorConfig config) {
        Properties properties = new Properties();
        properties.put(Configuration.Prefix.APPENDER, config.getLogAppender());
        properties.put(Configuration.Prefix.LEVEL, config.getLogLevel());
        properties.put(Configuration.Prefix.FILE, config.getLogPath());
        properties.put(Configuration.Prefix.FORMAT, config.getLogFormat());
        Logger.config(properties);
    }

    private OkHttp handleInitOkHttpConfig(BootorConfig config) throws Exception {
        BootorConfig.OkHttpConfig okHttpConfig = config.getOkHttpConfig();
        if (okHttpConfig == null || !okHttpConfig.isEnable()) {
            return null;
        }
        // 初始化服务发现组件，轮询策略和检测策略
        IDiscovery discovery = null;
        String discoveryType = okHttpConfig.getDiscoveryType();
        List<String> discoveryArgList = okHttpConfig.getDiscoveryArgs();
        if (discoveryArgList != null) {
            String[] discoveryArgs = new String[discoveryArgList.size()];
            discoveryArgList.toArray(discoveryArgs);
            discovery = DiscoveryFactory.createDiscovery(discoveryType, discoveryArgs);
            Map<String, BootorConfig.BalanceMode> balancerRules = okHttpConfig.getBalancer();
            for (String serviceId : balancerRules.keySet()) {
                BootorConfig.BalanceMode balanceMode = balancerRules.get(serviceId);
                discovery.setRule(serviceId, RuleFactory.createRule(balanceMode.getRule()));
                discovery.setPing(serviceId, PingFactory.createPing(balanceMode.getPing()));
            }
        }
        // 创建异步HTTP组件并注入到IOC容器中供业务直接使用
        OkHttp okHttp = HttpBuilder.builder()
                .loopSize(okHttpConfig.getLoopSize())
                .retryCount(okHttpConfig.getRetryCount())
                .retrySleepTime(okHttpConfig.getRetrySleepTime())
                .discovery(discovery).build();
        return okHttp;
    }

    /**
     * 初始化BANNER输出
     */
    private void handleInitBanner(Banner.Mode bannerMode, Banner banner, String charset) throws Exception {
        if (bannerMode != Banner.Mode.OFF) {
            if (bannerMode == Banner.Mode.CONSOLE) {
                banner.printBanner(System.out);
            } else {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                banner.printBanner(new PrintStream(baos));
                Logger.info(baos.toString(charset));
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
            Logger.info("IO Mode: %s", config.getIoMode());
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
    private RestConfig handleInitRestConfig(BootorConfig config) {
        RestConfig restConfig = new RestConfig();
        restConfig.setCharset(config.getCharset());
        String basePackage = config.getBasePackage();
        // 是否配置中不存在Bootor框架中的包，则需要配置进去，方便扫描Bootor框架中的各种组件包
        if (!StrUtil.isEmpty(basePackage)) {
            String bootorPackage = ReflectUtil.getPackage(ApplicationContext.class);
            String[] basePackageSplit = basePackage.split(",");
            List<String> basePackageList = new ArrayList<String>(basePackageSplit.length);
            for (int i = 0; i < basePackageSplit.length; i++) {
                basePackageList.add(basePackageSplit[i].trim());
            }
            if (!basePackageList.contains(bootorPackage + ".interceptor")) {
                basePackageList.add(bootorPackage + ".interceptor");
            }
            if (!basePackageList.contains(bootorPackage + ".listener")) {
                basePackageList.add(bootorPackage + ".listener");
            }
            if (!basePackageList.contains(bootorPackage + ".resolver")) {
                basePackageList.add(bootorPackage + ".resolver");
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
     * 根据Order注解进行列表的排序
     */
    private <T> void handleOrderAnnotationSort(List<T> compareList) {
        Collections.sort(compareList, new Comparator<T>() {
            @Override
            public int compare(T object1, T object2) {
                Order order1 = object1.getClass().getAnnotation(Order.class);
                Order order2 = object2.getClass().getAnnotation(Order.class);
                int order1Value = order1 == null ? 0 : order1.value();
                int order2Value = order2 == null ? 0 : order2.value();
                return order1Value - order2Value;
            }
        });
    }

    /**
     * 初始化熔断配置
     */
    private void handleIinitGuardRuleConfig(BootorConfig config) {
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
     * 如果开启服务注册的话，注册服务到配置中心，方便客户端进行服务发现和负载均衡
     */
    private ServiceInstance handleInitServiceInstance(BootorConfig config) throws Exception {
        RegistryConfig regstConfig = config.getRegistryConfig();
        if (regstConfig == null) {
            return null;
        }
        boolean enabelRegistry = regstConfig.isEnableRegistry() &&
                !StrUtil.isEmpty(regstConfig.getRegistryType()) &&
                !StrUtil.isEmpty(regstConfig.getRegistryInterface()) &&
                !StrUtil.isEmpty(regstConfig.getServiceId());
        if (!enabelRegistry) {
            return null;
        }

        Map<String, NetInterface> interfaces = NetUtil.getLocalAddressInfo();
        if (interfaces.isEmpty()) {
            throw new IllegalStateException("No network interface found");
        }
        String registryInterface = regstConfig.getRegistryInterface();
        NetInterface netInterface = interfaces.get(registryInterface);
        if (netInterface == null) {
            throw new IllegalStateException("No network interface found for " + registryInterface);
        }

        String serviceId = regstConfig.getServiceId();
        ServiceInstance serviceInstance = new ServiceInstance(serviceId,
                netInterface.getLocalAddress().getHostAddress(), config.getPort());
        return serviceInstance;
    }

    /**
     * 注册服务被kill时的回调，只能捕获kill -15的信号量 kill -9 没办法
     */
    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                shutdown();
            }
        });
    }

    /**
     * 关闭HTTP服务
     */
    public void shutdown() {
        if (shutdown.getAndSet(true)) {
            return;
        }

        if (registry != null) {
            registry.release();
        }
        if (okHttp != null) {
            okHttp.close();
        }
        if (restful != null) {
            restful.destroy();
        }
        handleCloseHttpServer();
        Logger.info("%s Bootor Server Has Been Shutdown. Running %s",
                primarySource.getSimpleName(), StrUtil.formatTimeOutput(System.currentTimeMillis() - appStartTime));
        Logger.close(true);
    }

    /**
     * 启动HTTP服务，由网络内核服务（如Netty/Undertow）根据自身服务特点启动
     */
    protected abstract void handleStartHttpServer(BootorConfig config) throws Exception;

    /**
     * 关闭服务，释放资源
     */
    protected abstract void handleCloseHttpServer();
}
