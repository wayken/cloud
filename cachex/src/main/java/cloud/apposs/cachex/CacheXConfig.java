package cloud.apposs.cachex;

import cloud.apposs.cache.CacheConfig;

import java.nio.charset.Charset;

/**
 * 数据源框架全局配置，业务可继承此方法，
 * 一个配置即一个数据源，内部对应是哪个DB数据源，哪个缓存类型
 */
public class CacheXConfig {
    /**
     * 是否为开发模式，
     * 开发模式下有几种情况：
     * 1、输出SQL执行语句
     */
    private boolean develop = false;

    /**
     * 所有操作的字符，便于统一字符编码
     */
    private String charsetName = "utf-8";
    private Charset chrset = Charset.forName(charsetName);

    /**
     * 缓存数据是否采用异步写，即数据存入数据库之后由线程异步执行写入缓存
     */
    private boolean writeBehind = false;

    /**
     * 数据库连接池相关配置
     */
    private DbConfig dbConfig = new DbConfig();

    /**
     * 缓存相关配置
     */
    private CacheConfig cacheConfig = new CacheConfig();

    public boolean isDevelop() {
        return develop;
    }

    public void setDevelop(boolean develop) {
        this.develop = develop;
    }

    public String getCharsetName() {
        return charsetName;
    }

    public void setCharsetName(String charsetName) {
        this.charsetName = charsetName;
        this.chrset = Charset.forName(charsetName);
    }

    public Charset getChrset() {
        return chrset;
    }

    public void setChrset(Charset chrset) {
        this.chrset = chrset;
    }

    public boolean isWriteBehind() {
        return writeBehind;
    }

    public void setWriteBehind(boolean writeBehind) {
        this.writeBehind = writeBehind;
    }

    public DbConfig getDbConfig() {
        return dbConfig;
    }

    public void setDbConfig(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public CacheConfig getCacheConfig() {
        return cacheConfig;
    }

    public void setCacheConfig(CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
    }

    /**
     * JDBC数据库及数据库连接池相关配置
     */
    public static class DbConfig {
        /**
         * 数据库方言，默认为MYSQL
         */
        private String dialect = CacheXConstants.DIALECT_MYSQL;

        /**
         * 数据库驱动类
         */
        private String driverClass;

        /**
         * 数据库URL连接
         */
        private String jdbcUrl;

        /**
         * 数据库名称
         */
        private String databaseName;

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

        /**
         * 当连接池中的连接耗尽的时候系统每次自增的Connection连接数，默认为4
         */
        private int acquireIncrement = 4;

        /**
         * Connection连接存活时间，单位毫秒，
         * 在Connection连接大于{@link #minConnections}的情况下将被关闭并移除
         */
        private long aliveTime = 60000L;

        /**
         * Statement缓存，小于等于0时不缓存Statement
         */
        private int statementsCacheSize = -1;

        /**
         * 连接的自动提交模式
         */
        private Boolean autoCommit;

        /**
         * Connection是否处于只读模式
         */
        private Boolean readOnly;

        /**
         * 是否为调试模式，输出SQL语句
         */
        private Boolean debuggable = false;

        /**
         * 对象的事务隔离级别
         */
        private Integer transactionIsolation;

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
         * 是否开启数据库连接池操作跟踪
         */
        private boolean poolOperationWatch = true;

        public String getDialect() {
            return dialect;
        }

        public void setDialect(String dialect) {
            this.dialect = dialect;
        }

        public String getDriverClass() {
            return driverClass;
        }

        public String getJdbcUrl() {
            return jdbcUrl;
        }

        public void setJdbcUrl(String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
        }

        public String getDatabaseName() {
            return databaseName;
        }

        public void setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
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

        public void setDriverClass(String driverClass) {
            this.driverClass = driverClass;
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

        public long getAliveTime() {
            return aliveTime;
        }

        public void setAliveTime(long aliveTime) {
            this.aliveTime = aliveTime;
        }

        public int getStatementsCacheSize() {
            return statementsCacheSize;
        }

        public void setStatementsCacheSize(int statementsCacheSize) {
            this.statementsCacheSize = statementsCacheSize;
        }

        public Boolean getAutoCommit() {
            return autoCommit;
        }

        public void setAutoCommit(Boolean autoCommit) {
            this.autoCommit = autoCommit;
        }

        public Boolean getReadOnly() {
            return readOnly;
        }

        public void setReadOnly(Boolean readOnly) {
            this.readOnly = readOnly;
        }

        public Boolean getDebuggable() {
            return debuggable;
        }

        public void setDebuggable(Boolean debuggable) {
            this.debuggable = debuggable;
        }

        public Integer getTransactionIsolation() {
            return transactionIsolation;
        }

        public void setTransactionIsolation(Integer transactionIsolation) {
            this.transactionIsolation = transactionIsolation;
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

        public static boolean isJDBCTemplate(String dialect) {
            if (CacheXConstants.DIALECT_MYSQL.equalsIgnoreCase(dialect)
                    || CacheXConstants.DIALECT_ORACLE.equalsIgnoreCase(dialect)
                    || CacheXConstants.DIALECT_SQLITE.equalsIgnoreCase(dialect)) {
                return true;
            }
            return false;
        }

        public static boolean isMongoTemplate(String dialect) {
            if (CacheXConstants.DIALECT_MONGODB.equalsIgnoreCase(dialect)) {
                return true;
            }
            return false;
        }
    }
}
