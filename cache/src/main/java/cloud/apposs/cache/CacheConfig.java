package cloud.apposs.cache;

import cloud.apposs.cache.jvm.CacheEvictionPolicyStrategy;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

public class CacheConfig {
    /**
     * 缓存类型，默认为JVM内存
     */
    private String type = Cache.CACHE_JVM;

    /**
     * 是否采用直接堆内存存储缓存
     */
    private boolean directBuffer = false;

    /**
     * 所有操作的字符，便于统一字符编码
     */
    private String charsetName = "utf-8";
    private Charset chrset = Charset.forName(charsetName);

    /**
     * JVM缓存相关配置
     */
    private JvmConfig jvmConfig = new JvmConfig();

    /**
     * Redis缓存相关配置
     */
    private RedisConfig redisConfig = new RedisConfig();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isDirectBuffer() {
        return directBuffer;
    }

    public void setDirectBuffer(boolean directBuffer) {
        this.directBuffer = directBuffer;
    }

    public Charset getChrset() {
        return chrset;
    }

    public void setChrset(Charset chrset) {
        this.chrset = chrset;
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

    /**
     * JVM缓存相关配置
     */
    public static class JvmConfig {
        /**
         * 缓存过期时间，单位毫秒，小于等于0为永不过期，默认为1小时
         */
        private int expirationTime = 1 * 60 * 60 * 1000;
        /**
         * 是否采用缓存过期时间随机，避免同一时间有大量缓存失效触发回收和数据落盘
         */
        private boolean expirationTimeRandom = true;
        /**
         * 缓存过期时间最小随机数，默认30分钟
         */
        private int expirationTimeRandomMin = 30 * 60 * 1000;
        /**
         * 缓存过期时间最大随机数，默认1小时
         */
        private int expirationTimeRandomMax = 60 * 60 * 1000;

        /**
         * 缓存过期定期检查间隔时间，默认为1分钟
         */
        private int expireCheckInterval = 60 * 1000;

        /**
         * 最多可以存放的缓存的条数，超过上限则会触发回收策略，-1为无限
         */
        private int maxElements = -1;

        /**
         * 最多可以存放的缓存的内存容量，单位字节(Byte)，超过上限则会触发回收策略，-1为无限
         */
        private long maxMemory = -1;

        /**
         * 缓存容器的并发级别，数据越多时需要设置的并发级别越高，否则获取Key数据会变成链表查找，性能会直线下降
         */
        private int concurrencyLevel = 1024;

        /**
         * 内存回收策略
         */
        private String evictionPolicy = CacheEvictionPolicyStrategy.CACHE_POLICY_LRU;

        public int getExpirationTime() {
            return expirationTime;
        }

        public void setExpirationTime(int expirationTime) {
            this.expirationTime = expirationTime;
        }

        public boolean isExpirationTimeRandom() {
            return expirationTimeRandom;
        }

        public void setExpirationTimeRandom(boolean expirationTimeRandom) {
            this.expirationTimeRandom = expirationTimeRandom;
        }

        public int getExpirationTimeRandomMin() {
            return expirationTimeRandomMin;
        }

        public void setExpirationTimeRandomMin(int expirationTimeRandomMin) {
            this.expirationTimeRandomMin = expirationTimeRandomMin;
        }

        public int getExpirationTimeRandomMax() {
            return expirationTimeRandomMax;
        }

        public void setExpirationTimeRandomMax(int expirationTimeRandomMax) {
            this.expirationTimeRandomMax = expirationTimeRandomMax;
        }

        public int getExpireCheckInterval() {
            return expireCheckInterval;
        }

        public void setExpireCheckInterval(int expireCheckInterval) {
            this.expireCheckInterval = expireCheckInterval;
        }

        public String getEvictionPolicy() {
            return evictionPolicy;
        }

        public void setEvictionPolicy(String evictionPolicy) {
            this.evictionPolicy = evictionPolicy;
        }

        public int getMaxElements() {
            return maxElements;
        }

        public void setMaxElements(int maxElements) {
            this.maxElements = maxElements;
        }

        public long getMaxMemory() {
            return maxMemory;
        }

        public void setMaxMemory(long maxMemory) {
            this.maxMemory = maxMemory;
        }

        public int getConcurrencyLevel() {
            return concurrencyLevel;
        }

        public void setConcurrencyLevel(int concurrencyLevel) {
            this.concurrencyLevel = concurrencyLevel;
        }
    }

    /**
     * Redis缓存相关配置
     */
    public static class RedisConfig {
        /**
         * Redis单机缓存管理
         */
        public static final int REDIS_CACHE_SINGLE = 0;
        /**
         * Redis多机集群管理
         */
        public static final int REDIS_CACHE_CLUSTER = 1;
        /**
         * Codis代理分布管理
         */
        public static final int REDIS_CACHE_CODIS = 2;

        /**
         * Redis集群节点线程监听服务
         */
        public static final int REDIS_WATCHER_THREAD = 0;
        /**
         * Redis集群节点ZooKeeper监听服务
         */
        public static final int REDIS_WATCHER_ZOOKEEPER = 1;
        /**
         * Redis集群节点QConf监听服务
         */
        public static final int REDIS_WATCHER_QCONF = 2;

        /**
         * Redis缓存管理模式，有
         * {@link RedisConfig#REDIS_CACHE_SINGLE}、
         * {@link RedisConfig#REDIS_CACHE_CLUSTER}、
         * {@link RedisConfig#REDIS_CACHE_CODIS}
         */
        private int cacheType = REDIS_CACHE_SINGLE;

        /**
         * Redis节点监听服务配置
         */
        private int watcherType = REDIS_WATCHER_THREAD;

        /**
         * 连接池最小Connection连接数
         */
        private int minConnections = 12;

        /**
         * 连接池最大Connection连接数，包括空闲和忙碌的Connection连接数
         */
        private int maxConnections = Byte.MAX_VALUE;

        /**
         * 当连接池中的连接耗尽的时候系统每次自增的Connection连接数，默认为4
         */
        private int acquireIncrement = 4;

        /**
         * Redis连接超时时间
         */
        private int connectTimeout = 4 * 1000;

        /**
         * Redis接收数据超时时间
         */
        private int recvTimeout = 60 * 1000;

        /**
         * 在取得连接的同时是否校验连接的有效性，默认为true，
         * 注意，开启此功能对连接池的性能将有一定影响
         */
        private boolean testConnectionOnCheckout = true;

        /**
         * 在回收连接的同时是否校验连接的有效性，默认为false，
         * 注意，开启此功能对连接池的性能将有一定影响
         */
        private boolean testConnectionOnCheckin = false;

        /**
         * 是否开启连接池操作跟踪
         */
        private boolean poolOperationWatch = true;

        /**
         * Redis节点列表，可以是Redis单机、集群，也可以是Codis代理集群，
         * Redis单机模式下只用第一个节点
         * ServerNameList为配置文件配置，格式为127.0.0.1:6030
         * ServerList为程序使用，通过ServerNameList初始化时也会初始化此字段，程序上也可调用addServer直接添加
         */
        private List<String> serverNameList = null;
        private List<RedisServer> serverList = new LinkedList<RedisServer>();

        /**
         * 缓存过期时间，单位毫秒，小于等于0为永不过期，默认为1小时
         */
        private int expirationTime = 1 * 60 * 60 * 1000;
        /**
         * 是否采用缓存过期时间随机，避免同一时间有大量缓存失效触发回收和数据落盘
         */
        private boolean expirationTimeRandom = false;
        /**
         * 缓存过期时间最小随机数
         */
        private int expirationTimeRandomMin = 30 * 60 * 1000;
        /**
         * 缓存过期时间最大随机数
         */
        private int expirationTimeRandomMax = 60 * 60 * 1000;

        public int getCacheType() {
            return cacheType;
        }

        public void setCacheType(int cacheType) {
            this.cacheType = cacheType;
        }

        public int getWatcherType() {
            return watcherType;
        }

        public void setWatcherType(int watcherType) {
            this.watcherType = watcherType;
        }

        public List<String> getServerNameList() {
            return serverNameList;
        }

        public void setServerNameList(List<String> serverNameList) {
            if (serverNameList == null || serverNameList.isEmpty()) {
                return;
            }
            this.serverNameList = serverNameList;
            serverList.clear();
            for (String serverName : serverNameList) {
                String[] serverInfo = serverName.split(":");
                if (serverInfo.length != 2) {
                    continue;
                }
                String serverHost = serverInfo[0];
                int serverPort = Integer.parseInt(serverInfo[1]);
                addServer(new RedisServer(serverHost, serverPort));
            }
        }

        public List<RedisServer> getServerList() {
            return serverList;
        }

        public void addServer(RedisServer server) {
            serverList.add(server);
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

        public int getAcquireIncrement() {
            return acquireIncrement;
        }

        public void setAcquireIncrement(int acquireIncrement) {
            this.acquireIncrement = acquireIncrement;
        }

        public int getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public int getRecvTimeout() {
            return recvTimeout;
        }

        public void setRecvTimeout(int recvTimeout) {
            this.recvTimeout = recvTimeout;
        }

        public boolean isTestConnectionOnCheckout() {
            return testConnectionOnCheckout;
        }

        public void setTestConnectionOnCheckout(boolean testConnectionOnCheckout) {
            this.testConnectionOnCheckout = testConnectionOnCheckout;
        }

        public boolean isTestConnectionOnCheckin() {
            return testConnectionOnCheckin;
        }

        public void setTestConnectionOnCheckin(boolean testConnectionOnCheckin) {
            this.testConnectionOnCheckin = testConnectionOnCheckin;
        }

        public boolean isPoolOperationWatch() {
            return poolOperationWatch;
        }

        public void setPoolOperationWatch(boolean poolOperationWatch) {
            this.poolOperationWatch = poolOperationWatch;
        }

        public int getExpirationTime() {
            return expirationTime;
        }

        public void setExpirationTime(int expirationTime) {
            this.expirationTime = expirationTime;
        }

        public boolean isExpirationTimeRandom() {
            return expirationTimeRandom;
        }

        public void setExpirationTimeRandom(boolean expirationTimeRandom) {
            this.expirationTimeRandom = expirationTimeRandom;
        }

        public int getExpirationTimeRandomMin() {
            return expirationTimeRandomMin;
        }

        public void setExpirationTimeRandomMin(int expirationTimeRandomMin) {
            this.expirationTimeRandomMin = expirationTimeRandomMin;
        }

        public int getExpirationTimeRandomMax() {
            return expirationTimeRandomMax;
        }

        public void setExpirationTimeRandomMax(int expirationTimeRandomMax) {
            this.expirationTimeRandomMax = expirationTimeRandomMax;
        }

        public static class RedisServer {
            private final String host;

            private final int port;

            /**
             * 是否配置该服务为在线服务状态，false则不作为在线服务
             */
            private final boolean online;

            public RedisServer(String host, int port) {
                this(host, port, true);
            }

            public RedisServer(String host, int port, boolean online) {
                this.host = host;
                this.port = port;
                this.online = online;
            }

            public String getHost() {
                return host;
            }

            public int getPort() {
                return port;
            }

            public boolean isOnline() {
                return online;
            }

            @Override
            public boolean equals(Object obj) {
                if (!(obj instanceof RedisServer)) {
                    return false;
                }
                RedisServer server = (RedisServer) obj;
                return host.equals(server.getHost()) && port == server.getPort();
            }

            @Override
            public int hashCode() {
                return (host + port).hashCode();
            }

            @Override
            public String toString() {
                StringBuilder info = new StringBuilder();
                info.append("[Host=").append(host);
                info.append(", Port=").append(port);
                info.append(", Online=").append(online);
                info.append("]");
                return info.toString();
            }
        }
    }
}
