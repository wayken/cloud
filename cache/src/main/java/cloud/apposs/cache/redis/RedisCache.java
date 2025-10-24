package cloud.apposs.cache.redis;

import cloud.apposs.cache.Cache;
import cloud.apposs.cache.CacheConfig;
import cloud.apposs.cache.CacheConfig;
import cloud.apposs.cache.CacheConfig.RedisConfig;
import cloud.apposs.cache.Cache;
import cloud.apposs.cache.CacheStatistics;
import cloud.apposs.cache.redis.codis.CodisCache;
import cloud.apposs.cache.redis.jedis.RedisCluster;
import cloud.apposs.cache.redis.jedis.RedisSingle;
import cloud.apposs.protobuf.ProtoBuf;
import cloud.apposs.protobuf.ProtoSchema;
import cloud.apposs.util.Param;
import cloud.apposs.util.Table;

import java.util.List;
import java.util.Map;

/**
 * Redis缓存管理，可实现
 * 1、Redis单机缓存管理
 * 2、Redis多机集群管理
 * 3、Codis代理分布管理
 */
public class RedisCache implements Cache {
	private static final long serialVersionUID = 8032359613996111604L;

	/** Redis缓存具体代理实现，有单机、集群、Codis模式等 */
	private final Cache proxy;
	
	public RedisCache(CacheConfig config) {
		RedisConfig redisConfig = config.getRedisConfig();
		int cacheType = redisConfig.getCacheType();
		if (cacheType == RedisConfig.REDIS_CACHE_CLUSTER) {
			proxy = new RedisCluster(config);
		} else if (cacheType == RedisConfig.REDIS_CACHE_CODIS) {
			proxy = new CodisCache(config);
		} else {
			proxy = new RedisSingle(config);
		}
	}

	@Override
	public CacheStatistics getStatistics() {
		return proxy.getStatistics();
	}

	@Override
	public int size() {
		return proxy.size();
	}

	@Override
	public boolean exists(String key) {
		return proxy.exists(key);
	}

	@Override
	public int expire(String key, int expirationTime) {
		return proxy.expire(key, expirationTime);
	}

	@Override
	public ProtoBuf get(String key) {
		return proxy.get(key);
	}
	
	@Override
	public String getString(String key) {
		return proxy.getString(key);
	}

	@Override
	public Integer getInt(String key) {
		return proxy.getInt(key);
	}

	@Override
	public Long getLong(String key) {
		return proxy.getLong(key);
	}

	@Override
	public Short getShort(String key) {
		return proxy.getShort(key);
	}

	@Override
	public Float getFloat(String key) {
		return proxy.getFloat(key);
	}

	@Override
	public Double getDouble(String key) {
		return proxy.getDouble(key);
	}

	@Override
	public ProtoBuf getBuffer(String key) {
		return proxy.getBuffer(key);
	}

	@Override
	public byte[] getBytes(String key) {
		return proxy.getBytes(key);
	}

	@Override
	public List<?> getList(String key, ProtoSchema schema) {
		return proxy.getList(key, schema);
	}

	@Override
	public Map<?, ?> getMap(String key, ProtoSchema schema) {
		return proxy.getMap(key, schema);
	}

	@Override
	public <T> T getObject(String key, Class<T> clazz, ProtoSchema schema) {
		return proxy.getObject(key, clazz, schema);
	}

	@Override
	public Param getParam(String key, ProtoSchema schema) {
		return proxy.getParam(key, schema);
	}

	@Override
	public Table<?> getTable(String key, ProtoSchema schema) {
		return proxy.getTable(key, schema);
	}

	@Override
	public List<String> getStringList(List<String> keys) {
		return proxy.getStringList(keys);
	}

	@Override
	public List<ProtoBuf> getBufferList(List<String> keys) {
		return proxy.getBufferList(keys);
	}

	@Override
	public boolean put(String key, ProtoBuf value) {
		return proxy.put(key, value);
	}

