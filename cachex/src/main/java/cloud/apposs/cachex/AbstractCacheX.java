package cloud.apposs.cachex;

import cloud.apposs.cache.CacheManager;
import cloud.apposs.cachex.database.Query;
import cloud.apposs.cachex.database.Updater;
import cloud.apposs.cachex.database.Where;
import cloud.apposs.protobuf.ProtoSchema;
import cloud.apposs.threadx.ThreadPool;
import cloud.apposs.threadx.ThreadPoolFactory;
import cloud.apposs.util.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 数据抽象，全局单例
 * 注意因为对数据和缓存多了一层封装，所以更多只针对主键的缓存，其他复杂查询则需要业务自己通过dao和cache来实现缓存
 */
public abstract class AbstractCacheX<K extends CacheKey, V> implements CacheX<K, V> {
    /**
     * 数据操作最终一致前的过期时间，一分钟
     */
    public static final int DEAFULT_CACHE_EXPIRE_TIME = 60 * 1000;

    /**
     * 数据配置
     */
    protected final CacheXConfig config;

    /**
     * 数据库服务
     */
    protected final DBTemplate template;

    /**
     * 缓存服务
     */
    protected final CacheManager cache;

    /**
     * 缓存数据加载服务
     */
    protected final CacheLoader<K, V> loader;

    /**
     * 缓存锁，用于从数据库中加载数据时加锁来保证服务原子性，
     * 如果外部已经有加锁了（如外部React.lock已经加异步锁），则不需要初始化此分段锁
     */
    protected final CacheLock lock;

    /**
     * 线程池，主要用于异步写缓存数据
     */
    private final ThreadPool threadPool;

    /**
     * 缓存统计服务
     */
    protected final CacheXStatistics statistics = new CacheXStatistics();

    public AbstractCacheX(CacheXConfig config, CacheLoader<K, V> loader) throws Exception {
        this(config, loader, CacheLock.DEFAULT_LOCK_LENGTH);
    }

    /**
     * 构造数据源
     * @param config     配置
     * @param loader     数据加载器
     * @param lockLength 分段缓存锁长度，
     *                   如果数据操作不需要加锁（如外部React.lock已经加异步锁），则可设置为-1，不初始化分段锁，减少内存消耗
     */
    public AbstractCacheX(CacheXConfig config, CacheLoader<K, V> loader, int lockLength) throws Exception {
        this.config = config;
        this.template = JDBCTemplateFactory.buildDBTemplate(config.getDbConfig());
        this.cache = new CacheManager(config.getCacheConfig());
        this.loader = loader;
        if (lockLength > 0) {
            this.lock = new CacheLock(lockLength);
        } else {
            this.lock = null;
        }
		if (config.isWriteBehind()) {
            this.threadPool = ThreadPoolFactory.createCachedThreadPool();
        } else {
            this.threadPool = null;
        }
        this.loader.initialize(this);
    }

    public CacheXConfig getConfig() {
        return config;
    }

    @Override
    public CacheManager getCache() {
        return cache;
    }

    @Override
    public DBTemplate getTemplate() {
        return template;
    }

    @Override
    public CacheXStatistics getStatistics() {
        return statistics;
    }

    @Override
    public int put(K key, V value, ProtoSchema schema, Object... args) throws Exception {
        if (key == null || value == null) {
            return -1;
        }

        return doPutCacheX(key, value, schema, config.isWriteBehind(), args);
    }

    @Override
    public int put(List<K> keys, List<V> values, ProtoSchema schema, Object... args) throws Exception {
        if (values == null) {
            return -1;
        }
        return doPutCacheX(keys, values, schema, config.isWriteBehind(), args);
    }

    @Override
    public V get(K key, ProtoSchema schema, Object... args) throws Exception {
        if (key == null) {
            return null;
        }

        return doGetCacheX(key, schema, config.isWriteBehind(), args);
    }

    @Override
    public List<V> get(List<K> keys, ProtoSchema schema, Object... args) throws Exception {
        if (keys == null || keys.size() <= 0) {
            throw new IllegalArgumentException("keys");
        }

        return doGetCacheX(keys, schema, config.isWriteBehind(), args);
    }

    @Override
    public V select(CacheKey<?> key, Query query, ProtoSchema schema, Object... args) throws Exception {
        return doSelectCacheX(key, query, schema, config.isWriteBehind(), args);
    }

    @Override
    public Table<V> query(CacheKey<?> key, Query query, ProtoSchema schema, Object... args) throws Exception {
        return doQueryCacheX(key, query, schema, config.isWriteBehind(), args);
    }

    @Override
    public boolean exist(K key, ProtoSchema schema, Object... args) throws Exception {
        V value = get(key, schema, args);
        return checkExist(value);
    }

    @Override
    public boolean exist(CacheKey<?> key, Query query, ProtoSchema schema, Object... args) throws Exception {
        V value = select(key, query, schema, args);
        return checkExist(value);
    }

    @Override
    public int replace(K key, V value, ProtoSchema schema, Object... args) throws Exception {
        if (key == null || value == null) {
            return -1;
        }

        return doReplaceCacheX(key, value, schema, config.isWriteBehind(), args);
    }

    @Override
    public int delete(K key, final Object... args) throws Exception {
        if (key == null) {
            return -1;
        }

        return doDeleteCacheX(key, args);
    }

    @Override
    public int delete(List<K> keys, Object... args) throws Exception {
        if (keys == null) {
            return -1;
        }

        return doDeleteCacheX(keys, args);
    }

    @Override
    public int delete(CacheKey<?> key, Where where, Object... args) throws Exception {
        if (key == null || where.isEmpty()) {
            return -1;
        }

        return doDeleteCacheX(key, where, args);
    }

    @Override
    public int delete(List<CacheKey<?>> keys, Where where, Object... args) throws Exception {
        if (keys == null || where.isEmpty()) {
            return -1;
        }

        return doDeleteCacheX(keys, where, args);
    }

