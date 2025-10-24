package cloud.apposs.cache;

import cloud.apposs.protobuf.ProtoBuf;
import cloud.apposs.protobuf.ProtoSchema;
import cloud.apposs.util.Param;
import cloud.apposs.util.Table;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 缓存接口定义，全局单例，不同的缓存实现的缓存方式不同，包括
 * <pre>
 * 1、采用JVM内存缓存
 * 2、采用Redis分布缓存
 * 3、采用Memcache分布缓存
 * </pre>
 */
public interface Cache extends Serializable, Cloneable {
    public static final String CACHE_JVM = "Jvm";
    public static final String CACHE_REDIS = "Redis";

    /**
     * 获取缓存条数
     */
    int size();

    /**
     * 判断Key值是否存在
     *
     * @param key 缓存Key
     * @return 存在返回true
     */
    boolean exists(String key);

    /**
     * 设置缓存过期时间
     *
     * @param key            缓存Key
     * @param expirationTime 缓存过期时间，单位毫秒
     * @return 原先过期时间，单位毫秒，如果指定的Key不存在或者其他错误则返回-1
     */
    int expire(String key, int expirationTime);

    /**
     * 获取原始缓存数据
     *
     * @param key 缓存Key
     * @return {@link ProtoBuf}
     */
    ProtoBuf get(String key);

    /**
     * 获取缓存数据
     *
     * @param key 缓存Key
     * @return {@link ProtoBuf}
     */
    ProtoBuf getBuffer(String key);

    /**
     * 获取缓存数据
     *
     * @param key 缓存Key
     * @return 字符串
     */
    String getString(String key);

    /**
     * 获取缓存数据
     */
    Integer getInt(String key);

    /**
     * 获取缓存数据
     */
    Long getLong(String key);

    /**
     * 获取缓存数据
     */
    Short getShort(String key);

    /**
     * 获取缓存数据
     */
    Float getFloat(String key);

    /**
     * 获取缓存数据
     */
    Double getDouble(String key);

    /**
     * 获取缓存数据
     *
     * @param key 缓存Key
     * @return 字节数组
     */
    byte[] getBytes(String key);

    /**
     * 获取缓存数据
     *
     * @param key    缓存Key
     * @param clazz  缓存值类型
     * @param schema 对象元信息
     * @return 缓存中包装对象
     */
    <T> T getObject(String key, Class<T> clazz, ProtoSchema schema);

    /**
     * 获取缓存数据
     *
     * @param key    缓存Key
     * @param schema 对象元信息
     * @return {@link Map}
     */
    Map<?, ?> getMap(String key, ProtoSchema schema);

    /**
     * 获取缓存数据
     *
     * @param key    缓存Key
     * @param schema 对象元信息
     * @return {@link List}
     */
    List<?> getList(String key, ProtoSchema schema);

    /**
     * 获取缓存数据
     *
     * @param key    缓存Key
     * @param schema 对象元信息
     * @return {@link Param}
     */
    Param getParam(String key, ProtoSchema schema);

    /**
     * 获取缓存数据
     *
     * @param key    缓存Key
     * @param schema 对象元信息
     * @return {@link Table}
     */
    Table<?> getTable(String key, ProtoSchema schema);

    /**
     * 批量获取缓存数据，像Redis底层直接用pipeline批量添加并只发送请求，提升性能，
     * 注意：因为是批量获取，所以返回的数组如果KEY集合中某个KEY不存在则返回的List集合中该元素为null，需要业务自己判断集合里面的元素是否有null
     *
     * @param keys 缓存Key列表
     * @return {@link List}
     */
    List<String> getStringList(List<String> keys);

    /**
     * 批量获取缓存数据，像Redis底层直接用pipeline批量添加并只发送请求，提升性能，
     * 注意：因为是批量获取，所以返回的数组如果KEY集合中某个KEY不存在则返回的List集合中该元素为null，需要业务自己判断集合里面的元素是否有null
     *
     * @param keys 缓存Key列表
     * @return {@link List}
     */
    List<ProtoBuf> getBufferList(List<String> keys);

    /**
     * 添加缓存数据
     *
     * @param key   缓存Key
     * @param value 缓存字节数据
     * @return 添加缓存成功返回true
     */
    boolean put(String key, ProtoBuf value);

    /**
     * 添加缓存数据
     *
     * @param key     缓存Key
     * @param value   缓存字节数据
     * @param compact 是否压缩可用字节
     * @return 添加缓存成功返回true
     */
    boolean put(String key, ProtoBuf value, boolean compact);

