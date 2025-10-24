package cloud.apposs.cachex;

import cloud.apposs.cache.CacheManager;
import cloud.apposs.cachex.database.Query;
import cloud.apposs.cachex.database.Updater;
import cloud.apposs.cachex.database.Where;
import cloud.apposs.protobuf.ProtoSchema;
import cloud.apposs.util.Ref;

import java.util.List;

/**
 * 数据服务接口，将Dao数据源和Cache缓存数据进行结合统一封装，
 * 内部维护着指定数据源的DAO以及指定缓存类型的CACHE，
 * 注意如果外部业务需要配置多数据源，则需要维护多种不同的源CacheX实例
 * @param <K> 缓存Key
 * @param <V> 缓存Value
 */
public interface CacheX<K extends CacheKey, V> {
    /**
     * 缓存执行异常时的返回值
     */
    int CACHEX_EXECUTE_FAILURE = -1;

    /**
     * 获取缓存接口
     */
    CacheManager getCache();

    /**
     * 获取数据源接口
     */
    DBTemplate getTemplate();

    /**
     * 数据存储，对应数据获取方法为{@link #get(CacheKey, ProtoSchema, Object...)}，
     * 和{@link #hput(CacheKey, Object, Object, ProtoSchema, Object...)}方法互斥，
     * 对应数据实现接口为{@link CacheLoader#add(CacheKey, Object, ProtoSchema, DBTemplate, Ref, Object...)}，
     * 不能一个存储里面既能存储一级数据，又能存储二级数据，这样在同样Key的情况会有数据覆盖风险
     *
     * @param  key    存储Key
     * @param  value  存储数据
     * @param  schema 数据元信息，用于序列化/反序列化
     * @param  args   业务方传递的参数
     * @return 成功存储的数据条数，存储失败返回-1
     */
    int put(K key, V value, ProtoSchema schema, Object... args) throws Exception;

    /**
     * 批量插入数据集，
     * 对应数据实现接口为{@link CacheLoader#add(List, List, ProtoSchema, DBTemplate, List, Object...)}
     *
     * @param  keys   存储Key列表，如果不想数据集缓存，可传递null
     * @param  values 数据集
     * @param  schema 数据元信息，用于序列化/反序列化
     * @param  args   业务方传递的参数
     * @return 成功存储的数据条数，存储失败返回-1
     */
    int put(List<K> keys, List<V> values, ProtoSchema schema, Object... args) throws Exception;

    /**
     * 数据替换存储，
     * 对应数据实现接口为{@link CacheLoader#replace(CacheKey, Object, ProtoSchema, DBTemplate, Ref, Object...)}，
     *
     * @param  key    存储Key
     * @param  value  存储数据
     * @param  schema 数据元信息，用于序列化/反序列化
     * @param  args   业务方传递的参数
     * @return 成功存储的数据条数，存储失败返回-1
     */
    int replace(K key, V value, ProtoSchema schema, Object... args) throws Exception;

    /**
     * 基于索引获取数据，默认有缓存，检索速度最快
     * 对应数据实现接口为{@link CacheLoader#load(CacheKey, ProtoSchema, DBTemplate, Object...)}
     *
     * @param key    数据Key，不允许为空
     * @param schema 数据元信息，用于序列化/反序列化
     * @param args   业务方传递的参数
     */
    V get(K key, ProtoSchema schema, Object... args) throws Exception;