    @Override
    public int update(K key, V value, ProtoSchema schema, final Object... args) throws Exception {
        if (key == null || value == null) {
            return -1;
        }

        return doUpdateCacheX(key, value, schema, args);
    }

    @Override
    public int update(List<K> keys, List<V> values, ProtoSchema schema, Object... args) throws Exception {
        if (keys == null || values == null || keys.isEmpty() || keys.size() != values.size()) {
            return -1;
        }

        return doUpdateCacheX(keys, values, schema, args);
    }

    @Override
    public int update(CacheKey<?> key, Updater updater, ProtoSchema schema, Object... args) throws Exception {
        if (key == null || updater == null || updater.isEmpty()) {
            return -1;
        }

        return doUpdateCacheX(key, updater, schema, args);
    }

    @Override
    public int update(List<CacheKey<?>> keys, Updater updater, ProtoSchema schema, Object... args) throws Exception {
        if (keys == null || updater == null || updater.isEmpty()) {
            return -1;
        }

        return doUpdateCacheX(keys, updater, schema, args);
    }

    @Override
    public int hput(K key, Object field, V value, ProtoSchema schema, Object... args) throws Exception {
        if (key == null || field == null) {
            return -1;
        }

        return doHputCacheX(key, field, value, schema, config.isWriteBehind(), args);
    }

    @Override
    public V hget(K key, Object field, ProtoSchema schema, Object... args) throws Exception {
        if (key == null || field == null) {
            return null;
        }

        return doHgetCacheX(key, field, schema, config.isWriteBehind(), args);
    }

    @Override
    public boolean hexist(K key, Object field, ProtoSchema schema, Object... args) throws Exception {
        V value = hget(key, field, schema, args);
        return checkHExist(value);
    }

    @Override
    public List<V> hgetAll(K key, ProtoSchema schema, Object... args) throws Exception {
        if (key == null) {
            return null;
        }

        return doHgetAllCacheX(key, schema, args);
    }

    @Override
    public int hdelete(K key, Object field, final Object... args) throws Exception {
        if (key == null || field == null) {
            return -1;
        }

        return doHdeleteCacheX(key, field, args);
    }

    @Override
    public int hdelete(K key, Object[] fields, final Object... args) throws Exception {
        if (key == null || fields == null) {
            return -1;
        }

        return doHdeleteCacheX(key, fields, args);
    }

    @Override
    public int hupdate(K key, Object field, V value,
                       ProtoSchema schema, final Object... args) throws Exception {
        if (key == null || field == null) {
            return -1;
        }

        return doHupdateCacheX(key, field, value, schema, args);
    }

    /**
     * 关闭数据库连接池、缓存连接、线程池，释放资源
     */
    @Override
    public synchronized void shutdown() {
        template.shutdown();
        cache.shutdown();
        if (threadPool != null) {
            threadPool.shutdown();
        }
    }

    @SuppressWarnings("unchecked")
    private int doPutCacheX(K key, V value, ProtoSchema schema, boolean writeBehind, Object... args) throws Exception {
        // 先将数据存储到数据库中
        Ref<Object> idRef = new Ref<Object>();
        int count = 0;
        int index = key.getLockIndex();
        if (index > 0 && lock != null) {
            try {
                // 加锁操作，避免同一时间有多个请求涌进
                lock.writeLock(index);
                // 把获取数据库连接的操作放到锁操作之后避免大量请求进来争锁，一开始就申请数据库连接占用资源
                count = loader.add(key, value, schema, template, idRef, args);
                if (count > 0 && idRef.value() != null) {
                    key.setPrimary(idRef.value());
                }
            } finally {
                lock.writeUnlock(index);
            }
        } else {
            // 不加锁操作，可以提升性能，但多个相同请求会有数据覆盖风险
            count = loader.add(key, value, schema, template, idRef, args);
            if (count > 0 && idRef.value() != null) {
                key.setPrimary(idRef.value());
            }
        }
        // 数据存储成功后并且有生成的主键，则将数据异步写入到缓存中
        if (count > 0 && key.getPrimary() != null) {
            doWriteCache(key, value, schema, writeBehind, args);
        }
        return count;
    }

    @SuppressWarnings("unchecked")
    private int doPutCacheX(List<K> keys, List<V> values, ProtoSchema schema,
                            boolean writeBehind, Object... args) throws Exception {
        // 先将数据存储到数据库中
        int count = 0;
        boolean cachable = keys != null && keys.size() == values.size();
        List<String> keyList = null;
        int index = -1;
        if (cachable && keys.size() > 0) {
            index = keys.get(0).getLockIndex();
            keyList = new ArrayList<String>(keys.size());
        }
        if (index > 0 && lock != null) {
            try {
                // 加锁操作，避免同一时间有多个请求涌进
                lock.writeLock(index);
                List<Object> idRefs = new ArrayList<Object>(values.size());
                count = loader.add(keys, values, schema, template, idRefs, args);
                if (count > 0 && cachable) {
                    for (int i = 0; i < idRefs.size(); i++) {
                        Object idRef = idRefs.get(i);
                        // 如果业务没有设置ID主键则表示不缓存了
                        if (idRef == null) {
                            cachable = false;
                            break;
                        }
                        K key = keys.get(i);
                        key.setPrimary(idRef);
                        keyList.add(key.getCacheKey());
                    }
                }
            } finally {
                lock.writeUnlock(index);
            }
        } else {
            // 不加锁操作，可以提升性能，但多个相同请求会有数据覆盖风险
            List<Object> idRefs = new ArrayList<Object>(values.size());
            count = loader.add(keys, values, schema, template, idRefs, args);
            if (count > 0 && cachable) {
                for (int i = 0; i < idRefs.size(); i++) {
                    Object idRef = idRefs.get(i);
                    // 如果业务没有设置ID主键则表示不缓存了
                    if (idRef == null) {
                        cachable = false;
                        break;
                    }
                    K key = keys.get(i);
                    key.setPrimary(idRef);
                    keyList.add(key.getCacheKey());
                }
            }
        }

        // 数据存储成功后并且有生成的主键，则将数据异步写入到缓存中
        if (count > 0 && cachable) {
            doWriteCacheList(keyList, values, schema, writeBehind, args);
        }
        return count;
    }

