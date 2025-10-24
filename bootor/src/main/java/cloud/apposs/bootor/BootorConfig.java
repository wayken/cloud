package cloud.apposs.bootor;

import cloud.apposs.cache.CacheConfig;
import cloud.apposs.cachex.CacheXConfig;
import cloud.apposs.cache.CacheConfig.JvmConfig;
import cloud.apposs.cache.CacheConfig.RedisConfig;
import cloud.apposs.configure.Value;
import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.logger.Appender;
import cloud.apposs.logger.Logger;
import cloud.apposs.registry.IRegistry;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

@Component
public class BootorConfig {
    public static final String IO_MODE_NETTY = "netty";

    /**
     * 数据传输时（文件上传）临时文件存储文件
     */
    public static final String DEFAULT_TMP_DIRECTORY = System.getProperty("java.io.tmpdir");
    /**
     * 最大接收的文件大小，小于0为不限制
     */
    public static final int DEFAULT_MAX_FILE_SIZE = -1;
    /**
     * 默认HTTP编码解码
     */
    public static final String DEFAULT_CHARSET = "utf-8";
    public static final long REBIND_SLEEPTIME = 500L;
    public static final int DEFAULT_SEND_TIMEOUT = 60 * 1000;
    public static final int DEFAULT_RECV_TIMEOUT = 60 * 1000;

    /**
     * 线上模式，即实际生产环境
     */
    public static final int DEV_MODE_ONLINE = 0;
    /**
     * 本地开发模式
     */
    public static final int DEV_MODE_LOCAL = 1;
    /**
     * 预览模式，即部署环境
     */
    public static final int DEV_MODE_DEPLOY = 2;

    /**
     * 扫描基础包，必须配置，框架会自动扫描Action注解类
     */
    protected String basePackage;

    /**
     * 底层网格模型，默认是采用NETTY
     */
    protected String ioMode = IO_MODE_NETTY;

    /**
     * 开发模式，不同的开发模式下逻辑或者日志输出级别可能不同，由各业务方定义
     * 0、线上模式，即实际生产环境
     * 2、本地开发模式
     * 3、预览模式，即部署环境
     */
    protected int devMode = DEV_MODE_ONLINE;

    private String filename;

    /**
     * 绑定服务器地址
     */
    private String host = "0.0.0.0";
    /**
     * 绑定服务器端口
     */
    private int port = -1;
    /**
     * 绑定的主机列表
     */
    private InetSocketAddress bindSocketAddress;

    /**
     * BACKLOG对应的是TCP/IP协议listen函数中的backlog参数，函数listen(int socketfd,int backlog)用来初始化服务端可连接队列，
     * 服务端处理客户端连接请求是顺序处理的，所以同一时间只能处理一个客户端连接
     * 多个客户端来的时候，服务端将不能处理的客户端连接请求放在队列中等待处理，backlog参数指定了等待队列的大小
     */
    private int backlog = 10240;

    /**
     * SO_REUSEADDR对应于套接字选项中的SO_REUSEADDR，这个参数表示允许重复使用本地地址和端口，
     * 比如，某个服务器进程占用了TCP的80端口进行监听，此时再次监听该端口就会返回错误，使用该参数就可以解决问题，
     * 该参数允许共用该端口，这个在服务器程序中比较常使用，
     * 比如某个进程非正常退出，该程序占用的端口可能要被占用一段时间才能允许其他进程使用，
     * 而且程序死掉以后，内核一需要一定的时间才能够释放此端口，不设置SO_REUSEADDR就无法正常使用该端口
     */
    private boolean reuseAddress = true;

    /**
     * 开启此参数，那么客户端在每次发送数据时，无论数据包的大小都会将这些数据发送出 去
     * 参考：
     * http://blog.csdn.net/huang_xw/article/details/7340241
     * http://www.open-open.com/lib/view/open1412994697952.html
     */
    private boolean tcpNoDelay = true;

    /**
     * 多少个EventLoop轮询器，主要用于处理各自网络读写数据，
     * 当服务性能不足可提高此配置提升对网络IO的并发处理，但注意EventLoop业务层必须要做到异步，不能有同步阻塞请求
     */
    private int numOfGroup = Runtime.getRuntime().availableProcessors() + 1;

