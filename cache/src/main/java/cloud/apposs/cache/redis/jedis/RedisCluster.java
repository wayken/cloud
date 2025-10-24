package cloud.apposs.cache.redis.jedis;

import cloud.apposs.cache.Cache;
import cloud.apposs.cache.CacheConfig;
import cloud.apposs.cache.CacheConfig.RedisConfig;
import cloud.apposs.cache.CacheConfig.RedisConfig.RedisServer;
import cloud.apposs.cache.CacheStatistics;
import cloud.apposs.protobuf.ProtoBuf;
import cloud.apposs.protobuf.ProtoSchema;
import cloud.apposs.util.Param;
import cloud.apposs.util.Table;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

/**
 * Redis多机集群管理
 */
public class RedisCluster implements Cache {
    private static final long serialVersionUID = 7211934533896702648L;

    public static final String REDIS_RESPONSE_OK = "OK";

    private final CacheConfig config;

    /**
     * Jedis集群，内部维护连接池
     */
    private final JedisCluster jedis;

    /**
     * 缓存统计服务
     */
    private final CacheStatistics statistics = new CacheStatistics();

    /**
     * Redis没有获取缓存总数的API，只能自己统计
     */
    private volatile int size;

    private final Random random = new Random();

    public RedisCluster(CacheConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config");
        }
        RedisConfig redisConfig = config.getRedisConfig();
        List<RedisServer> serverList = redisConfig.getServerList();
        if (serverList == null || serverList.isEmpty()) {
            throw new IllegalArgumentException("Redis Server List Not Configed");
        }