    private V doGetCacheX(K key, final ProtoSchema schema, boolean writeBehind, Object... args) throws Exception {
        // 先从缓存中获取数据
        V value = doGet(key.getCacheKey(), schema);
        if (value != null) {
            // 数据有缓存，但其实缓存定义的空数据，
            // 此时改成返回空告诉业务方是没有数据的，只是命中了缓存
            // 当该KEY新增时缓存会更新，不会导致一直缓存不存在的数据
            if (!checkExist(value)) {
                return null;
            }
            statistics.addHitCount();
            return value;
        }
        // 缓存数据不存在，从Dao回源加载数据
        int index = key.getLockIndex();
        if (index > 0 && lock != null) {
            try {
                // 加锁操作，避免同一时间有多个请求涌进
                lock.writeLock(index);
                // 双重检测，继续判断缓存中是否存在
                value = doGet(key.getCacheKey(), schema);
                if (value != null) {
                    return value;
                }
                value = loader.load(key, schema, template, args);
            } finally {
                lock.writeUnlock(index);
            }
        } else {
            // 不加锁操作，可以提升性能，但多个请求进来会多次读取数据库，加重数据库负担
            value = loader.load(key, schema, template, args);
        }
        // 将数据库中查询的数据添加到缓存中以便于下次直接从缓存获取
        if (value != null) {
            doWriteCache(key, value, schema, writeBehind, args);
        }
        statistics.addMissCount();
        // 数据有缓存，但其实缓存的是定义的空数据，
        // 此时改成返回空告诉业务方是没有数据的，只是命中了缓存
        // 当该KEY新增时缓存会更新，不会导致一直缓存不存在的数据
        if (!checkExist(value)) {
            return null;
        }
        return value;
    }

    /**
     * 从缓存中获取数据列表，因为有可能获取的数据列表有长度，但里面的数据为NULL，
     * 针对数据为NULL的不存在的缓存数据，需要筛选出来并重新从数据库中加载
     * 注意：
     * 因为从数据库查询如果还不存在数据，所以业务的loader实现最好在判断数据库返回为null时返回对应的空对象，
     * 例如ParamCacheX.DATA_NOT_FOUND，避免不存在的数据一直落盘查询
     */
    private Table<V> doGetCacheX(List<K> keys, ProtoSchema schema, boolean writeBehind, Object[] args) throws Exception {
        // 以第一个CacheKey作为缓存Key依据
        K currentKey = keys.get(0);
        List<String> keyList = new LinkedList<String>();
        for (K k : keys) {
            keyList.add(k.getCacheKey());
        }
        // 先从缓存中获取数据
        Table<V> values = doGetList(keyList, schema);
        // 筛选缓存中存在和不存在的数据，首次请求时数据都是不存在
        List<Pair<Integer, K>> missList = new LinkedList<Pair<Integer, K>>();
        for (int i = 0; i < keys.size(); i++) {
            K k = keys.get(i);
            V v = values.get(i);
            if (v != null) {
                // 数据有缓存，但其实缓存定义的空数据，
                // 此时改成返回空告诉业务方是没有数据的，只是命中了缓存
                // 当该KEY新增时缓存会更新，不会导致一直缓存不存在的数据
                if (!checkExist(v)) {
                    values.set(i, null);
                }
                statistics.addHitCount();
            } else {
                missList.add(new Pair<Integer, K>(i, k));
                statistics.addMissCount();
            }
        }
        // 如果数据在缓存中都存在则直接返回
        if (missList.size() <= 0) {
            return values;
        }

        // 部分或者全部缓存数据不存在，从Dao回源加载数据，此时会触发1+N查询
        int index = currentKey.getLockIndex();
        List<String> addKeyList = new LinkedList<String>();
        List<V> addValueList = new LinkedList<V>();
        if (index > 0 && lock != null) {
            try {
                // 加锁操作，避免同一时间有多个请求涌进
                lock.writeLock(index);
                for (Pair<Integer, K> current : missList) {
                    K key = current.value();
                    V value = loader.load(key, schema, template, args);
                    // 空数据或者NULL不存储进返回列表里面，避免列表中返回空的数据导致业务获取数据异常
                    if (checkExist(value)) {
                        values.set(current.key(), value);
                    }
                    // 空数据也可以做缓存提升命中率减少落库查询，只是不返回空数据出去而已
                    if (value != null) {
                        addKeyList.add(key.getCacheKey());
                        addValueList.add(value);
                    }
                }
            } finally {
                lock.writeUnlock(index);
            }
        } else {
            // 不加锁操作，可以提升性能，但多个请求进来会多次读取数据库，加重数据库负担
            for (Pair<Integer, K> current : missList) {
                K key = current.value();
                V value = loader.load(key, schema, template, args);
                // 空数据或者NULL不存储进返回列表里面，避免列表中返回空的数据导致业务获取数据异常
                if (checkExist(value)) {
                    values.set(current.key(), value);
                }
                // 空数据也可以做缓存，只是不返回空数据出去而已
                if (value != null) {
                    addKeyList.add(key.getCacheKey());
                    addValueList.add(value);
                }
            }
        }

        // 将数据库中查询的数据添加到缓存中以便于下次直接从缓存获取
        if (!addKeyList.isEmpty()) {
            doWriteCacheList(addKeyList, addValueList, schema, writeBehind, args);
        }
        return values;
    }