	@Override
	public boolean put(String key, ProtoBuf value, boolean compact) {
		return proxy.put(key, value, compact);
	}

	@Override
	public boolean put(String key, String value) {
		return proxy.put(key, value);
	}

	@Override
	public boolean put(String key, int value) {
		return proxy.put(key, value);
	}

	@Override
	public boolean put(String key, boolean value) {
		return proxy.put(key, value);
	}

	@Override
	public boolean put(String key, long value) {
		return proxy.put(key, value);
	}

	@Override
	public boolean put(String key, short value) {
		return proxy.put(key, value);
	}

	@Override
	public boolean put(String key, double value) {
		return proxy.put(key, value);
	}

	@Override
	public boolean put(String key, float value) {
		return proxy.put(key, value);
	}

	@Override
	public boolean put(String key, byte[] value) {
		return proxy.put(key, value);
	}

	@Override
	public boolean put(String key, Object value, ProtoSchema schema) {
		return proxy.put(key, value, schema);
	}

	@Override
	public boolean put(String key, Map<?, ?> value, ProtoSchema schema) {
		return proxy.put(key, value, schema);
	}

	@Override
	public boolean put(String key, List<?> value, ProtoSchema schema) {
		return proxy.put(key, value, schema);
	}

	@Override
	public boolean put(String key, Param value, ProtoSchema schema) {
		return proxy.put(key, value, schema);
	}

	@Override
	public boolean put(String key, Table<?> value, ProtoSchema schema) {
		return proxy.put(key, value, schema);
	}

	@Override
	public boolean put(List<String> keys, List<String> values) {
		return proxy.put(keys, values);
	}

	@Override
	public boolean put(List<String> keys, List<ProtoBuf> values, boolean compact) {
		return proxy.put(keys, values, compact);
	}

	@Override
	public long incr(String key) {
		return proxy.incr(key);
	}

	@Override
	public long incrBy(String key, long value) {
		return proxy.incrBy(key, value);
	}

	@Override
	public long decr(String key) {
		return proxy.decr(key);
	}

	@Override
	public long decrBy(String key, long value) {
		return proxy.decrBy(key, value);
	}

	@Override
	public ProtoBuf hget(String key, String field) {
		return proxy.hget(key, field);
	}

	@Override
	public long hlen(String key) {
		return proxy.hlen(key);
	}

	@Override
	public ProtoBuf hgetBuffer(String key, String field) {
		return proxy.hgetBuffer(key, field);
	}

	@Override
	public String hgetString(String key, String field) {
		return proxy.hgetString(key, field);
	}

	@Override
	public Integer hgetInt(String key, String field) {
		return proxy.hgetInt(key, field);
	}

	@Override
	public Long hgetLong(String key, String field) {
		return proxy.hgetLong(key, field);
	}

	@Override
	public Short hgetShort(String key, String field) {
		return proxy.hgetShort(key, field);
	}

	@Override
	public Double hgetDouble(String key, String field) {
		return proxy.hgetDouble(key, field);
	}

	@Override
	public Float hgetFloat(String key, String field) {
		return proxy.hgetFloat(key, field);
	}

	@Override
	public byte[] hgetBytes(String key, String field) {
		return proxy.hgetBytes(key, field);
	}

	@Override
	public <T> T hgetObject(String key, String field, Class<T> clazz, ProtoSchema schema) {
		return proxy.hgetObject(key, field, clazz, schema);
	}

	@Override
	public Map<?, ?> hgetMap(String key, String field, ProtoSchema schema) {
		return proxy.hgetMap(key, field, schema);
	}
	
	@Override
	public List<?> hgetList(String key, String field, ProtoSchema schema) {
		return proxy.hgetList(key, field, schema);
	}

	@Override
	public Param hgetParam(String key, String field, ProtoSchema schema) {
		return proxy.hgetParam(key, field, schema);
	}

	@Override
	public Table<?> hgetTable(String key, String field, ProtoSchema schema) {
		return proxy.hgetTable(key, field, schema);
	}

