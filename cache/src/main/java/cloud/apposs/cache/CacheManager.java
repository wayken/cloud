package cloud.apposs.cache;

import cloud.apposs.cache.jvm.JvmCache;
import cloud.apposs.cache.redis.RedisCache;
import cloud.apposs.protobuf.ProtoBuf;
import cloud.apposs.protobuf.ProtoSchema;
import cloud.apposs.util.Param;
import cloud.apposs.util.Table;

import java.util.List;
import java.util.Map;

/**
 * 缓存管理器，
 * 为不同的缓存方案提供统一的接口调用，包括
 * <pre>
 * 1、采用JVM内存缓存
 * 2、采用Redis分布缓存
 * 3、采用Memcache分布缓存
 * </pre>
 * 支持一键式切换不同的缓存存储，
 * 即统一的缓存API调用，底层用JVM/Redis则通过配置一键更换
 */
public final class CacheManager {
    private final CacheConfig config;

    private final Cache cache;

    public CacheManager(CacheConfig config) {
        this(config.getType(), config);
    }

    public CacheManager(String type, CacheConfig config) {
        this.config = config;
        this.cache = getCache(type, config);
    }

    /**
     * 获取指定缓存接口
     *
     * @param type 缓存类型
     * @return 缓存接口
     */
    public static Cache getCache(String type, CacheConfig config) {
        if (Cache.CACHE_JVM.equals(type)) {
            return new JvmCache(config);
        } else if (Cache.CACHE_REDIS.equals(type)) {
            return new RedisCache(config);
        }
        return new JvmCache(config);
    }

    public CacheConfig getConfig() {
        return config;
    }

    public boolean exist(String key) {
        return cache.exists(key);
    }

    public int expire(String key, int expirationTime) {
        return cache.expire(key, expirationTime);
    }

    public ProtoBuf get(String key) {
        return cache.get(key);
    }

    public String getString(String key) {
        return cache.getString(key);
    }

    public Integer getInt(String key) {
        return cache.getInt(key);
    }

    public Long getLong(String key) {
        return cache.getLong(key);
    }

    public Short getShort(String key) {
        return cache.getShort(key);
    }

    public Float getFloat(String key) {
        return cache.getFloat(key);
    }

    public Double getDouble(String key) {
        return cache.getDouble(key);
    }

    public ProtoBuf getBuffer(String key) {
        return cache.getBuffer(key);
    }

    public byte[] getBytes(String key) {
        return cache.getBytes(key);
    }

    public List<?> getList(String key, ProtoSchema schema) {
        return cache.getList(key, schema);
    }

    public Map<?, ?> getMap(String key, ProtoSchema schema) {
        return cache.getMap(key, schema);
    }

    public <T> T getObject(String key, Class<T> clazz, ProtoSchema schema) {
        return cache.getObject(key, clazz, schema);
    }

    public Param getParam(String key, ProtoSchema schema) {
        return cache.getParam(key, schema);
    }

    public Table<?> getTable(String key, ProtoSchema schema) {
        return cache.getTable(key, schema);
    }

    public List<ProtoBuf> getBufferList(List<String> keys) {
        return cache.getBufferList(keys);
    }

    public boolean put(String key, ProtoBuf value) {
        return cache.put(key, value);
    }

    public boolean put(String key, ProtoBuf value, boolean compact) {
        return cache.put(key, value, compact);
    }

    public boolean put(String key, String value) {
        return cache.put(key, value);
    }

    public boolean put(String key, int value) {
        return cache.put(key, value);
    }

    public boolean put(String key, boolean value) {
        return cache.put(key, value);
    }

    public boolean put(String key, long value) {
        return cache.put(key, value);
    }

    public boolean put(String key, short value) {
        return cache.put(key, value);
    }

    public boolean put(String key, double value) {
        return cache.put(key, value);
    }

    public boolean put(String key, float value) {
        return cache.put(key, value);
    }

    public boolean put(String key, byte[] value) {
        return cache.put(key, value);
    }

    public boolean put(String key, Object value, ProtoSchema schema) {
        return cache.put(key, value, schema);
    }

    public boolean put(String key, Map<?, ?> value, ProtoSchema schema) {
        return cache.put(key, value, schema);
    }

    public boolean put(String key, List<?> value, ProtoSchema schema) {
        return cache.put(key, value, schema);
    }

    public boolean put(String key, Param value, ProtoSchema schema) {
        return cache.put(key, value, schema);
    }

    public boolean put(String key, Table<?> value, ProtoSchema schema) {
        return cache.put(key, value, schema);
    }

    public boolean put(List<String> keys, List<ProtoBuf> values) {
        return cache.put(keys, values, false);
    }

    public long incr(String key) {
        return cache.incr(key);
    }

    public long incrBy(String key, long value) {
        return cache.incrBy(key, value);
    }

    public long decr(String key) {
        return cache.decr(key);
    }

    public long decrBy(String key, long value) {
        return cache.decrBy(key, value);
    }

    public ProtoBuf hget(String key, String field) {
        return cache.hget(key, field);
    }