        this.config = config;
        // 使用JedisCluster对象，需要一个Set<HostAndPort>参数，Redis节点的列表
        Set<HostAndPort> nodes = new HashSet<HostAndPort>();
        for (RedisServer server : serverList) {
            nodes.add(new HostAndPort(server.getHost(), server.getPort()));
        }
        this.jedis = new JedisCluster(nodes);
    }

    @Override
    public CacheStatistics getStatistics() {
        return statistics;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean exists(String key) {
        return jedis.exists(key);
    }

    @Override
    public int expire(String key, int expirationTime) {
        long expire = jedis.ttl(key);
        jedis.expire(key, expirationTime / 1000);
        return (int) expire;
    }

    @Override
    public ProtoBuf get(String key) {
        if (key == null) {
            return null;
        }

        Charset charset = config.getChrset();
        byte[] value = jedis.get(key.getBytes(charset));
        if (value == null) {
            statistics.addMissCount();
            return null;
        }

        statistics.addHitCount();
        return ProtoBuf.wrap(value);
    }

    @Override
    public ProtoBuf getBuffer(String key) {
        return get(key);
    }

    @Override
    public String getString(String key) {
        if (key == null) {
            return null;
        }

        String value = jedis.get(key);
        if (value == null) {
            statistics.addMissCount();
            return null;
        }

        statistics.addHitCount();
        return value;
    }

    @Override
    public Integer getInt(String key) {
        if (key == null) {
            return null;
        }

        String value = jedis.get(key);
        if (value == null) {
            statistics.addMissCount();
            return null;
        }

        statistics.addHitCount();
        return Integer.valueOf(value);
    }

    @Override
    public Long getLong(String key) {
        if (key == null) {
            return null;
        }

        String value = jedis.get(key);
        if (value == null) {
            statistics.addMissCount();
            return null;
        }

        statistics.addHitCount();
        return Long.valueOf(value);
    }

    @Override
    public Short getShort(String key) {
        if (key == null) {
            return null;
        }

        String value = jedis.get(key);
        if (value == null) {
            statistics.addMissCount();
            return null;
        }

        statistics.addHitCount();
        return Short.valueOf(value);
    }

    @Override
    public Float getFloat(String key) {
        if (key == null) {
            return null;
        }

        String value = jedis.get(key);
        if (value == null) {
            statistics.addMissCount();
            return null;
        }

        statistics.addHitCount();
        return Float.valueOf(value);
    }

    @Override
    public Double getDouble(String key) {
        if (key == null) {
            return null;
        }

        String value = jedis.get(key);
        if (value == null) {
            statistics.addMissCount();
            return null;
        }

        statistics.addHitCount();
        return Double.valueOf(value);
    }

    @Override
    public byte[] getBytes(String key) {
        Charset charset = config.getChrset();
        byte[] value = jedis.get(key.getBytes(charset));
        if (value == null) {
            statistics.addMissCount();
            return null;
        }

        statistics.addHitCount();
        return value;
    }

    @Override
    public List<?> getList(String key, ProtoSchema schema) {
        ProtoBuf buffer = get(key);
        if (buffer == null) {
            return null;
        }
        List<?> value = buffer.getList(schema);
        buffer.rewind();
        if (value == null) {
            statistics.addMissCount();
            return null;
        }

        statistics.addHitCount();
        return value;
    }

    @Override
    public Map<?, ?> getMap(String key, ProtoSchema schema) {
        ProtoBuf buffer = get(key);
        if (buffer == null) {
            return null;
        }
        Map<?, ?> value = buffer.getMap(schema);
        buffer.rewind();
        if (value == null) {
            statistics.addMissCount();
            return null;
        }

        statistics.addHitCount();
        return value;
    }

    @Override
    public <T> T getObject(String key, Class<T> clazz, ProtoSchema schema) {
        ProtoBuf buffer = get(key);
        if (buffer == null) {
            return null;
        }
        T value = buffer.getObject(clazz, schema);
        buffer.rewind();
        if (value == null) {
            statistics.addMissCount();
            return null;
        }

        statistics.addHitCount();
        return value;
    }

    @Override
    public Param getParam(String key, ProtoSchema schema) {
        ProtoBuf buffer = get(key);
        if (buffer == null) {
            return null;
        }
        Param value = buffer.getParam(schema);
        buffer.rewind();
        if (value == null) {
            statistics.addMissCount();
            return null;
        }

        statistics.addHitCount();
        return value;
    }

    @Override
    public Table<?> getTable(String key, ProtoSchema schema) {
        ProtoBuf buffer = get(key);
        if (buffer == null) {
            return null;
        }
        Table<?> value = buffer.getTable(schema);
        buffer.rewind();
        if (value == null) {
            statistics.addMissCount();
            return null;
        }

        statistics.addHitCount();
        return value;
    }

    @Override
    public List<String> getStringList(List<String> keys) {
        if (keys == null) {
            return null;
        }

        String[] skeys = new String[keys.size()];
        keys.toArray(skeys);
        List<String> values = jedis.mget(skeys);
        for (String value : values) {
            if (value == null) {
                statistics.addMissCount();
            } else {
                statistics.addHitCount();
            }
        }
        return values;
    }

    @Override
    public List<ProtoBuf> getBufferList(List<String> keys) {
        if (keys == null) {
            return null;
        }

        Charset charset = config.getChrset();
        byte[][] bkeys = new byte[keys.size()][];
        int currentIndex = 0;
        for (String key : keys) {
            bkeys[currentIndex++] = key.getBytes(charset);
        }
        List<byte[]> bvalues = jedis.mget(bkeys);
        List<ProtoBuf> values = new LinkedList<ProtoBuf>();
        for (byte[] value : bvalues) {
            if (value == null) {
                statistics.addMissCount();
                values.add(null);
            } else {
                statistics.addHitCount();
                values.add(ProtoBuf.wrap(value));
            }
        }
        return values;
    }

    @Override
    public boolean put(String key, ProtoBuf value) {
        return put(key, value, false);
    }

    @Override
    public boolean put(String key, ProtoBuf value, boolean compact) {
        if (key == null || value == null) {
            return false;
        }

        if (compact) {
            value.compact();
        }
        Charset charset = config.getChrset();
        String result = jedis.set(key.getBytes(charset), value.array());
        doSetExpire(key);
        return REDIS_RESPONSE_OK.equals(result);
    }

    @Override
    public boolean put(String key, String value) {
        if (key == null || value == null) {
            return false;
        }

        String result = jedis.set(key, value);
        doSetExpire(key);
        return REDIS_RESPONSE_OK.equals(result);
    }

    @Override
    public boolean put(String key, int value) {
        return put(key, String.valueOf(value));
    }

    @Override
    public boolean put(String key, boolean value) {
        return put(key, String.valueOf(value));
    }

    @Override
    public boolean put(String key, long value) {
        return put(key, String.valueOf(value));
    }

    @Override
    public boolean put(String key, short value) {
        return put(key, String.valueOf(value));
    }

    @Override
    public boolean put(String key, double value) {
        return put(key, String.valueOf(value));
    }

    @Override
    public boolean put(String key, float value) {
        return put(key, String.valueOf(value));
    }

    @Override
    public boolean put(String key, byte[] value) {
        if (value == null) {
            return false;
        }

        ProtoBuf buffer = ProtoBuf.wrap(value);
        return put(key, buffer, false);
    }

    @Override
    public boolean put(String key, Object value, ProtoSchema schema) {
        if (value == null) {
            return false;
        }

        ProtoBuf buffer = new ProtoBuf();
        buffer.putObject(value, schema);
        return put(key, buffer, true);
    }

    @Override
    public boolean put(String key, Map<?, ?> value, ProtoSchema schema) {
        ProtoBuf buffer = new ProtoBuf();
        buffer.putMap(value, schema);
        return put(key, buffer, true);
    }

    @Override
    public boolean put(String key, List<?> value, ProtoSchema schema) {
        if (value == null) {
            return false;
        }

        ProtoBuf buffer = new ProtoBuf(config.isDirectBuffer());
        buffer.putList(value, schema);
        return put(key, buffer, true);
    }

    @Override
    public boolean put(String key, Param value, ProtoSchema schema) {
        if (value == null) {
            return false;
        }

        ProtoBuf buffer = new ProtoBuf(config.isDirectBuffer());
        buffer.putParam(value, schema);
        return put(key, buffer, true);
    }

    @Override
    public boolean put(String key, Table<?> value, ProtoSchema schema) {
        if (value == null) {
            return false;
        }

        ProtoBuf buffer = new ProtoBuf(config.isDirectBuffer());
        buffer.putTable(value, schema);
        return put(key, buffer, true);
    }

    public boolean put(List<String> keys, List<String> values) {
        if (keys == null || values == null || keys.size() != values.size()) {
            return false;
        }

        // 因为当前采用的是redis cluster集群，获取不到pipeline，只能循环操作
        Iterator<String> keyIterator = keys.iterator();
        Iterator<String> valueIterator = values.iterator();
        while(keyIterator.hasNext()) {
            String key = keyIterator.next();
            jedis.set(key, valueIterator.next());
            doSetExpire(key);
        }
        return true;
    }

    @Override
    public boolean put(List<String> keys, List<ProtoBuf> values, boolean compact) {
        if (keys == null || values == null || keys.size() != values.size()) {
            return false;
        }

        // 因为当前采用的是redis cluster集群，获取不到pipeline，只能循环操作
        // 底层采用Iterator迭代器遍历而非fori索引遍历
        // 这样可以保证List为ArrayList或者LinkList时都可以加快遍历速度
        Iterator<String> keyIterator = keys.iterator();
        Iterator<ProtoBuf> valueIterator = values.iterator();
        while(keyIterator.hasNext()) {
            String key = keyIterator.next();
            ProtoBuf value = valueIterator.next();
            if (compact) {
                value.compact();
            }
            Charset charset = config.getChrset();
            jedis.set(key.getBytes(charset), value.array());
            doSetExpire(key);
        }
        return true;
    }

    @Override
    public long incr(String key) {
        long result = jedis.incr(key);
        doSetExpire(key);
        return result;
    }

    @Override
    public long incrBy(String key, long value) {
        long result = jedis.incrBy(key, value);
        doSetExpire(key);
        return result;
    }

    @Override
    public long decr(String key) {
        long result = jedis.decr(key);
        doSetExpire(key);
        return result;
    }

    @Override
    public long decrBy(String key, long value) {
        long result = jedis.decrBy(key, value);
        doSetExpire(key);
        return result;
    }

    @Override
    public ProtoBuf hget(String key, String field) {
        if (key == null || field == null) {
            return null;
        }

        Charset charset = config.getChrset();
        byte[] value = jedis.hget(key.getBytes(charset), field.getBytes(charset));
        if (value == null) {
            statistics.addMissCount();
            return null;
        }

        statistics.addHitCount();
        return ProtoBuf.wrap(value);
    }

    @Override
    public long hlen(String key) {
        if (key == null) {
            return 0;
        }
        return jedis.hlen(key);
    }

    @Override
    public ProtoBuf hgetBuffer(String key, String field) {
        return hget(key, field);
    }

    @Override
    public String hgetString(String key, String field) {
        if (key == null || field == null) {
            return null;
        }

        String value = jedis.hget(key, field);
        if (value == null) {
            statistics.addMissCount();
            return null;
        }

        statistics.addHitCount();
        return value;
    }

    @Override
    public Integer hgetInt(String key, String field) {
        String value = hgetString(key, field);
        if (value == null) {
            statistics.addMissCount();
            return null;
        }

        statistics.addHitCount();
        return Integer.valueOf(value);
    }

    @Override
    public Long hgetLong(String key, String field) {
        String value = hgetString(key, field);
        if (value == null) {
            statistics.addMissCount();
            return null;
        }

        statistics.addHitCount();
        return Long.valueOf(value);
    }

    @Override
    public Short hgetShort(String key, String field) {
        String value = hgetString(key, field);
        if (value == null) {
            statistics.addMissCount();
            return null;
        }

        statistics.addHitCount();
        return Short.valueOf(value);
    }

    @Override
    public Double hgetDouble(String key, String field) {
        String value = hgetString(key, field);
        if (value == null) {
            statistics.addMissCount();
            return null;
        }

        statistics.addHitCount();
        return Double.valueOf(value);
    }

    @Override
    public Float hgetFloat(String key, String field) {
        String value = hgetString(key, field);
        if (value == null) {
            statistics.addMissCount();
            return null;
        }

        statistics.addHitCount();
        return Float.valueOf(value);
    }

    @Override
    public byte[] hgetBytes(String key, String field) {
        if (key == null || field == null) {
            return null;
        }

        Charset charset = config.getChrset();
        byte[] value = jedis.hget(key.getBytes(charset), field.getBytes(charset));
        if (value == null) {
            statistics.addMissCount();
            return null;
        }

        statistics.addHitCount();
        return value;
    }

    @Override
    public <T> T hgetObject(String key, String field, Class<T> clazz, ProtoSchema schema) {
        ProtoBuf value = hget(key, field);
        if (value == null) {
            statistics.addMissCount();
            return null;
        }

        statistics.addHitCount();
        return value.getObject(clazz, schema);
    }

    @Override
    public Map<?, ?> hgetMap(String key, String field, ProtoSchema schema) {
        ProtoBuf value = hget(key, field);
        if (value == null) {
            statistics.addMissCount();
            return null;
        }

        statistics.addHitCount();
        return value.getMap(schema);
    }

    @Override
    public List<?> hgetList(String key, String field, ProtoSchema schema) {
        ProtoBuf value = hget(key, field);
        if (value == null) {
            statistics.addMissCount();
            return null;
        }

        statistics.addHitCount();
        return value.getList(schema);
    }

    @Override
    public Param hgetParam(String key, String field, ProtoSchema schema) {
        ProtoBuf value = hget(key, field);
        if (value == null) {
            statistics.addMissCount();
            return null;
        }

        statistics.addHitCount();
        return value.getParam(schema);
    }

    @Override
    public Table<?> hgetTable(String key, String field, ProtoSchema schema) {
        ProtoBuf value = hget(key, field);
        if (value == null) {
            statistics.addMissCount();
            return null;
        }

        statistics.addHitCount();
        return value.getTable(schema);
    }

    @Override
    public Map<String, ProtoBuf> hgetBufferMap(String key) {
        if (key == null) {
            return null;
        }

        Charset charset = config.getChrset();
        Map<String, ProtoBuf> value = new HashMap<String, ProtoBuf>();
        Map<byte[], byte[]> values = jedis.hgetAll(key.getBytes(charset));
        if (values == null) {
            statistics.addMissCount();
            return null;
        }

        for (Entry<byte[], byte[]> entry : values.entrySet()) {
            String mapKey = new String(entry.getKey(), charset);
            byte[] mapValue = entry.getValue();
            ProtoBuf buffer = ProtoBuf.wrap(mapValue);
            value.put(mapKey, buffer);
        }
        statistics.addHitCount();
        return value;
    }

    @Override
    public List<String> hgetStringList(String key) {
        if (key == null) {
            return null;
        }

        List<String> value = new LinkedList<String>();
        Map<String, String> values = jedis.hgetAll(key);
        if (values == null) {
            statistics.addMissCount();
            return null;
        }

        for (String data : values.values()) {
            value.add(data);
        }
        statistics.addHitCount();
        return value;
    }

    @Override
    public Table<String> hgetStringTable(String key) {
        if (key == null) {
            return null;
        }

        Table<String> value = new Table<String>();
        Map<String, String> values = jedis.hgetAll(key);
        if (values == null) {
            statistics.addMissCount();
            return null;
        }

        for (String data : values.values()) {
            value.add(data);
        }
        statistics.addHitCount();
        return value;
    }

    @Override
    public <T> List<T> hgetObjectList(String key, Class<T> clazz, ProtoSchema schema) {
        if (key == null) {
            return null;
        }

        Charset charset = config.getChrset();
        List<T> value = new LinkedList<T>();
        Map<byte[], byte[]> values = jedis.hgetAll(key.getBytes(charset));
        if (values == null) {
            statistics.addMissCount();
            return null;
        }

        for (byte[] data : values.values()) {
            ProtoBuf buffer = ProtoBuf.wrap(data);
            value.add((T) buffer.getObject(clazz, schema));
        }
        statistics.addHitCount();
        return value;
    }

    @Override
    public <T> Table<T> hgetObjectTable(String key, Class<T> clazz, ProtoSchema schema) {
        if (key == null) {
            return null;
        }

        Charset charset = config.getChrset();
        Table<T> value = new Table<T>();
        Map<byte[], byte[]> values = jedis.hgetAll(key.getBytes(charset));
        if (values == null) {
            statistics.addMissCount();
            return null;
        }

        for (byte[] data : values.values()) {
            ProtoBuf buffer = ProtoBuf.wrap(data);
            value.add((T) buffer.getObject(clazz, schema));
        }
        statistics.addHitCount();
        return value;
    }

    @Override
    public List<Param> hgetParamList(String key, ProtoSchema schema) {
        if (key == null) {
            return null;
        }

        Charset charset = config.getChrset();
        List<Param> value = new LinkedList<Param>();
        Map<byte[], byte[]> values = jedis.hgetAll(key.getBytes(charset));
        if (values == null) {
            statistics.addMissCount();
            return null;
        }

        for (byte[] data : values.values()) {
            ProtoBuf buffer = ProtoBuf.wrap(data);
            value.add(buffer.getParam(schema));
        }
        statistics.addHitCount();
        return value;
    }

    @Override
    public Table<Param> hgetParamTable(String key, ProtoSchema schema) {
        if (key == null) {
            return null;
        }

        Charset charset = config.getChrset();
        Table<Param> value = new Table<Param>();
        Map<byte[], byte[]> values = jedis.hgetAll(key.getBytes(charset));
        if (values == null) {
            statistics.addMissCount();
            return null;
        }

        for (byte[] data : values.values()) {
            ProtoBuf buffer = ProtoBuf.wrap(data);
            value.add(buffer.getParam(schema));
        }
        statistics.addHitCount();
        return value;
    }

    @Override
    public List<Table<?>> hgetTableList(String key, ProtoSchema schema) {
        if (key == null) {
            return null;
        }

        Charset charset = config.getChrset();
        List<Table<?>> value = new LinkedList<Table<?>>();
        Map<byte[], byte[]> values = jedis.hgetAll(key.getBytes(charset));
        if (values == null) {
            statistics.addMissCount();
            return null;
        }

        for (byte[] data : values.values()) {
            ProtoBuf buffer = ProtoBuf.wrap(data);
            value.add(buffer.getTable(schema));
        }
        statistics.addHitCount();
        return value;
    }

    @Override
    public Table<Table<?>> hgetTableAll(String key, ProtoSchema schema) {
        if (key == null) {
            return null;
        }

        Charset charset = config.getChrset();
        Table<Table<?>> value = new Table<Table<?>>();
        Map<byte[], byte[]> values = jedis.hgetAll(key.getBytes(charset));
        if (values == null) {
            statistics.addMissCount();
            return null;
        }

        for (byte[] data : values.values()) {
            ProtoBuf buffer = ProtoBuf.wrap(data);
            value.add(buffer.getTable(schema));
        }
        statistics.addHitCount();
        return value;
    }

    @Override
    public List<String> hgetKeyList(String key) {
        if (key == null) {
            return null;
        }

        Charset charset = config.getChrset();
        Set<byte[]> keys = jedis.hkeys(key.getBytes(charset));
        if (keys == null) {
            return null;
        }
        List<String> value = new ArrayList<String>(keys.size());
        for (byte[] data : keys) {
            value.add(new String(data, charset));
        }
        return value;
    }

    @Override
    public boolean hput(String key, String field, ProtoBuf value, boolean compact) {
        if (key == null || field == null || value == null) {
            return false;
        }

        if (compact) {
            value.compact();
        }
        Charset charset = config.getChrset();
        long count = jedis.hset(key.getBytes(charset), field.getBytes(charset), value.array());
        doSetExpire(key);
        return count >= 0;
    }

    @Override
    public boolean hput(String key, String field, byte[] value) {
        if (key == null || field == null || value == null) {
            return false;
        }

        Charset charset = config.getChrset();
        long count = jedis.hset(key.getBytes(charset), field.getBytes(charset), value);
        doSetExpire(key);
        return count >= 0;
    }

    @Override
    public boolean hput(String key, String field, String value) {
        if (key == null || field == null || value == null) {
            return false;
        }

        return jedis.hset(key, field, value) >= 0;
    }

    @Override
    public boolean hput(String key, String field, int value) {
        return hput(key, field, String.valueOf(value));
    }

    @Override
    public boolean hput(String key, String field, long value) {
        return hput(key, field, String.valueOf(value));
    }

    @Override
    public boolean hput(String key, String field, short value) {
        return hput(key, field, String.valueOf(value));
    }

    @Override
    public boolean hput(String key, String field, double value) {
        return hput(key, field, String.valueOf(value));
    }

    @Override
    public boolean hput(String key, String field, float value) {
        return hput(key, field, String.valueOf(value));
    }

    @Override
    public boolean hput(String key, String field, Object value, ProtoSchema schema) {
        if (value == null) {
            return false;
        }

        ProtoBuf buffer = new ProtoBuf();
        buffer.putObject(value, schema);
        return hput(key, field, buffer, false);
    }

    @Override
    public boolean hput(String key, String field, Map<?, ?> value, ProtoSchema schema) {
        if (value == null) {
            return false;
        }

        ProtoBuf buffer = new ProtoBuf();
        buffer.putMap(value, schema);
        return hput(key, field, buffer, false);
    }

    @Override
    public boolean hput(String key, String field, List<?> value, ProtoSchema schema) {
        if (value == null) {
            return false;
        }

        ProtoBuf buffer = new ProtoBuf();
        buffer.putList(value, schema);
        return hput(key, field, buffer, false);
    }

    @Override
    public boolean hput(String key, String field, Param value, ProtoSchema schema) {
        if (value == null) {
            return false;
        }

        ProtoBuf buffer = new ProtoBuf();
        buffer.putParam(value, schema);
        return hput(key, field, buffer, false);
    }

    @Override
    public boolean hput(String key, String field, Table<?> value, ProtoSchema schema) {
        if (value == null) {
            return false;
        }

        ProtoBuf buffer = new ProtoBuf();
        buffer.putTable(value, schema);
        return hput(key, field, buffer, false);
    }

    @Override
    public long hincrBy(String key, String field, long value) {
        if (key == null || field == null) {
            throw new IllegalArgumentException();
        }

        Charset charset = config.getChrset();
        long count = jedis.hincrBy(key.getBytes(charset), field.getBytes(charset), value);
        doSetExpire(key);
        return count;
    }

    @Override
    public boolean hmput(String key, Map<byte[], byte[]> value) {
        if (key == null || value == null) {
            return false;
        }

        Charset charset = config.getChrset();
        String result = jedis.hmset(key.getBytes(charset), value);
        doSetExpire(key);
        return REDIS_RESPONSE_OK.equals(result);
    }

    @Override
    public boolean remove(String key) {
        if (key == null) {
            return false;
        }

        return jedis.del(key) >= 0;
    }

    @Override
    public boolean remove(String... keys) {
        if (keys == null) {
            return false;
        }

        return jedis.del(keys) >= 0;
    }

    @Override
    public boolean remove(List<String> keys) {
        if (keys == null) {
            return false;
        }

        String[] keyList = new String[keys.size()];
        keys.toArray(keyList);
        return jedis.del(keyList) >= 0;
    }

    @Override
    public boolean remove(String key, String field) {
        if (key == null || field == null) {
            return false;
        }

        return jedis.hdel(key, field) >= 0;
    }

    @Override
    public boolean remove(String key, String... fields) {
        if (key == null || fields == null) {
            return false;
        }

        return jedis.hdel(key, fields) >= 0;
    }

    @Override
    public boolean remove(String key, List<String> fields) {
        if (key == null || fields == null) {
            return false;
        }
        String[] fieldArray = new String[fields.size()];
        fields.toArray(fieldArray);
        return jedis.hdel(key, fieldArray) >= 0;
    }

    @Override
    public synchronized void shutdown() {
        try {
            jedis.close();
        } catch (IOException e) {
        }
    }

    /**
     * 设置过期时间
     */
    private void doSetExpire(String key) {
        RedisConfig redisConfig = config.getRedisConfig();
        int expirationTime = redisConfig.getExpirationTime();
        if (redisConfig.isExpirationTimeRandom()) {
            // 设置过期时间随机，避免同一时间有大量缓存过期导致回缓压力大
            int timeMin = redisConfig.getExpirationTimeRandomMin();
            int timeMax = redisConfig.getExpirationTimeRandomMax();
            expirationTime = random.nextInt(timeMax - timeMin) + timeMin;
        }
        expirationTime = expirationTime / 1000;
        if (expirationTime > 0) {
            jedis.expire(key, expirationTime);
        }
    }
}