    private V doSelectCacheX(CacheKey<?> key, Query query, ProtoSchema schema,
                             boolean writeBehind, Object... args) throws Exception {
        // 先从缓存中获取数据
        V value = null;
        if (!StrUtil.isEmpty(key.getCacheKey())) {
            value = doGet(key.getCacheKey(), schema);
            if (value != null) {
                // 数据有缓存，但其实缓存定义的空数据，
                // 此时改成返回空告诉业务方是没有数据的，只是命中了缓存
                // 当该KEY新增时缓存会更新，不会导致一直缓存不存在的数据
                if (!checkExist(value)) {
                    return null;
                }
                statistics.addHitCount();
                return value;
            }
        }
        // 缓存数据不存在，从Dao回源加载数据
        int index = key.getLockIndex();
        if (index > 0 && lock != null) {
            try {
                // 加锁操作，避免同一时间有多个请求涌进
                lock.writeLock(index);
                // 双重检测，继续判断缓存中是否存在
                if (!StrUtil.isEmpty(key.getCacheKey())) {
                    value = doGet(key.getCacheKey(), schema);
                    if (value != null) {
                        return value;
                    }
                }
                value = loader.select(key, query, schema, template, args);
            } finally {
                lock.writeUnlock(index);
            }
        } else {
            // 不加锁操作，可以提升性能，但多个请求进来会多次读取数据库，加重数据库负担
            value = loader.select(key, query, schema, template, args);
        }
        // 将数据库中查询的数据添加到缓存中以便于下次直接从缓存获取
        if (value != null) {
            if (!StrUtil.isEmpty(key.getCacheKey())) {
                doWriteCache(key, value, schema, writeBehind, args);
            }
        }
        statistics.addMissCount();
        // 数据有缓存，但其实缓存的是定义的空数据，
        // 此时改成返回空告诉业务方是没有数据的，只是命中了缓存
        // 当该KEY新增时缓存会更新，不会导致一直缓存不存在的数据
        if (!checkExist(value)) {
            return null;
        }
        return value;
    }

    private Table<V> doQueryCacheX(CacheKey<?> key, Query query, ProtoSchema schema,
                boolean writeBehind, Object[] args) throws Exception {
        // 先从缓存中获取数据
        Table<V> values = null;
        if (!StrUtil.isEmpty(key.getCacheKey())) {
            values = doGetList(key.getCacheKey(), schema);
            if (values != null) {
                statistics.addHitCount();
                return values;
            }
        }
        // 缓存数据不存在，从Dao回源加载数据
        int index = key.getLockIndex();
        if (index > 0 && lock != null) {
            try {
                // 加锁操作，避免同一时间有多个请求涌进
                lock.writeLock(index);
                // 双重检测，继续判断缓存中是否存在
                if (!StrUtil.isEmpty(key.getCacheKey())) {
                    values = doGetList(key.getCacheKey(), schema);
                    if (values != null) {
                        return values;
                    }
                }
                values = loader.query(key, query, schema, template, args);
            } finally {
                lock.writeUnlock(index);
            }
        } else {
            // 不加锁操作，可以提升性能，但多个请求进来会多次读取数据库，加重数据库负担
            values = loader.query(key, query, schema, template, args);
        }
        // 将数据库中查询的数据添加到缓存中以便于下次直接从缓存获取
        if (values != null) {
            if (!StrUtil.isEmpty(key.getCacheKey())) {
                doWriteCacheList(key, values, schema, writeBehind, args);
            }
        }
        statistics.addMissCount();
        return values;
    }

    @SuppressWarnings("unchecked")
    private int doReplaceCacheX(K key, V value, ProtoSchema schema, boolean writeBehind, Object... args) throws Exception {
        // 先从缓存中移除数据，
        // 不更新缓存数据，操作比较复杂，下次获取数据如果没有直接回源即可
        boolean success = cache.remove(key.getCacheKey());
        if (!success) {
            // 缓存删除失败直接退出，
            // 不允许缓存删除不成功数据库却更新成功，会有脏数据
            return -1;
        }
        // 先将数据存储到数据库中
        Ref<Object> idRef = new Ref<Object>();
        int count = 0;
        int index = key.getLockIndex();
        if (index > 0 && lock != null) {
            try {
                // 加锁操作，避免同一时间有多个请求涌进
                lock.writeLock(index);
                // 把获取数据库连接的操作放到锁操作之后避免大量请求进来争锁，一开始就申请数据库连接占用资源
                count = loader.replace(key, value, schema, template, idRef, args);
                if (count > 0 && idRef.value() != null) {
                    key.setPrimary(idRef.value());
                }
            } finally {
                lock.writeUnlock(index);
            }
        } else {
            // 不加锁操作，可以提升性能，但多个相同请求会有数据覆盖风险
            count = loader.replace(key, value, schema, template, idRef, args);
            if (count > 0 && idRef.value() != null) {
                key.setPrimary(idRef.value());
            }
        }
        // 再删除缓存，
        // 因为有可能在高并发情况下该方法在一开始删除了缓存，但同时又有另外的请求又重新加载了缓存
        if (count != -1) {
            cache.remove(key.getCacheKey());
        }
        return count;
    }

    private int doDeleteCacheX(K key, Object... args) throws Exception {
        // 先从缓存中移除数据
        boolean success = cache.remove(key.getCacheKey());
        if (!success) {
            // 缓存删除失败直接退出，
            // 不允许缓存删除不成功数据库却删除成功，会有脏数据
            return -1;
        }

        // 再从数据库中删除数据
        int count = -1;
        int index = key.getLockIndex();
        if (index > 0 && lock != null) {
            try {
                // 加锁操作，避免同一时间有多个请求涌进
                lock.writeLock(index);
                count = loader.delete(key, template, args);
            } finally {
                lock.writeUnlock(index);
            }
        } else {
            // 不加锁操作，可以提升性能，但多个请求进来会多次删除数据库
            count = loader.delete(key, template, args);
        }
        // 再删除缓存，
        // 因为有可能要高并发情况下该方法在一开始删除了缓存，但同时又有另外的请求又重新加载了缓存
        if (count != -1) {
            cache.remove(key.getCacheKey());
        }
        return count;
    }