    /**
     * 添加缓存数据
     *
     * @param key   缓存Key
     * @param value 缓存字节数据
     * @return 添加缓存成功返回true
     */
    boolean put(String key, String value);

    /**
     * 添加缓存数据
     *
     * @param key   缓存Key
     * @param value 缓存字节数据
     * @return 添加缓存成功返回true
     */
    boolean put(String key, int value);

    /**
     * 添加缓存数据
     *
     * @param key   缓存Key
     * @param value 缓存字节数据
     * @return 添加缓存成功返回true
     */
    boolean put(String key, boolean value);

    /**
     * 添加缓存数据
     *
     * @param key   缓存Key
     * @param value 缓存字节数据
     * @return 添加缓存成功返回true
     */
    boolean put(String key, long value);

    /**
     * 添加缓存数据
     *
     * @param key   缓存Key
     * @param value 缓存字节数据
     * @return 添加缓存成功返回true
     */
    boolean put(String key, short value);

    /**
     * 添加缓存数据
     *
     * @param key   缓存Key
     * @param value 缓存字节数据
     * @return 添加缓存成功返回true
     */
    boolean put(String key, double value);

    /**
     * 添加缓存数据
     *
     * @param key   缓存Key
     * @param value 缓存字节数据
     * @return 添加缓存成功返回true
     */
    boolean put(String key, float value);

    /**
     * 添加缓存数据
     *
     * @param key   缓存Key
     * @param value 缓存字节数据
     * @return 添加缓存成功返回true
     */
    boolean put(String key, byte[] value);

    /**
     * 添加缓存数据
     *
     * @param key    缓存Key
     * @param value  缓存字节数据
     * @param schema 对象元信息
     * @return 添加缓存成功返回true
     */
    boolean put(String key, Object value, ProtoSchema schema);

    /**
     * 添加缓存数据
     *
     * @param key    缓存Key
     * @param value  缓存字节数据
     * @param schema 对象元信息
     * @return 添加缓存成功返回true
     */
    boolean put(String key, Map<?, ?> value, ProtoSchema schema);

    /**
     * 添加缓存数据
     *
     * @param key    缓存Key
     * @param value  缓存字节数据
     * @param schema 对象元信息
     * @return 添加缓存成功返回true
     */
    boolean put(String key, List<?> value, ProtoSchema schema);

    /**
     * 添加缓存数据
     *
     * @param key    缓存Key
     * @param value  缓存字节数据
     * @param schema 对象元信息
     * @return 添加缓存成功返回true
     */
    boolean put(String key, Param value, ProtoSchema schema);

    /**
     * 添加缓存数据
     *
     * @param key    缓存Key
     * @param value  缓存字节数据
     * @param schema 对象元信息
     * @return 添加缓存成功返回true
     */
    boolean put(String key, Table<?> value, ProtoSchema schema);

    /**
     * 批量添加缓存数据，keys长度和values长度必须一致且索引匹配，
     * 像Redis底层直接用pipeline批量添加并只发送请求，提升性能，
     * 注意：因为是批量操作，所以无论数据有没有插入成功，均返回true
     *
     * @param keys   缓存Key列表
     * @param values 缓存字节数据列表
     * @return 添加缓存成功返回true
     */
    boolean put(List<String> keys, List<String> values);

    /**
     * 批量添加缓存数据，keys长度和values长度必须一致且索引匹配，
     * 像Redis底层直接用pipeline批量添加并只发送请求，提升性能
     * 注意：因为是批量操作，所以无论数据有没有插入成功，均返回true
     *
     * @param keys   缓存Key列表
     * @param values 缓存字节数据列表
     * @param compact 是否压缩可用字节
     * @return 添加缓存成功返回true
     */
    boolean put(List<String> keys, List<ProtoBuf> values, boolean compact);

    /**
     * 将 key 中储存的数字值+1
     * 如果 key 不存在，那么 key 的值会先被初始化为0，然后再执行 INCR 操作
     * 注意该 key 无法通过get来获取，需要直接通过 INCR 来获取，主要应用于限流、秒杀等场景
     *
     * @param  key 缓存Key
     * @return 执行 INCR 命令之后 key 的值
     */
    long incr(String key);

    /**
     * 将 key 中储存的数字加上指定的增量值
     * 如果 key 不存在，那么 key 的值会先被初始化为0，然后再执行 INCRBY 命令
     * 注意该 key 无法通过get来获取，需要直接通过 INCRBY 来获取，主要应用于限流、秒杀等场景
     *
     * @param  key 缓存Key
     * @param  value 增量值
     * @return 加上指定的增量值之后 key 的值
     */
    long incrBy(String key, long value);