    /**
     * 是否开启线程池
     */
    private boolean executorOn = false;

    /**
     * 工作线程池数量
     */
    private int workerCount = Runtime.getRuntime().availableProcessors() << 1;

    /**
     * 服务是否为只读
     */
    private boolean readonly;

    /**
     * 是否为调式模式
     */
    private boolean debug;

    /**
     * 服务编码
     */
    private String charset = "utf-8";

    /**
     * 访问应用时的 URL 前缀
     */
    private String contextPath = "";

    /**
     * 发送数据缓存默认分配内存大小
     */
    private int bufferSize = 2 * 1024;
    /**
     * 是否直接使用堆内存
     */
    private boolean bufferDirect = false;

    /**
     * 是否保持服务器端长连接，不检查网络超时
     */
    private boolean keepAlive = false;

    /**
     * 是否采用Linux底层Epoll网络模型，针对底层为NETTY
     * Netty底层会通过Native方法为调用底层Epoll函数，可以提升性能，减少GC
     */
    protected boolean useLinuxEpoll = false;

    /**
     * 网络接收超时时间
     */
    private int recvTimeout = DEFAULT_RECV_TIMEOUT;
    /**
     * 网络发送超时时间
     */
    private int sendTimeout = DEFAULT_SEND_TIMEOUT;

    /**
     * 数据传输时（文件上传）临时文件存储文件
     */
    private String tempDir = DEFAULT_TMP_DIRECTORY;

    /**
     * 最大接收的文件大小，小于0为不限制，
     * 主要为了保护业务不让用户上传文件过大，默认不限制
     */
    private long maxFileSize = DEFAULT_MAX_FILE_SIZE;

    /**
     * 是否将HTTP请求的HEADER KEY自动转换成小写，
     * 在查询header数据时直接用小写获取，无需遍历，便于提升性能，
     * 不过转换为小写业务传递的再获取的时候可能会踩坑，视业务特点而定
     */
    private boolean lowerHeaderKey = false;

    /**
     * 是否开启权限注解拦截验证
     */
    protected boolean authDriven = false;

    /** 服务管理相关配置 */
    private boolean managementEnable = false;
    private String managementHost = BootorConstants.DEFAULT_MANAGEMENT_HOST;
    private int managementPort = BootorConstants.DEFAULT_MANAGEMENT_PORT;
    private String managementContextPath = BootorConstants.DEFAULT_MANAGEMENT_CONTEXT_PATH;

    /**
     * 是否输出系统信息
     */
    protected boolean showSysInfo = true;

    /** 日志配置相关 */
    /** 日志输出终端 */
    protected String logAppender = Appender.CONSOLE;
    /**
     * 日志输出级别，
     * FATAL(致命)、ERROR(错误)、WARN(警告)、INFO(信息)、DEBUG(调试)、OFF(关闭)，
     * 默认为INFO
     */
    protected String logLevel = "INFO";
    /** 日志的存储路径 */
    protected String logPath = "log";
    /** 日志输出模板 */
    protected String logFormat = Logger.DEFAULT_LOG_FORMAT;

    /**
     * 是否输出请求日志
     */
    protected boolean httpLogEnable = true;

    /**
     * 请求日志输出格式
     */
    protected String httpLogFormat;

    /**
     * 服务发现异步请求组件
     */
    protected OkHttpConfig okHttpConfig;

    /**
     * 业务自定义配置
     */
    protected Object options;

    /**
     * 服务注册相关配置
     */
    protected RegistryConfig registryConfig;

    /**
     * 数据源相关配置，支持多数据源配置，适用场景：
     * 1、固定数据存储用mysql源存储，文本存储用es源存储，便于文档可通过ES快速检索
     * 2、主从数据库读写分离，写用主库，读用从库，减少数据库压力，提升读性能
     */
    protected Map<String, ResourceConfig> resources;

    /**
     * 限流规则列表
     */
    protected List<GuardRule> rules;

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public String getIoMode() {
        return ioMode;
    }

    public void setIoMode(String ioMode) {
        this.ioMode = ioMode;
    }

    public int getDevMode() {
        return devMode;
    }

    public void setDevMode(int devMode) {
        this.devMode = devMode;
    }