    private int doDeleteCacheX(List<K> keys, Object... args) throws Exception {
        int count = 0;
        // 先从缓存中移除数据
        List<String> cacheKeys = new ArrayList<String>(keys.size());
        for (K key : keys) {
            cacheKeys.add(key.getCacheKey());
        }
        boolean success = cache.remove(cacheKeys);
        if (!success) {
            // 缓存删除失败直接退出，
            // 不允许缓存删除不成功数据库却删除成功，会有脏数据
            return -1;
        }
        int index = -1;
        if (!keys.isEmpty()) {
            index = keys.get(0).getLockIndex();
        }
        if (index > 0 && lock != null) {
            try {
                // 加锁操作，避免同一时间有多个请求涌进
                lock.writeLock(index);
                count = loader.delete(keys, template, args);
            } finally {
                lock.writeUnlock(index);
            }
        } else {
            // 不加锁操作，可以提升性能，但多个请求进来会多次删除数据库
            count = loader.delete(keys, template, args);
        }
        // 再删除缓存，
        // 因为有可能要高并发情况下该方法在一开始删除了缓存，但同时又有另外的请求又重新加载了缓存
        if (count != -1) {
            cache.remove(cacheKeys);
        }
        return count;
    }

    private int doDeleteCacheX(CacheKey<?> key, Where where, Object... args) throws Exception {
        // 直接从数据库中删除数据，注意业务需要自己维护主键缓存的清除保证数据的一致性
        int count = -1;
        int index = key.getLockIndex();
        if (index > 0 && lock != null) {
            try {
                // 加锁操作，避免同一时间有多个请求涌进
                lock.writeLock(index);
                count = loader.delete(key, where, template, args);
            } finally {
                lock.writeUnlock(index);
            }
        } else {
            // 不加锁操作，可以提升性能，但多个请求进来会多次删除数据库
            count = loader.delete(key, where, template, args);
        }
        // 再删除缓存，
        // 因为有可能要高并发情况下该方法在一开始删除了缓存，但同时又有另外的请求又重新加载了缓存
        if (count != -1) {
            if (!StrUtil.isEmpty(key.getCacheKey())) {
                cache.remove(key.getCacheKey());
            }
        }
        return count;
    }

    private int doDeleteCacheX(List<CacheKey<?>> keys, Where where, Object... args) throws Exception {
        // 直接从数据库中删除数据，注意业务需要自己维护主键缓存的清除保证数据的一致性
        int count = -1;
        // 先从缓存中移除数据
        List<String> cacheKeys = new ArrayList<String>(keys.size());
        for (CacheKey<?> key : keys) {
            cacheKeys.add(key.getCacheKey());
        }
        boolean success = cache.remove(cacheKeys);
        if (!success) {
            // 缓存删除失败直接退出，
            // 不允许缓存删除不成功数据库却删除成功，会有脏数据
            return -1;
        }
        int index = -1;
        if (!keys.isEmpty()) {
            index = keys.get(0).getLockIndex();
        }
        if (index > 0 && lock != null) {
            try {
                // 加锁操作，避免同一时间有多个请求涌进
                lock.writeLock(index);
                count = loader.delete(keys, where, template, args);
            } finally {
                lock.writeUnlock(index);
            }
        } else {
            // 不加锁操作，可以提升性能，但多个请求进来会多次删除数据库
            count = loader.delete(keys, where, template, args);
        }
        // 再删除缓存，
        // 因为有可能要高并发情况下该方法在一开始删除了缓存，但同时又有另外的请求又重新加载了缓存
        if (count != -1) {
            cache.remove(cacheKeys);
        }
        return count;
    }

    private int doUpdateCacheX(K key, V value, ProtoSchema schema, Object... args) throws Exception {
        // 先从缓存中移除数据，
        // 不更新缓存数据，操作比较复杂，下次获取数据如果没有直接回源即可
        if (!StrUtil.isEmpty(key.getCacheKey())) {
            boolean success = cache.remove(key.getCacheKey());
            if (!success) {
                // 缓存删除失败直接退出，
                // 不允许缓存删除不成功数据库却更新成功，会有脏数据
                return -1;
            }
        }
        // 再从数据库中更新数据
        int count = -1;
        int index = key.getLockIndex();
        if (index > 0 && lock != null) {
            try {
                // 加锁操作，避免同一时间有多个请求涌进
                lock.writeLock(index);
                count = loader.update(key, value, schema, template, args);
            } finally {
                lock.writeUnlock(index);
            }
        } else {
            // 不加锁操作，可以提升性能，但多个请求进来会多次更新数据库
            count = loader.update(key, value, schema, template, args);
        }
        // 再删除缓存，
        // 因为有可能要高并发情况下该方法在一开始删除了缓存，但同时又有另外的请求又重新加载了缓存
        if (count != -1) {
            if (!StrUtil.isEmpty(key.getCacheKey())) {
                cache.remove(key.getCacheKey());
            }
        }
        return count;
    }

    private int doUpdateCacheX(List<K> keys, List<V> values, ProtoSchema schema, Object... args) throws Exception {
        // 先从缓存中批量移除数据，
        // 不更新缓存数据，操作比较复杂，下次获取数据如果没有直接回源即可
        List<String> cacheKeys = new ArrayList<String>(keys.size());
        for (K key : keys) {
            cacheKeys.add(key.getCacheKey());
        }
        boolean success = cache.remove(cacheKeys);
        if (!success) {
            // 缓存删除失败直接退出，
            // 不允许缓存删除不成功数据库却更新成功，会有脏数据
            return -1;
        }
        // 再从数据库中批量更新数据
        int count = -1;
        K key = keys.get(0);
        int index = key.getLockIndex();
        if (index > 0 && lock != null) {
            try {
                // 加锁操作，避免同一时间有多个请求涌进
                lock.writeLock(index);
                count = loader.update(keys, values, schema, template, args);
            } finally {
                lock.writeUnlock(index);
            }
        } else {
            // 不加锁操作，可以提升性能，但多个请求进来会多次更新数据库
            count = loader.update(keys, values, schema, template, args);
        }
        // 再删除缓存，
        // 因为有可能要高并发情况下该方法在一开始删除了缓存，但同时又有另外的请求又重新加载了缓存
        if (count != -1) {
            cache.remove(cacheKeys);
        }
        return count;
    }