    /**
     * 将 key 中储存的数字值-1
     * 注意该 key 无法通过get来获取，需要直接通过 DECR 来获取，主要应用于限流、秒杀等场景
     *
     * @param  key 缓存Key
     * @return 执行 DECR 命令之后 key 的值
     */
    long decr(String key);

    /**
     * 将 key 所储存的值减去指定的减量值
     * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 DECRBY 操作
     * 注意该 key 无法通过get来获取，需要直接通过 DECRBY 来获取，主要应用于限流、秒杀等场景
     *
     * @param  key key 缓存Key
     * @param  value 减量值
     * @return 减去指定减量值之后， key 的值
     */
    long decrBy(String key, long value);

    /**
     * 获取原始二级缓存数据
     *
     * @param key   缓存Key
     * @param field 缓存二级Key
     * @return {@link ProtoBuf}
     */
    ProtoBuf hget(String key, String field);

    /**
     * 获取二级缓存数据下第一层有多少个key
     *
     * @param key 缓存Key
     * @return Key的数量
     */
    long hlen(String key);

    /**
     * 获取二级缓存数据
     *
     * @param key   缓存Key
     * @param field 缓存二级Key
     * @return {@link ProtoBuf}
     */
    ProtoBuf hgetBuffer(String key, String field);

    /**
     * 获取二级缓存数据
     *
     * @param key   缓存Key
     * @param field 缓存二级Key
     * @return 字符串
     */
    String hgetString(String key, String field);

    /**
     * 获取二级缓存数据
     *
     * @param key   缓存Key
     * @param field 缓存二级Key
     * @return 字符串
     */
    Integer hgetInt(String key, String field);

    /**
     * 获取二级缓存数据
     *
     * @param key   缓存Key
     * @param field 缓存二级Key
     * @return 字符串
     */
    Long hgetLong(String key, String field);

    /**
     * 获取二级缓存数据
     *
     * @param key   缓存Key
     * @param field 缓存二级Key
     * @return 字符串
     */
    Short hgetShort(String key, String field);

    /**
     * 获取二级缓存数据
     *
     * @param key   缓存Key
     * @param field 缓存二级Key
     * @return 字符串
     */
    Double hgetDouble(String key, String field);

    /**
     * 获取二级缓存数据
     *
     * @param key   缓存Key
     * @param field 缓存二级Key
     * @return 字符串
     */
    Float hgetFloat(String key, String field);

    /**
     * 获取二级缓存数据
     *
     * @param key   缓存Key
     * @param field 缓存二级Key
     * @return 字节数组
     */
    byte[] hgetBytes(String key, String field);

    /**
     * 获取缓存数据
     *
     * @param key    缓存Key
     * @param clazz  缓存值类型
     * @param schema 对象元信息
     * @return 缓存中包装对象
     */
    <T> T hgetObject(String key, String field, Class<T> clazz, ProtoSchema schema);

    /**
     * 获取缓存数据
     *
     * @param key    缓存Key
     * @param field  缓存二级Key
     * @param schema 对象元信息
     * @return {@link Map}
     */
    Map<?, ?> hgetMap(String key, String field, ProtoSchema schema);

    /**
     * 获取缓存数据
     *
     * @param key    缓存Key
     * @param field  缓存二级Key
     * @param schema 对象元信息
     * @return {@link List}
     */
    List<?> hgetList(String key, String field, ProtoSchema schema);

    /**
     * 获取缓存数据
     *
     * @param key    缓存Key
     * @param field  缓存二级Key
     * @param schema 对象元信息
     * @return {@link Map}
     */
    Param hgetParam(String key, String field, ProtoSchema schema);

    /**
     * 获取缓存数据
     *
     * @param key    缓存Key
     * @param field  缓存二级Key
     * @param schema 对象元信息
     * @return {@link List}
     */
    Table<?> hgetTable(String key, String field, ProtoSchema schema);

    /**
     * 获取指定Key下所有的原始二级缓存数据，即Key->List(Field->Value)模式，
     * 注意如果Key下的Map数据比较多的话也有OOM风险，建议业务方拆分成范围再存储
     *
     * @param key 缓存Key
     */
    Map<String, ProtoBuf> hgetBufferMap(String key);