    public boolean isDevLocal() {
        return devMode == DEV_MODE_LOCAL;
    }

    public boolean isDevOnline() {
        return devMode == DEV_MODE_ONLINE;
    }

    public boolean isDevDeploy() {
        return devMode == DEV_MODE_DEPLOY;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public boolean isReuseAddress() {
        return reuseAddress;
    }

    public void setReuseAddress(boolean reuseAddress) {
        this.reuseAddress = reuseAddress;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public int getNumOfGroup() {
        return numOfGroup;
    }

    public void setNumOfGroup(int numOfGroup) {
        this.numOfGroup = numOfGroup;
    }

    public boolean isExecutorOn() {
        return executorOn;
    }

    public void setExecutorOn(boolean executorOn) {
        this.executorOn = executorOn;
    }

    public int getWorkerCount() {
        return workerCount;
    }

    public void setWorkerCount(int workerCount) {
        this.workerCount = workerCount;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public boolean isBufferDirect() {
        return bufferDirect;
    }

    public void setBufferDirect(boolean bufferDirect) {
        this.bufferDirect = bufferDirect;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public boolean isUseLinuxEpoll() {
        return useLinuxEpoll;
    }

    public void setUseLinuxEpoll(boolean useLinuxEpoll) {
        this.useLinuxEpoll = useLinuxEpoll;
    }

    public int getRecvTimeout() {
        return recvTimeout;
    }

    public void setRecvTimeout(int recvTimeout) {
        this.recvTimeout = recvTimeout;
    }

    public int getSendTimeout() {
        return sendTimeout;
    }

    public void setSendTimeout(int sendTimeout) {
        this.sendTimeout = sendTimeout;
    }

    public String getTempDir() {
        return tempDir;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public boolean isLowerHeaderKey() {
        return lowerHeaderKey;
    }

    public void setLowerHeaderKey(boolean lowerHeaderKey) {
        this.lowerHeaderKey = lowerHeaderKey;
    }

    public boolean isAuthDriven() {
        return authDriven;
    }

    public void setAuthDriven(boolean authDriven) {
        this.authDriven = authDriven;
    }

    public boolean isManagementEnable() {
        return managementEnable;
    }

    public void setManagementEnable(boolean managementEnable) {
        this.managementEnable = managementEnable;
    }

    public String getManagementHost() {
        return managementHost;
    }

    public void setManagementHost(String managementHost) {
        this.managementHost = managementHost;
    }

    public int getManagementPort() {
        return managementPort;
    }

    public void setManagementPort(int managementPort) {
        this.managementPort = managementPort;
    }

    public String getManagementContextPath() {
        return managementContextPath;
    }

    public void setManagementContextPath(String managementContextPath) {
        this.managementContextPath = managementContextPath;
    }

    public boolean isShowSysInfo() {
        return showSysInfo;
    }

    public void setShowSysInfo(boolean showSysInfo) {
        this.showSysInfo = showSysInfo;
    }

    public String getLogAppender() {
        return logAppender;
    }

    public void setLogAppender(String logAppender) {
        this.logAppender = logAppender;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public String getLogFormat() {
        return logFormat;
    }

    public void setLogFormat(String logFormat) {
        this.logFormat = logFormat;
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
        this.httpLogFormat = httpLogFormat;
    }

    public OkHttpConfig getOkHttpConfig() {
        return okHttpConfig;
    }

    public void setOkHttpConfig(OkHttpConfig okHttpConfig) {
        this.okHttpConfig = okHttpConfig;
    }

    public List<GuardRule> getRules() {
        return rules;
    }

    public void setRules(List<GuardRule> rules) {
        this.rules = rules;
    }

    public Object getOptions() {
        return options;
    }

    public void setOptions(Object options) {
        this.options = options;
    }

    public RegistryConfig getRegistryConfig() {
        return registryConfig;
    }

    public void setRegistryConfig(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
    }

    /**
     * 默认返回配置的第一个数据源配置
     */
    public ResourceConfig getResourceConfig() {
        // 没有配置数据源直接返回空
        if (resources == null) {
            return null;
        }

        for (String resouce : resources.keySet()) {
            return resources.get(resouce);
        }
        return null;
    }

    /**
     * 获取指定Key的数据源配置
     */
    public ResourceConfig getResourceConfig(String resource) {
        return resources.get(resource);
    }

    public Map<String, ResourceConfig> getResources() {
        return resources;
    }

    public void setResources(Map<String, ResourceConfig> resources) {
        this.resources = resources;
    }

    public CacheXConfig getCacheXConfig() {
        return getCacheXConfig(null);
    }

    /**
     * 获取数据源框架配置
     *
     * @param resource 指定的数据源类型，为空则返回第一个数据源配置
     */
    public CacheXConfig getCacheXConfig(String resource) {
        ResourceConfig resourceConfig = null;
        if (resource == null) {
            // 如果不指定数据源则返回第一个数据源配置
            resourceConfig = getResourceConfig();
        } else {
            resourceConfig = getResourceConfig(resource);
        }

        CacheXConfig cacheXConfig = new CacheXConfig();
        if (resourceConfig != null) {
            CacheConfig cacheConfig = new CacheConfig();
            cacheConfig.setType(resourceConfig.getCache());
            cacheXConfig.setCacheConfig(cacheConfig);
            CacheXConfig.DbConfig dbConfig = cacheXConfig.getDbConfig();
            DbPoolConfig dbPoolConfig = resourceConfig.getDbPoolConfig();
            if (dbPoolConfig != null) {
                dbConfig.setDialect(resourceConfig.getDialect());
                dbConfig.setJdbcUrl(dbPoolConfig.getJdbcUrl());
                dbConfig.setUsername(dbPoolConfig.getUsername());
                dbConfig.setPassword(dbPoolConfig.getPassword());
            }
            cacheConfig.setJvmConfig(resourceConfig.getJvmConfig());
            cacheConfig.setRedisConfig(resourceConfig.getRedisConfig());
            cacheXConfig.setCacheConfig(cacheConfig);
        }
        return cacheXConfig;
    }

    @Value("okhttp")
    public static class OkHttpConfig {
        public static final int DEFAULT_RETRY_COUNT = 3;
        public static final int DEFAULT_RETRY_SLEEP_TIME = 200;

        /**
         * 是否采用OkHttp对象注入，供业务方直接使用
         */
        protected boolean enable = false;

        /**
         * 异步轮询器数量
         */
        protected int loopSize = Runtime.getRuntime().availableProcessors();

        /**
         * HTTP请求失败后的重试次数，为0则不重试
         */
        private int retryCount = DEFAULT_RETRY_COUNT;

        /**
         * HTTP请求失败后重试的休眠失败，避免雪崩
         */
        private int retrySleepTime = DEFAULT_RETRY_SLEEP_TIME;

        /**
         * 异步请求服务发现类型
         */
        protected String discoveryType = "QConf";
        protected List<String> discoveryArgs;

        protected Map<String, BalanceMode> balancer;

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public int getLoopSize() {
            return loopSize;
        }

        public void setLoopSize(int loopSize) {
            this.loopSize = loopSize;
        }

        public int getRetryCount() {
            return retryCount;
        }

        public void setRetryCount(int retryCount) {
            this.retryCount = retryCount;
        }

        public int getRetrySleepTime() {
            return retrySleepTime;
        }

        public void setRetrySleepTime(int retrySleepTime) {
            this.retrySleepTime = retrySleepTime;
        }

        public String getDiscoveryType() {
            return discoveryType;
        }

        public void setDiscoveryType(String discoveryType) {
            this.discoveryType = discoveryType;
        }

        public List<String> getDiscoveryArgs() {
            return discoveryArgs;
        }

        public void setDiscoveryArgs(List<String> discoveryArgs) {
            this.discoveryArgs = discoveryArgs;
        }

        public Map<String, BalanceMode> getBalancer() {
            return balancer;
        }

        public void setBalancer(Map<String, BalanceMode> balancer) {
            this.balancer = balancer;
        }
    }

    public static class BalanceMode {
        private String rule;

        private String ping;

        public String getRule() {
            return rule;
        }

        public void setRule(String rule) {
            this.rule = rule;
        }

        public String getPing() {
            return ping;
        }

        public void setPing(String ping) {
            this.ping = ping;
        }
    }

    @Value("registry")
    public static class RegistryConfig {
        /** 是否在启动之后注册服务信息到配置中心，即服务注册 */
        protected boolean enableRegistry = false;

        /** 服务注册的类型，有Zookeeper/QConf/File等 */
        protected String registryType = null;

        /** 配置中心地址 */
        protected String registryUrl = null;

        /** 服务注册的路径，如果是zk则为路径，如果是nacos则为groupName */
        protected String registryPath = IRegistry.DEFAULT_REGISTRY_ROOT_PATH;

        /** 服务启动时读取的网卡信息，微服务最好统一网卡，例如统一用eth0作为绑定网卡地址 */
        protected String registryInterface = null;

        /** 服务注册Service Id */
        protected String serviceId = null;

        public boolean isEnableRegistry() {
            return enableRegistry;
        }

        public void setEnableRegistry(boolean enableRegistry) {
            this.enableRegistry = enableRegistry;
        }

        public String getRegistryType() {
            return registryType;
        }

        public void setRegistryType(String registryType) {
            this.registryType = registryType;
        }

        public String getRegistryUrl() {
            return registryUrl;
        }

        public void setRegistryUrl(String registryUrl) {
            this.registryUrl = registryUrl;
        }

        public String getRegistryPath() {
            return registryPath;
        }

        public void setRegistryPath(String registryPath) {
            this.registryPath = registryPath;
        }

        public String getRegistryInterface() {
            return registryInterface;
        }

        public void setRegistryInterface(String registryInterface) {
            this.registryInterface = registryInterface;
        }

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }
    }

    @Value("rule")
    public static class GuardRule {
        private String type;

        private String resource;

        private int threshold;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getResource() {
            return resource;
        }

        public void setResource(String resource) {
            this.resource = resource;
        }

        public int getThreshold() {
            return threshold;
        }

        public void setThreshold(int threshold) {
            this.threshold = threshold;
        }
    }

    @Value("cachex")
    public static class ResourceConfig {
        /**
         * 数据库方言，默认为MYSQL
         */
        private String dialect;

        /**
         * 缓存类型，默认为JVM内存
         */
        private String cache;

        /**
         * 数据源相关配置
         */
        protected DbPoolConfig dbPoolConfig;

        /**
         * JVM缓存相关配置
         */
        @Value("jvm")
        private JvmConfig jvmConfig = new JvmConfig();

        /**
         * Redis缓存相关配置
         */
        @Value("redis")
        private RedisConfig redisConfig = new RedisConfig();

        public String getDialect() {
            return dialect;
        }

        public void setDialect(String dialect) {
            this.dialect = dialect;
        }

        public String getCache() {
            return cache;
        }

        public void setCache(String cache) {
            this.cache = cache;
        }

        public DbPoolConfig getDbPoolConfig() {
            return dbPoolConfig;
        }

        public void setDbPoolConfig(DbPoolConfig dbPoolConfig) {
            this.dbPoolConfig = dbPoolConfig;
        }

        public JvmConfig getJvmConfig() {
            return jvmConfig;
        }

        public void setJvmConfig(JvmConfig jvmConfig) {
            this.jvmConfig = jvmConfig;
        }

        public RedisConfig getRedisConfig() {
            return redisConfig;
        }

        public void setRedisConfig(RedisConfig redisConfig) {
            this.redisConfig = redisConfig;
        }
    }

    @Value("dbpool")
    public static class DbPoolConfig {
        /**
         * 数据库URL连接
         */
        private String jdbcUrl;

        /**
         * 数据库用户名
         */
        private String username;

        /**
         * 数据库密码
         */
        private String password;

        /**
         * 连接池最小Connection连接数
         */
        private int minConnections = 12;

        /**
         * 连接池最大Connection连接数，包括空闲和忙碌的Connection连接数
         */
        private int maxConnections = Byte.MAX_VALUE;

        public String getJdbcUrl() {
            return jdbcUrl;
        }

        public void setJdbcUrl(String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public int getMinConnections() {
            return minConnections;
        }

        public void setMinConnections(int minConnections) {
            this.minConnections = minConnections;
        }

        public int getMaxConnections() {
            return maxConnections;
        }

        public void setMaxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
        }
    }
}