    private int doUpdateCacheX(CacheKey<?> key, Updater updater, ProtoSchema schema, Object... args) throws Exception {
        // 先从缓存中移除数据，
        // 不更新缓存数据，操作比较复杂，下次获取数据如果没有直接回源即可
        if (!StrUtil.isEmpty(key.getCacheKey())) {
            boolean success = cache.remove(key.getCacheKey());
            if (!success) {
                // 缓存删除失败直接退出，
                // 不允许缓存删除不成功数据库却更新成功，会有脏数据
                return -1;
            }
        }
        // 再从数据库中更新数据
        int count = -1;
        int index = key.getLockIndex();
        if (index > 0 && lock != null) {
            try {
                // 加锁操作，避免同一时间有多个请求涌进
                lock.writeLock(index);
                count = loader.update(key, updater, schema, template, args);
            } finally {
                lock.writeUnlock(index);
            }
        } else {
            // 不加锁操作，可以提升性能，但多个请求进来会多次更新数据库
            count = loader.update(key, updater, schema, template, args);
        }
        // 再删除缓存，
        // 因为有可能要高并发情况下该方法在一开始删除了缓存，但同时又有另外的请求又重新加载了缓存
        if (count != -1) {
            if (!StrUtil.isEmpty(key.getCacheKey())) {
                cache.remove(key.getCacheKey());
            }
        }
        return count;
    }

    private int doUpdateCacheX(List<CacheKey<?>> keys, Updater updater, ProtoSchema schema, Object... args) throws Exception {
        // 先从缓存中批量移除数据，
        // 不更新缓存数据，操作比较复杂，下次获取数据如果没有直接回源即可
        List<String> cacheKeys = new ArrayList<String>(keys.size());
        for (CacheKey<?> key : keys) {
            cacheKeys.add(key.getCacheKey());
        }
        boolean success = cache.remove(cacheKeys);
        if (!success) {
            // 缓存删除失败直接退出，
            // 不允许缓存删除不成功数据库却更新成功，会有脏数据
            return -1;
        }
        // 再从数据库中批量更新数据
        int count = -1;
        CacheKey<?> key = keys.get(0);
        int index = key.getLockIndex();
        if (index > 0 && lock != null) {
            try {
                // 加锁操作，避免同一时间有多个请求涌进
                lock.writeLock(index);
                count = loader.update(keys, updater, schema, template, args);
            } finally {
                lock.writeUnlock(index);
            }
        } else {
            // 不加锁操作，可以提升性能，但多个请求进来会多次更新数据库
            count = loader.update(keys, updater, schema, template, args);
        }
        // 再删除缓存，
        // 因为有可能要高并发情况下该方法在一开始删除了缓存，但同时又有另外的请求又重新加载了缓存
        if (count != -1) {
            cache.remove(cacheKeys);
        }
        return count;
    }

    @SuppressWarnings("unchecked")
    private int doHputCacheX(K key, Object field, V value, ProtoSchema schema, boolean writeBehind, Object... args) throws Exception {
        // 先将数据存储到数据库中
        int index = key.getLockIndex();
        Ref<Object> idRef = new Ref<Object>();
        int count = 0;
        if (index > 0 && lock != null) {
            try {
                // 加锁操作，避免同一时间有多个请求涌进
                lock.writeLock(index);
                count = loader.hadd(key, field, value, schema, template, idRef, args);
                if (count > 0 && idRef.value() != null) {
                    key.setPrimary(idRef.value());
                }
            } finally {
                lock.writeUnlock(index);
            }
        } else {
            // 不加锁操作，可以提升性能，但多个相同请求会有数据覆盖风险
            count = loader.hadd(key, field, value, schema, template, idRef, args);
            if (count > 0 && idRef.value() != null) {
                key.setPrimary(idRef.value());
            }
        }
        // 数据存储成功后再将数据异步写入到缓存中
        if (count > 0) {
            doWriteCacheH(key, field, value, schema, writeBehind, args);
        }
        return count;
    }

    private V doHgetCacheX(K key, Object field, ProtoSchema schema, boolean writeBehind, Object... args) throws Exception {
        // 先从缓存中获取数据
        V value = doHget(key.getCacheKey(), field, schema);
        if (value != null) {
            // 数据有缓存，但其实缓存定义的空数据，
            // 此时改成返回空告诉业务方是没有数据的，只是命中了缓存
            // 当该KEY新增时缓存会更新，不会导致一直缓存不存在的数据
            if (!checkExist(value)) {
                return null;
            }
            statistics.addHitCount();
            return value;
        }
        statistics.addMissCount();

        // 缓存数据不存在，从Dao回源加载数据
        int index = key.getLockIndex();
        final List<V> values;
        if (index > 0 && lock != null) {
            try {
                // 加锁操作，避免同一时间有多个请求涌进
                lock.writeLock(index);
                // 双重检测，继续判断缓存中是否存在
                value = doHget(key.getCacheKey(), field, schema);
                if (value != null) {
                    return value;
                }
                values = loader.hload(key, schema, template, args);
            } finally {
                lock.writeUnlock(index);
            }
        } else {
            // 不加锁操作，可以提升性能，但多个请求进来会多次读取数据库，加重数据库负担
            values = loader.hload(key, schema, template, args);
        }
        if (values == null) {
            return null;
        }
        statistics.addMissCount();
        doWriteCacheHAll(key, values, schema, writeBehind, args);
        value = doHget(key.getCacheKey(), field, schema);
        // 数据有缓存，但其实缓存定义的空数据，
        // 此时改成返回空告诉业务方是没有数据的，只是命中了缓存
        // 当该KEY新增时缓存会更新，不会导致一直缓存不存在的数据
        if (!checkExist(value)) {
            return null;
        }
        return value;
    }