    /**
     * 获取指定Key下所有的二级缓存数据，即Key->List(String)模式，
     * 注意如果Key下的Map数据比较多的话也有OOM风险，建议业务方拆分成范围再存储
     *
     * @param key 缓存Key
     */
    List<String> hgetStringList(String key);

    /**
     * 获取指定Key下所有的二级缓存数据，即Key->List(String)模式，
     * 注意如果Key下的Map数据比较多的话也有OOM风险，建议业务方拆分成范围再存储
     *
     * @param key 缓存Key
     */
    Table<String> hgetStringTable(String key);

    /**
     * 获取指定Key下所有的二级缓存数据，即Key->List(Object)模式，
     * 注意如果Key下的Map数据比较多的话也有OOM风险，建议业务方拆分成范围再存储
     *
     * @param key    缓存Key
     * @param schema 对象元信息
     */
    <T> List<T> hgetObjectList(String key, Class<T> clazz, ProtoSchema schema);

    /**
     * 获取指定Key下所有的二级缓存数据，即Key->Table(Object)模式，
     * 注意如果Key下的Map数据比较多的话也有OOM风险，建议业务方拆分成范围再存储
     *
     * @param key    缓存Key
     * @param schema 对象元信息
     */
    <T> Table<T> hgetObjectTable(String key, Class<T> clazz, ProtoSchema schema);

    /**
     * 获取指定Key下所有的二级缓存数据，即Key->List(Param)模式，
     * 注意如果Key下的Map数据比较多的话也有OOM风险，建议业务方拆分成范围再存储
     *
     * @param key    缓存Key
     * @param schema 对象元信息
     */
    List<Param> hgetParamList(String key, ProtoSchema schema);

    /**
     * 获取指定Key下所有的二级缓存数据，即Key->List(Param)模式，
     * 注意如果Key下的Map数据比较多的话也有OOM风险，建议业务方拆分成范围再存储
     *
     * @param key    缓存Key
     * @param schema 对象元信息
     */
    Table<Param> hgetParamTable(String key, ProtoSchema schema);

    /**
     * 获取指定Key下所有的二级缓存数据，即Key->List(Table)模式，
     * 注意如果Key下的Map数据比较多的话也有OOM风险，建议业务方拆分成范围再存储
     *
     * @param key    缓存Key
     * @param schema 对象元信息
     */
    List<Table<?>> hgetTableList(String key, ProtoSchema schema);

    /**
     * 获取指定Key下所有的二级缓存数据，即Key->List(Table)模式，
     * 注意如果Key下的Map数据比较多的话也有OOM风险，建议业务方拆分成范围再存储
     *
     * @param key    缓存Key
     * @param schema 对象元信息
     */
    Table<Table<?>> hgetTableAll(String key, ProtoSchema schema);

    /**
     * 获取缓存二级键数据列表
     *
     * @param  key 缓存一级Key
     * @return 缓存二级key列表
     */
    List<String> hgetKeyList(String key);

    /**
     * 添加二缓存数据
     *
     * @param key     缓存Key
     * @param field   缓存二级Key
     * @param value   缓存字节数据
     * @param compact 是否压缩可用字节
     * @return 添加缓存成功返回true
     */
    boolean hput(String key, String field, ProtoBuf value, boolean compact);

    /**
     * 添加缓存二级数据，存储结构为Key->Field->String
     *
     * @param key   缓存Key
     * @param field 缓存二级Key
     * @param value 缓存字节数据
     * @return 添加缓存成功返回true
     */
    boolean hput(String key, String field, String value);

    /**
     * 添加缓存二级数据，存储结构为Key->Field->String
     *
     * @param key   缓存Key
     * @param field 缓存二级Key
     * @param value 缓存字节数据
     * @return 添加缓存成功返回true
     */
    boolean hput(String key, String field, int value);

    /**
     * 添加缓存二级数据，存储结构为Key->Field->String
     *
     * @param key   缓存Key
     * @param field 缓存二级Key
     * @param value 缓存字节数据
     * @return 添加缓存成功返回true
     */
    boolean hput(String key, String field, long value);

    /**
     * 添加缓存二级数据，存储结构为Key->Field->String
     *
     * @param key   缓存Key
     * @param field 缓存二级Key
     * @param value 缓存字节数据
     * @return 添加缓存成功返回true
     */
    boolean hput(String key, String field, short value);

    /**
     * 添加缓存二级数据，存储结构为Key->Field->String
     *
     * @param key   缓存Key
     * @param field 缓存二级Key
     * @param value 缓存字节数据
     * @return 添加缓存成功返回true
     */
    boolean hput(String key, String field, double value);