    /**
     * 基于索引的批量数据获取，并用指定的缓存Key列表进行缓存，
     * 主要用于业务中需要通过主键KEY列表批量获取数据，同时将数据和对应的缓存Key进行缓存，注意底层发现没有缓存数据会触发1+N查询
     * 对应数据实现接口为{@link CacheLoader#load(CacheKey, ProtoSchema, DBTemplate, Object...)}
     * 之所以没有query<List keys>是因为如果是查询并批量缓存无法保证查询的数据和缓存key列表是一致的，也无法做到缓存key和查询结果精确匹配
     *
     * <pre>
     * 相对于{@link #get(CacheKey, ProtoSchema, Object...)}遍历去获取，该接口的性能体现如下：
     * 1、一次Dao连接获取而不用每次遍历获取，减少数据库连接时间
     * 2、一次批量数据存进缓存，不用每次都远程连接缓存服务缓存数据，减少缓存服务连接时间
     * </pre>
     *
     * @param  keys   存储Key列表，因为是指定类型的缓存KEY，所以底层数据会进行缓存
     * @param  schema 数据元信息，用于序列化/反序列化
     * @param  args   业务方传递的参数
     * @return 查询结果集合并进行缓存
     */
    List<V> get(List<K> keys, ProtoSchema schema, Object... args) throws Exception;

    /**
     * 通过查询条件检索单条数据，并用特别KEY进行缓存存储，
     * 对应数据实现接口为{@link CacheLoader#select(CacheKey, Query, ProtoSchema, DBTemplate, Object...)}
     * 一般应用于复杂条件单条数据查询并且注意要确保要查询的数据是唯一，底层会只查询一条
     * 注意一般情况下如果有索引一定要改成{@link #get(CacheKey, ProtoSchema, Object...)}来进行基于索引的查询
     *
     * @param  key    存储Key，
     *                因为每个复杂查询Key不同，就不限制Key的主键定义，允许为空或者为NoCacheKey，则不缓存
     * @param  query  查询条件，可为空
     * @param  schema 数据元信息，用于序列化/反序列化
     * @param  args   业务方传递的参数
     * @return 查询结果集合
     */
    V select(CacheKey<?> key, Query query, ProtoSchema schema, Object... args) throws Exception;

    /**
     * 通过查询条件检索所有数据集，并用特别KEY进行缓存存储
     * 对应数据实现接口为{@link CacheLoader#query(CacheKey, Query, ProtoSchema, DBTemplate, Object...)}
     *
     * @param  key    存储Key，
     *                因为每个复杂查询Key不同，就不限制Key的主键定义，允许为空或者为NoCacheKey，则不缓存
     * @param  query  查询条件，可为空
     * @param  schema 数据元信息，用于序列化/反序列化
     * @param  args   业务方传递的参数
     * @return 查询结果集合
     */
    List<V> query(CacheKey<?> key, Query query, ProtoSchema schema, Object... args) throws Exception;

    /**
     * 检查指定Key的数据是否存在，底层依然是获取指定数据判断是否为空来判断数据是否存在，
     * 对应数据实现接口为{@link CacheLoader#load(CacheKey, ProtoSchema, DBTemplate, Object...)}
     *
     * @param key    数据Key
     * @param schema 数据元信息，用于序列化/反序列化
     * @param args   业务方传递的参数
     */
    boolean exist(K key, ProtoSchema schema, Object... args) throws Exception;

    /**
     * 检查指定Key的数据是否存在，底层依然是获取指定数据判断是否为空来判断数据是否存在，
     * 当数据为多条时，只要有一条数据存在则代表数据存在
     * 对应数据实现接口为{@link CacheLoader#query(CacheKey, Query, ProtoSchema, DBTemplate, Object...)}
     *
     * @param key    数据Key
     * @param query  查询条件，可为空
     * @param schema 数据元信息，用于序列化/反序列化
     * @param args   业务方传递的参数
     */
    boolean exist(CacheKey<?> key, Query query, ProtoSchema schema, Object... args) throws Exception;

    /**
     * 通过主键删除数据，底层维护主键缓存的清除
     * 对应数据实现接口为{@link CacheLoader#delete(CacheKey, DBTemplate, Object...)}
     *
     * @param  key  数据Key
     * @param  args 业务方传递的参数
     * @return 成功删除的数据条数，删除失败返回-1
     */
    int delete(K key, Object... args) throws Exception;