	@Override
	public Map<String, ProtoBuf> hgetBufferMap(String key) {
		return proxy.hgetBufferMap(key);
	}

	@Override
	public List<String> hgetStringList(String key) {
		return proxy.hgetStringList(key);
	}

	@Override
	public Table<String> hgetStringTable(String key) {
		return proxy.hgetStringTable(key);
	}

	@Override
	public <T> List<T> hgetObjectList(String key, Class<T> clazz, ProtoSchema schema) {
		return proxy.hgetObjectList(key, clazz, schema);
	}

	@Override
	public <T> Table<T> hgetObjectTable(String key, Class<T> clazz, ProtoSchema schema) {
		return proxy.hgetObjectTable(key, clazz, schema);
	}

	@Override
	public List<Param> hgetParamList(String key, ProtoSchema schema) {
		return proxy.hgetParamList(key, schema);
	}

	@Override
	public Table<Param> hgetParamTable(String key, ProtoSchema schema) {
		return proxy.hgetParamTable(key, schema);
	}

	@Override
	public List<Table<?>> hgetTableList(String key, ProtoSchema schema) {
		return proxy.hgetTableList(key, schema);
	}

	@Override
	public Table<Table<?>> hgetTableAll(String key, ProtoSchema schema) {
		return proxy.hgetTableAll(key, schema);
	}

	@Override
	public List<String> hgetKeyList(String key) {
		return proxy.hgetKeyList(key);
	}

	@Override
	public boolean hput(String key, String field, ProtoBuf value, boolean compact) {
		return proxy.hput(key, field, value, compact);
	}

	@Override
	public boolean hput(String key, String field, byte[] value) {
		return proxy.hput(key, field, value);
	}

	@Override
	public boolean hput(String key, String field, String value) {
		return proxy.hput(key, field, value);
	}

	@Override
	public boolean hput(String key, String field, int value) {
		return proxy.hput(key, field, value);
	}

	@Override
	public boolean hput(String key, String field, long value) {
		return proxy.hput(key, field, value);
	}

	@Override
	public boolean hput(String key, String field, short value) {
		return proxy.hput(key, field, value);
	}

	@Override
	public boolean hput(String key, String field, double value) {
		return proxy.hput(key, field, value);
	}

	@Override
	public boolean hput(String key, String field, float value) {
		return proxy.hput(key, field, value);
	}

	@Override
	public boolean hput(String key, String field, Object value, ProtoSchema schema) {
		return proxy.hput(key, field, value, schema);
	}

	@Override
	public boolean hput(String key, String field, Map<?, ?> value, ProtoSchema schema) {
		return proxy.hput(key, field, value, schema);
	}

	@Override
	public boolean hput(String key, String field, List<?> value,
			ProtoSchema schema) {
		return proxy.hput(key, field, value, schema);
	}

	@Override
	public boolean hput(String key, String field, Param value, ProtoSchema schema) {
		return proxy.hput(key, field, value, schema);
	}

	@Override
	public boolean hput(String key, String field, Table<?> value, ProtoSchema schema) {
		return proxy.hput(key, field, value, schema);
	}

	@Override
	public boolean hmput(String key, Map<byte[], byte[]> value) {
		return proxy.hmput(key, value);
	}

	@Override
	public long hincrBy(String key, String field, long value) {
		return proxy.hincrBy(key, field, value);
	}

	@Override
	public boolean remove(String key) {
		return proxy.remove(key);
	}

	@Override
	public boolean remove(String... keys) {
		return proxy.remove(keys);
	}

	@Override
	public boolean remove(List<String> keys) {
		return proxy.remove(keys);
	}

	@Override
	public boolean remove(String key, String field) {
		return proxy.remove(key, field);
	}
	
	@Override
	public boolean remove(String key, String... fields) {
		return proxy.remove(key, fields);
	}

	@Override
	public boolean remove(String key, List<String> fields) {
		return proxy.remove(key, fields);
	}

	@Override
	public synchronized void shutdown() {
		proxy.shutdown();
	}
}