    public ProtoBuf hgetBuffer(String key, String field) {
        return cache.hgetBuffer(key, field);
    }

    public String hgetString(String key, String field) {
        return cache.hgetString(key, field);
    }

    public Integer hgetInt(String key, String field) {
        return cache.hgetInt(key, field);
    }

    public Long hgetLong(String key, String field) {
        return cache.hgetLong(key, field);
    }

    public Short hgetShort(String key, String field) {
        return cache.hgetShort(key, field);
    }

    public Double hgetDouble(String key, String field) {
        return cache.hgetDouble(key, field);
    }

    public Float hgetFloat(String key, String field) {
        return cache.hgetFloat(key, field);
    }

    public byte[] hgetBytes(String key, String field) {
        return cache.hgetBytes(key, field);
    }

    public <T> T hgetObject(String key, String field, Class<T> clazz, ProtoSchema schema) {
        return cache.hgetObject(key, field, clazz, schema);
    }

    public Map<?, ?> hgetMap(String key, String field, ProtoSchema schema) {
        return cache.hgetMap(key, field, schema);
    }

    public List<?> hgetList(String key, String field, ProtoSchema schema) {
        return cache.hgetList(key, field, schema);
    }

    public Param hgetParam(String key, String field, ProtoSchema schema) {
        return cache.hgetParam(key, field, schema);
    }

    public Table<?> hgetTable(String key, String field, ProtoSchema schema) {
        return cache.hgetTable(key, field, schema);
    }

    public Map<String, ProtoBuf> hgetBufferMap(String key) {
        return cache.hgetBufferMap(key);
    }

    public List<String> hgetStringList(String key) {
        return cache.hgetStringList(key);
    }

    public Table<String> hgetStringTable(String key) {
        return cache.hgetStringTable(key);
    }

    public <T> List<T> hgetObjectList(String key, Class<T> clazz, ProtoSchema schema) {
        return cache.hgetObjectList(key, clazz, schema);
    }

    public <T> Table<T> hgetObjectTable(String key, Class<T> clazz, ProtoSchema schema) {
        return cache.hgetObjectTable(key, clazz, schema);
    }

    public List<Param> hgetParamList(String key, ProtoSchema schema) {
        return cache.hgetParamList(key, schema);
    }

    public Table<Param> hgetParamTable(String key, ProtoSchema schema) {
        return cache.hgetParamTable(key, schema);
    }

    public List<Table<?>> hgetTableList(String key, ProtoSchema schema) {
        return cache.hgetTableList(key, schema);
    }

    public Table<Table<?>> hgetTableAll(String key, ProtoSchema schema) {
        return cache.hgetTableAll(key, schema);
    }

    public List<String> hgetKeyList(String key) {
        return cache.hgetKeyList(key);
    }

    public boolean hput(String key, String field, ProtoBuf value, boolean compact) {
        return cache.hput(key, field, value, compact);
    }

    public long hlen(String key) {
        return cache.hlen(key);
    }

    public boolean hput(String key, String field, byte[] value) {
        return cache.hput(key, field, value);
    }

    public boolean hput(String key, String field, String value) {
        return cache.hput(key, field, value);
    }

    public boolean hput(String key, String field, int value) {
        return cache.hput(key, field, value);
    }

    public boolean hput(String key, String field, long value) {
        return cache.hput(key, field, value);
    }

    public boolean hput(String key, String field, short value) {
        return cache.hput(key, field, value);
    }

    public boolean hput(String key, String field, double value) {
        return cache.hput(key, field, value);
    }

    public boolean hput(String key, String field, float value) {
        return cache.hput(key, field, value);
    }

    public boolean hput(String key, String field, Object value, ProtoSchema schema) {
        return cache.hput(key, field, value, schema);
    }

    public boolean hput(String key, String field, Map<?, ?> value, ProtoSchema schema) {
        return cache.hput(key, field, value, schema);
    }

    public boolean hput(String key, String field, List<?> value, ProtoSchema schema) {
        return cache.hput(key, field, value, schema);
    }

    public boolean hput(String key, String field, Param value, ProtoSchema schema) {
        return cache.hput(key, field, value, schema);
    }

    public boolean hput(String key, String field, Table<?> value, ProtoSchema schema) {
        return cache.hput(key, field, value, schema);
    }

    public long hincrBy(String key, String field, long value) {
        return cache.hincrBy(key, field, value);
    }

    public boolean hmput(String key, Map<byte[], byte[]> value) {
        return cache.hmput(key, value);
    }

    public boolean remove(String key) {
        return cache.remove(key);
    }

    public boolean remove(String... keys) {
        return cache.remove(keys);
    }

    public boolean remove(List<String> keys) {
        return cache.remove(keys);
    }

    public boolean remove(String key, String field) {
        return cache.remove(key, field);
    }

    public boolean remove(String key, String... fields) {
        return cache.remove(key, fields);
    }

    public boolean remove(String key, List<String> fields) {
        return cache.remove(key, fields);
    }

    public synchronized void shutdown() {
        cache.shutdown();
    }
}