    /**
     * 通过主键列表批量删除数据，底层维护主键缓存的清除
     * 对应数据实现接口为{@link CacheLoader#delete(List, DBTemplate, Object...)}
     *
     * @param  keys 数据Key列表
     * @param  args 业务方传递的参数
     * @return 成功删除的数据条数，删除失败返回-1
     */
    int delete(List<K> keys, Object... args) throws Exception;

    /**
     * 通过匹配条件删除数据，
     * 对应数据实现接口为{@link CacheLoader#delete(CacheKey, Where, DBTemplate, Object...)}
     * 注意：此接口删除是业务自己定义删除条件，不通过主键来删除，
     * 则当业务自己定义删除条件时注意要做好对应缓存清除保证数据的一致性
     *
     * @param  key   数据Key
     *               因为每个删除Key不同，就不限制Key的主键定义，允许为空或者为NoCacheKey
     * @param  where 删除的匹配条件
     * @param  args  业务方传递的参数
     * @return 成功删除的数据条数，删除失败返回-1
     */
    int delete(CacheKey<?> key, Where where, Object... args) throws Exception;

    /**
     * 通过匹配条件删除数据，底层也会根据CacheKey列表维护缓存
     * 对应数据实现接口为{@link CacheLoader#delete(List, Where, DBTemplate, Object...)}
     * 注意：此接口删除是业务自己定义删除条件，底层通过CacheKey列表维护缓存，
     * 所以业务要注意缓存Key列表的删除匹配条件的删除条数一致，避免批量删除后缓存和数据库数据记录不一致
     *
     * @param  keys  数据Key列表
     *               因为每个删除Key不同，就不限制Key的主键定义，允许为空或者为NoCacheKey
     * @param  where 删除的匹配条件
     * @param  args  业务方传递的参数
     * @return 成功删除的数据条数，删除失败返回-1
     */
    int delete(List<CacheKey<?>> keys, Where where, Object... args) throws Exception;

    /**
     * 基于主键的数据更新，
     * 对应数据实现接口为{@link CacheLoader#update(CacheKey, Object, ProtoSchema, DBTemplate, Object...)}
     *
     * @param  key    存储Key
     * @param  value  存储数据
     * @param  schema 数据元信息，用于序列化/反序列化
     * @param  args   业务方传递的参数
     * @return 成功更新的数据条数，更新失败返回-1
     */
    int update(K key, V value, ProtoSchema schema, Object... args) throws Exception;

    /**
     * 基于主键的数据批量更新，
     * 对应数据实现接口为{@link CacheLoader#update(List, List, ProtoSchema, DBTemplate, Object...)}
     *
     * @param  keys   Key列表
     * @param  values 更新数据列表
     * @param  schema 数据元信息，用于序列化/反序列化
     * @param  args   业务方传递的参数
     * @return 成功更新的数据条数，更新失败返回-1
     */
    int update(List<K> keys, List<V> values, ProtoSchema schema, Object... args) throws Exception;

    /**
     * 通过匹配条件进行数据更新，
     * 对应数据实现接口为{@link CacheLoader#update(CacheKey, Updater, ProtoSchema, DBTemplate, Object...)}
     * 注意：此接口更新是业务自己定义更新匹配条件，不通过主键来更新，
     * 则当业务自己定义匹配条件更新时注意要做好对应缓存清除保证数据的一致性
     *
     * @param  key     存储Key
     * @param  updater 自定义更新器
     * @param  schema  数据元信息，用于序列化/反序列化
     * @param  args    业务方传递的参数
     * @return 成功更新的数据条数，更新失败返回-1
     */
    int update(CacheKey<?> key, Updater updater, ProtoSchema schema, Object... args) throws Exception;

    /**
     * 通过匹配条件进行数据批量更新，
     * 对应数据实现接口为{@link CacheLoader#update(List, Updater, ProtoSchema, DBTemplate, Object...)}
     * 注意：此接口更新是业务自己定义更新匹配条件，不通过主键来更新，
     * 则当业务自己定义匹配条件更新时注意要做好对应缓存清除保证数据的一致性
     *
     * @param  keys   Key列表
     * @param  updater 自定义更新器
     * @param  schema  数据元信息，用于序列化/反序列化
     * @param  args    业务方传递的参数
     * @return 成功更新的数据条数，更新失败返回-1
     */
    int update(List<CacheKey<?>> keys, Updater updater, ProtoSchema schema, Object... args) throws Exception;