    private List<V> doHgetAllCacheX(K key, ProtoSchema schema, Object... args) throws Exception {
        // 先从缓存中获取数据
        List<V> values = doHgetAll(key.getCacheKey(), schema);
        if (values != null) {
            statistics.addHitCount();
            return values;
        }
        // 缓存数据不存在，从Dao回源加载数据
        int index = key.getLockIndex();
        if (index > 0 && lock != null) {
            try {
                // 加锁操作，避免同一时间有多个请求涌进
                lock.writeLock(index);
                // 双重检测，继续判断缓存中是否存在
                values = doHgetAll(key.getCacheKey(), schema, args);
                if (values != null) {
                    return values;
                }
                values = loader.hload(key, schema, template, args);
            } finally {
                lock.writeUnlock(index);
            }
        } else {
            // 不加锁操作，可以提升性能，但多个请求进来会多次读取数据库，加重数据库负担
            values = loader.hload(key, schema, template, args);
        }
        if (values == null) {
            return null;
        }
        doWriteCacheHAll(key, values, schema, config.isWriteBehind(), args);
        statistics.addMissCount();
        return values;
    }

    private int doHdeleteCacheX(K key, Object field, Object... args) throws Exception {
        if (key == null || field == null) {
            return -1;
        }

        // 先设置缓存过期时间，之后这么操作的原因有：
        // 1、不能简单先删除Key，因为有可能该二级缓存的数据量很大，只删除一级Key代价高
        // 2、不能简单先删除二级Key，因为有可能该二级缓存删除之后，但数据库没删除成功或者服务被重启了，缓存出现脏数据
        // 3、设置短时间的TTL即使在数据库删除成功，但下一步删除缓存服务被重启后依然可以在TTL过期之后删除，
        // 不过注意数据删除成功后还没删除缓存而服务被重启这期间可能有脏数据，只能做到数据最终一致
        int preExpire = cache.expire(key.getCacheKey(), DEAFULT_CACHE_EXPIRE_TIME);
        // 先从缓存中移除数据
        int count = -1;
        // 再从数据库中删除数据
        int index = key.getLockIndex();
        if (index > 0 && lock != null) {
            try {
                // 加锁操作，避免同一时间有多个请求涌进
                lock.writeLock(index);
                count = loader.hdelete(key, field, template, args);
            } finally {
                lock.writeUnlock(index);
            }
        } else {
            // 不加锁操作，可以提升性能，但多个请求进来会多次删除数据库
            count = loader.hdelete(key, field, template, args);
        }
        if (count == -1) {
            // 数据库删除失败，还原之前的过期时间
            cache.expire(key.getCacheKey(), preExpire);
        } else {
            // 数据库删除成功，同时删除二级缓存Key
            cache.remove(key.getCacheKey(), field.toString());
        }
        return count;
    }

    private int doHdeleteCacheX(K key, String[] fields, Object... args) throws Exception {
        if (key == null || fields == null) {
            return -1;
        }

        // 先设置缓存过期时间，之后这么操作的原因有：
        // 1、不能简单先删除Key，因为有可能该二级缓存的数据量很大，只删除一级Key代价高
        // 2、不能简单先删除二级Key，因为有可能该二级缓存删除之后，在数据库没删除成功或者服务被重启了，缓存出现脏数据
        // 3、设置短时间的TTL即使在数据库删除成功，但下一步删除缓存服务被重启后依然可以在TTL过期之后删除，
        // 不过注意数据删除成功后还没删除缓存而服务被重启这期间可能有脏数据，只能做到数据最终一致
        int preExpire = cache.expire(key.getCacheKey(), DEAFULT_CACHE_EXPIRE_TIME);
        // 再从数据库中删除数据
        int count = -1;
        int index = key.getLockIndex();
        if (index > 0 && lock != null) {
            try {
                // 加锁操作，避免同一时间有多个请求涌进
                lock.writeLock(index);
                count = loader.hdelete(key, fields, template, args);
            } finally {
                lock.writeUnlock(index);
            }
        } else {
            // 不加锁操作，可以提升性能，但多个请求进来会多次删除数据库
            count = loader.hdelete(key, fields, template, args);
        }
        if (count == -1) {
            // 数据库删除失败，还原之前的过期时间
            cache.expire(key.getCacheKey(), preExpire);
        } else {
            // 数据库删除成功，同时删除二级缓存Key
            cache.remove(key.getCacheKey(), fields);
        }
        return count;
    }

    private int doHupdateCacheX(K key, Object field, V value, ProtoSchema schema, Object... args) throws Exception {
        if (key == null || field == null || value == null) {
            return -1;
        }

        // 先设置缓存过期时间，之后这么操作的原因有：
        // 1、不能简单先删除Key，因为有可能该二级缓存的数据量很大，只删除一级Key代价高
        // 2、不能简单先删除二级Key，因为有可能该二级缓存删除之后，但数据库没更新成功或者服务被重启了，缓存出现脏数据
        // 3、不能简单先更新二级Key，因为有可能该二级缓存更新之后，但数据库没更新成功或者服务被重启了，缓存出现脏数据
        // 4、设置短时间的TTL即使在数据库删除成功，但下一步删除缓存服务被重启后依然可以在TTL过期之后删除，
        // 不过注意数据更新成功后还没删除缓存而服务被重启这期间可能有脏数据，只能做到数据最终一致
        int preExpire = cache.expire(key.getCacheKey(), DEAFULT_CACHE_EXPIRE_TIME);
        // 再从数据库中更新数据
        int count = -1;
        int index = key.getLockIndex();
        if (index > 0 && lock != null) {
            try {
                // 加锁操作，避免同一时间有多个请求涌进
                lock.writeLock(index);
                count = loader.hupdate(key, field, value, schema, template, args);
            } finally {
                lock.writeUnlock(index);
            }
        } else {
            // 不加锁操作，可以提升性能，但多个请求进来会多次更新数据库
            count = loader.hupdate(key, field, value, schema, template, args);
        }
        if (count == -1) {
            // 数据库删除失败，还原之前的过期时间
            cache.expire(key.getCacheKey(), preExpire);
        } else {
            // 数据库删除成功，同时删除二级缓存Key
            cache.remove(key.getCacheKey(), field.toString());
        }
        return count;
    }