    /**
     * 添加缓存二级数据，存储结构为Key->Field->String
     *
     * @param key   缓存Key
     * @param field 缓存二级Key
     * @param value 缓存字节数据
     * @return 添加缓存成功返回true
     */
    boolean hput(String key, String field, float value);

    /**
     * 添加缓存二级数据，存储结构为Key->Field->Byte[]
     *
     * @param key   缓存Key
     * @param field 缓存二级Key
     * @param value 缓存字节数据
     * @return 添加缓存成功返回true
     */
    boolean hput(String key, String field, byte[] value);

    /**
     * 添加缓存二级数据，存储结构为Key->Field->Object
     *
     * @param key    缓存Key
     * @param field  缓存二级Key
     * @param value  缓存字节数据
     * @param schema 对象元信息
     * @return 添加缓存成功返回true
     */
    boolean hput(String key, String field, Object value, ProtoSchema schema);

    /**
     * 添加缓存二级数据，存储结构为Key->Field->Map
     *
     * @param key    缓存Key
     * @param field  缓存二级Key
     * @param value  缓存字节数据
     * @param schema 对象元信息
     * @return 添加缓存成功返回true
     */
    boolean hput(String key, String field, Map<?, ?> value, ProtoSchema schema);

    /**
     * 添加二级缓存数据，存储结构为Key->Field->List
     *
     * @param key    缓存Key
     * @param field  缓存二级Key
     * @param value  缓存字节数据
     * @param schema 对象元信息
     * @return 添加缓存成功返回true
     */
    boolean hput(String key, String field, List<?> value, ProtoSchema schema);

    /**
     * 添加缓存二级数据，存储结构为Key->Field->Param
     *
     * @param key    缓存Key
     * @param field  缓存二级Key
     * @param value  缓存字节数据
     * @param schema 对象元信息
     * @return 添加缓存成功返回true
     */
    boolean hput(String key, String field, Param value, ProtoSchema schema);

    /**
     * 添加二级缓存数据，存储结构为Key->Field->Table
     *
     * @param key    缓存Key
     * @param field  缓存二级Key
     * @param value  缓存字节数据
     * @param schema 对象元信息
     * @return 添加缓存成功返回true
     */
    boolean hput(String key, String field, Table<?> value, ProtoSchema schema);

    /**
     * 添加二级缓存数据，存储结构为Key->Field->Value，
     * 底层是遍历Map数据然后将Filed逐个设置到缓存中
     *
     * @param key   缓存Key
     * @param value 缓存字节数据
     * @return 添加缓存成功返回true
     */
    boolean hmput(String key, Map<byte[], byte[]> value);

    /**
     * 为哈希表 key 中的 field 的值加上增量 increment
     * 增量也可以为负数，相当于对给定字段的值进行减法操作
     * 如果 key 不存在，将自动创建一个新的哈希表并执行 HINCRBY 命令；如果域 field 不存在，那么在执行命令前，字段的值被初始化为 0
     * 若对于一个储存字符串值的 field 执行 HINCRBY 命令将造成一个错误，该命令操作的数值被限制在 64 位(bit)有符号数字表示之内。
     *
     * @param  key 缓存Key
     * @return 执行 INCR 命令之后 key 的值
     */
    long hincrBy(String key, String field, long value);

    /**
     * 移除缓存节点
     *
     * @param key 缓存Key
     * @return 移除成功true
     */
    boolean remove(String key);

    /**
     * 批量移除缓存节点
     *
     * @param  keys 缓存Key列表
     * @return 移除成功true
     */
    boolean remove(String... keys);

    /**
     * 批量移除缓存节点
     *
     * @param  keys 缓存Key列表
     * @return 移除成功true
     */
    boolean remove(List<String> keys);

    /**
     * 移除二级缓存节点
     *
     * @param key   缓存Key
     * @param field 缓存二级Key
     * @return 移除成功true
     */
    boolean remove(String key, String field);

    /**
     * 批量移除二级缓存节点
     *
     * @param key    缓存Key
     * @param fields 缓存二级Key
     * @return 移除成功true
     */
    boolean remove(String key, String... fields);

    /**
     * 批量移除二级缓存节点
     *
     * @param key    缓存Key
     * @param fields 缓存二级Key列表
     * @return 移除成功true
     */
    boolean remove(String key, List<String> fields);

    /**
     * 获取缓存统计信息
     */
    CacheStatistics getStatistics();

    /**
     * 关闭缓存服务
     */
    void shutdown();
}