    /**
     * 二级Key数据存储，对应数据获取方法为{@link #hget(CacheKey, Object, ProtoSchema, Object...)}，
     * 和{@link #put(CacheKey, Object, ProtoSchema, Object...)}方法互斥，
     * 不能一个存储里面既能存储一级数据，又能存储二级数据，这样在同样Key的情况会有数据覆盖风险，
     * 对应数据实现接口为{@link CacheLoader#hadd(CacheKey, Object, Object, ProtoSchema, DBTemplate, Ref, Object...)}
     *
     * @param  key    存储Key
     * @param  field  缓存二级Key
     * @param  value  存储数据
     * @param  schema 数据元信息，用于序列化/反序列化
     * @param  args   业务方传递的参数
     * @return 成功存储的数据条数，存储失败返回-1
     */
    int hput(K key, Object field, V value, ProtoSchema schema, Object... args) throws Exception;

    /**
     * 获取二级数据，
     * 对应数据实现接口为{@link CacheLoader#hload(CacheKey, ProtoSchema, DBTemplate, Object...)}
     *
     * @param key    数据Key
     * @param field  缓存二级Key
     * @param schema 数据元信息，用于序列化/反序列化
     * @param args   业务方传递的参数
     */
    V hget(K key, Object field, ProtoSchema schema, Object... args) throws Exception;

    /**
     * 检查指定二级Key的数据是否存在，依然是获取指定数据判断是否为空来判断数据是否存在，
     * 对应数据实现接口为{@link CacheLoader#load(CacheKey, ProtoSchema, DBTemplate, Object...)}
     *
     * @param key    数据Key
     * @param field  缓存二级Key
     * @param schema 数据元信息，用于序列化/反序列化
     * @param args   业务方传递的参数
     */
    boolean hexist(K key, Object field, ProtoSchema schema, Object... args) throws Exception;

    /**
     * 获取所有二级数据，注意如果数据量过多会有OOM风险，
     * 对应数据实现接口为{@link CacheLoader#hload(CacheKey, ProtoSchema, DBTemplate, Object...)}
     *
     * @param key    数据Key
     * @param schema 数据元信息，用于序列化/反序列化
     * @param args   业务方传递的参数
     */
    List<V> hgetAll(K key, ProtoSchema schema, Object... args) throws Exception;

    /**
     * 删除二级数据，
     * 对应数据实现接口为{@link CacheLoader#hdelete(CacheKey, Object, DBTemplate, Object...)}
     *
     * @param key   数据Key
     * @param field 缓存二级Key
     * @param args  业务方传递的参数
     */
    int hdelete(K key, Object field, Object... args) throws Exception;

    /**
     * 批量删除二级数据，
     * 对应数据实现接口为{@link CacheLoader#hdelete(CacheKey, Object[], DBTemplate, Object...)}
     *
     * @param key   数据Key
     * @param fields 缓存二级Key
     * @param args   业务方传递的参数
     */
    int hdelete(K key, Object[] fields, Object... args) throws Exception;

    /**
     * 数据更新，
     * 对应数据实现接口为{@link CacheLoader#hupdate(CacheKey, Object, Object, ProtoSchema, DBTemplate, Object...)}
     *
     * @param  key    存储Key
     * @param  field  缓存二级Key
     * @param  value  存储数据
     * @param  schema 数据元信息，用于序列化/反序列化
     * @param  args   业务方传递的参数
     * @return 更新成功返回true
     */
    int hupdate(K key, Object field, V value, ProtoSchema schema, Object... args) throws Exception;

    /**
     * 缓存统计服务
     */
    CacheXStatistics getStatistics();

    /**
     * 关闭服务，释放资源
     */
    void shutdown();
}