    /**
     * 将数据库中取出的数据写入到缓存中
     *
     * @param writeBehind 是否采用异步写，如果上层接口已经是异步了则参数为FALSE
     */
    private void doWriteCache(CacheKey<?> key, V value, ProtoSchema schema, boolean writeBehind, Object... args) {
        // 缓存异步/同步写
        if (writeBehind && threadPool != null) {
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    doPut(key.getCacheKey(), value, schema, args);
                }
            });
        } else {
            doPut(key.getCacheKey(), value, schema, args);
        }
    }

    private void doWriteCacheList(CacheKey<?> key, List<V> values, ProtoSchema schema, boolean writeBehind, Object... args) {
        // 缓存异步/同步写
        if (writeBehind && threadPool != null) {
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    doPutList(key.getCacheKey(), values, schema, args);
                }
            });
        } else {
            doPutList(key.getCacheKey(), values, schema, args);
        }
    }

    private void doWriteCacheList(List<String> keys, List<V> values, ProtoSchema schema, boolean writeBehind, Object... args) {
        // 缓存异步/同步写
        if (writeBehind && threadPool != null) {
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    doPutList(keys, values, schema, args);
                }
            });
        } else {
            doPutList(keys, values, schema, args);
        }
    }

    /**
     * 将数据库中取出的数据写入到缓存中
     */
    private void doWriteCacheH(K key, Object field, V value, ProtoSchema schema, boolean writeBehind, Object... args) {
        // 缓存异步/同步写
        if (writeBehind && threadPool != null) {
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    doHput(key.getCacheKey(), field, value, schema, args);
                }
            });
        } else {
            doHput(key.getCacheKey(), field, value, schema, args);
        }
    }

    /**
     * 将数据库中取出的二级缓存数据写入到缓存中
     */
    private void doWriteCacheHAll(K key, List<V> values, ProtoSchema schema, boolean writeBehind, Object... args) {
        // 缓存异步/同步写
        if (writeBehind && threadPool != null) {
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    doHputAll(key.getCacheKey(), values, schema, args);
                }
            });
        } else {
            doHputAll(key.getCacheKey(), values, schema, args);
        }
    }

    /**
     * 从缓存中存储数据，由具体数据类型类实现
     *
     * @param key    存储Key
     * @param value  存储数据
     * @param schema 数据元信息，用于序列化/反序列化
     * @return 存储成功返回true
     */
    public abstract boolean doPut(String key, V value, ProtoSchema schema, Object... args);

    /**
     * 从缓存中存储数据，由具体数据类型类实现
     *
     * @param key    存储Key
     * @param values 存储数据集
     * @param schema 数据元信息，用于序列化/反序列化
     * @return 存储成功返回true
     */
    protected abstract boolean doPutList(String key, List<V> values, ProtoSchema schema, Object... args);

    /**
     * 从缓存中存储数据，由具体数据类型类实现
     *
     * @param keys   存储Key列表
     * @param values 存储数据集
     * @param schema 数据元信息，用于序列化/反序列化
     * @return 存储成功返回true
     */
    protected abstract boolean doPutList(List<String> keys, List<V> values, ProtoSchema schema, Object... args);

    /**
     * 从缓存中存储二级数据，由具体数据类型类实现
     *
     * @param key    存储Key
     * @param value  存储数据
     * @param field  缓存二级Key
     * @param schema 数据元信息，用于序列化/反序列化
     * @return 存储成功返回true
     */
    public abstract boolean doHput(String key, Object field, V value, ProtoSchema schema, Object... args);

    /**
     * 从缓存中存储二级数据，由具体数据类型类实现
     *
     * @param key    存储Key
     * @param value  存储数据
     * @param schema 数据元信息，用于序列化/反序列化
     * @return 存储成功返回true
     */
    public abstract boolean doHputAll(String key, List<V> value, ProtoSchema schema, Object... args);

    /**
     * 从缓存中获取数据，由具体数据类型类实现
     *
     * @param key    存储Key
     * @param schema 数据元信息，用于序列化/反序列化
     */
    public abstract V doGet(String key, ProtoSchema schema, Object... args);

    /**
     * 从缓存中获取数据集，由具体数据类型类实现
     *
     * @param key    存储Key
     * @param schema 数据元信息，用于序列化/反序列化
     */
    public abstract Table<V> doGetList(String key, ProtoSchema schema, Object... args);

    /**
     * 从缓存中获取数据集，由具体数据类型类实现
     *
     * @param keys   存储Key列表
     * @param schema 数据元信息，用于序列化/反序列化
     */
    public abstract Table<V> doGetList(List<String> keys, ProtoSchema schema, Object... args);

    /**
     * 判断获取的数据是否为空，
     * 同时也用于当数据库查询不存在时也缓存对应的空数据，避免数据库不存在数据时一直落库查询，
     * 实例可参考ParamCacheX#checkExist及ParamCacheX#DATA_NOT_FOUND
     *
     * @param value 通过{@link #doGet(String, ProtoSchema, Object...)}获取的数据
     */
    public abstract boolean checkExist(V value);

    /**
     * 从缓存中获取二级缓存数据，由具体数据类型类实现
     *
     * @param key    存储Key
     * @param field  缓存二级Key
     * @param schema 数据元信息，用于序列化/反序列化
     */
    public abstract V doHget(String key, Object field, ProtoSchema schema, Object... args);

    /**
     * 判断获取的二级数据是否为空
     *
     * @param value 通过{@link #doHget(String, Object, ProtoSchema, Object...)}获取的数据
     */
    public abstract boolean checkHExist(V value);

    /**
     * 从缓存中获取所有二级缓存数据，由具体数据类型类实现
     *
     * @param key    存储Key
     * @param schema 数据元信息，用于序列化/反序列化
     */
    public abstract Table<V> doHgetAll(String key, ProtoSchema schema, Object... args);
}
