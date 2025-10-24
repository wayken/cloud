package cloud.apposs.cachex;

import cloud.apposs.cachex.database.Query;
import cloud.apposs.cachex.database.Updater;
import cloud.apposs.cachex.database.Where;
import cloud.apposs.protobuf.ProtoSchema;
import cloud.apposs.util.Ref;
import cloud.apposs.util.Table;

import java.util.List;

/**
 * 数据加载器，
 * 因为数据如何加载只有业务清楚，所以由业务自己实现对数据的加载，
 * 具体有如下使用场景：
 * 1、当没命中缓存时调用该服务从数据库加载数据到缓存中（查）
 * 2、当添加数据时存储该数据到数据库中（增）
 * 3、当删除数据时从数据库删除数据（删）
 * 4、当更新数据时从数据库更新数据（改）
 */
public interface CacheLoader<K extends CacheKey, V> {
    /**
     * 初始化数据加载器，{@link CacheX}创建时便会自动初始化，
     * 默认不实现，可实现数据的启动加载，提升服务性能
     *
     * @param cachex 缓存服务，
     *               因为缓存可以获取String、对象等，需要让业务方自己实现存储String或者对象，
     *               注意所有的数据必须要调用CacheX进行数据存储以便于底层能够获取到数据
     */
    void initialize(CacheX<K, V> cachex);

    /**
     * 通过索引从数据库中加载数据并添加到缓存中，
     * 和{@link CacheX#get(CacheKey, ProtoSchema, Object...)}方法对应
     *
     * @param key       缓存Key
     * @param template  数据访问接口
     * @param args      业务方传递的参数
     * @return Key对应的加载数据
     */
    V load(K key, ProtoSchema schema, DBTemplate template, Object... args) throws Exception;

    /**
     * 通过业务传递的查询条件从数据库中加载数据并添加到缓存中，
     * 和{@link CacheX#select(CacheKey, Query, ProtoSchema, Object...)}方法对应
     *
     * @param key       缓存Key
     * @param query     查询条件
     * @param template  数据访问接口
     * @param args      业务方传递的参数
     * @return Key对应的加载数据
     */
    V select(CacheKey<?> key, Query query, ProtoSchema schema, DBTemplate template, Object... args) throws Exception;

    /**
     * 通过业务传递的查询条件从数据库中批量加载数据并添加到缓存中，
     * 和{@link CacheX#query(CacheKey, Query, ProtoSchema, Object...)}方法对应
     *
     * @param key       缓存Key
     * @param query     查询条件
     * @param template  数据访问接口
     * @param args      业务方传递的参数
     * @return Key对应的加载数据
     */
    Table<V> query(CacheKey<?> key, Query query, ProtoSchema schema, DBTemplate template, Object... args) throws Exception;

    /**
     * 添加数据到数据库中并添加到缓存中，
     * 和{@link CacheX#put(CacheKey, Object, ProtoSchema, Object...)}方法对应
     *
     * @param key       缓存Key
     * @param value     缓存数据
     * @param template  数据访问接口
     * @param idRef     数据存储后生成的主键ID，
     *                  如果返回非空则会设置到{@link K#setPrimary(Object)}中，CacheX会根据该主键是否存在以决定是否缓存，
     *                  注意：如果业务是自己生成ID（通过IdWorker生成等），则数据库不会生成主键，这个由业务自己定义Key规则传递进来
     * @param args      业务方传递的参数
     * @return 添加成功的数据条数
     */
    int add(K key, V value, ProtoSchema schema, DBTemplate template, Ref<Object> idRef, Object... args) throws Exception;

    /**
     * 批量添加数据到数据库中并添加到缓存中，
     * 和{@link CacheX#put(List, List, ProtoSchema, Object...)}方法对应
     *
     * @param keys      主键列表
     * @param values    存储的数据集
     * @param template  数据访问接口
     * @param idRefs    数据存储后生成的主键ID列表，目前该参数没有意义
     * @param args      业务方传递的参数
     * @return 添加成功的数据条数
     */
    int add(List<K>keys, List<V> values, ProtoSchema schema, DBTemplate template, List<Object> idRefs, Object... args) throws Exception;

    /**
     * 添加数据到数据库中并添加到缓存中，存在则更新，不存在则插入
     * 和{@link CacheX#replace(CacheKey, Object, ProtoSchema, Object...)}方法对应
     *
     * @param key       缓存Key
     * @param value     缓存数据
     * @param template  数据访问接口
     * @param idRef     数据存储后生成的主键ID，
     *                  如果返回非空则会设置到{@link K#setPrimary(Object)}中，CacheX会根据该主键是否存在以决定是否缓存，
     *                  注意：如果业务是自己生成ID（通过IdWorker生成等），则数据库不会生成主键，这个由业务自己定义Key规则传递进来
     * @param args      业务方传递的参数
     * @return 添加成功的数据条数
     */
    int replace(K key, V value, ProtoSchema schema, DBTemplate template, Ref<Object> idRef, Object... args) throws Exception;

    /**
     * 通过主键删除数据，
     * 和{@link CacheX#delete(CacheKey, Object...)}方法对应
     *
     * @param key       缓存Key
     * @param template  数据访问接口
     * @param args      业务方传递的参数
     * @return 删除成功的数据条数
     */
    int delete(K key, DBTemplate template, Object... args) throws Exception;

    /**
     * 通过主键列表批量删除数据，
     * 和{@link CacheX#delete(List, Object...)}方法对应
     *
     * @param keys      缓存Key列表
     * @param template  数据访问接口
     * @param args      业务方传递的参数
     * @return 删除成功的数据条数
     */
    int delete(List<K> keys, DBTemplate template, Object... args) throws Exception;

    /**
     * 通过匹配条件删除数据，
     * 和{@link CacheX#delete(CacheKey, Where, Object...)}方法对应
     *
     * @param key       缓存Key
     * @param template  数据访问接口
     * @param where     删除的匹配条件
     * @param args      业务方传递的参数
     * @return 删除成功的数据条数
     */
    int delete(CacheKey<?> key, Where where, DBTemplate template, Object... args) throws Exception;

    /**
     * 通过匹配条件批量删除数据，
     * 和{@link CacheX#delete(List, Where, Object...)}方法对应
     *
     * @param keys      缓存Key列表
     * @param template  数据访问接口
     * @param where     删除的匹配条件
     * @param args      业务方传递的参数
     * @return 删除成功的数据条数
     */
    int delete(List<CacheKey<?>> keys, Where where, DBTemplate template, Object... args) throws Exception;

    /**
     * 通过主键从数据库中更新数据，同步更新缓存
     * 和{@link CacheX#update(CacheKey, Object, ProtoSchema, Object...)}方法对应
     *
     * @param key       缓存Key
     * @param value     更新数据
     * @param template  数据访问接口
     * @param args      业务方传递的参数
     * @return 更新成功的数据条数
     */
    int update(K key, V value, ProtoSchema schema, DBTemplate template, Object... args) throws Exception;

    /**
     * 通过主键从数据库中批量更新数据，同步更新缓存
     * 和{@link CacheX#update(List, List, ProtoSchema, Object...)}方法对应
     *
     * @param keys      缓存Key列表
     * @param values    更新数据列表
     * @param template  数据访问接口
     * @param args      业务方传递的参数
     * @return 更新成功的数据条数
     */
    int update(List<K> keys, List<V> values, ProtoSchema schema, DBTemplate template, Object... args) throws Exception;

    /**
     * 通过匹配条件更新数据，
     * 和{@link CacheX#update(CacheKey, Updater, ProtoSchema, Object...)}方法对应
     *
     * @param key       缓存Key
     * @param updater   自定义更新器
     * @param template  数据访问接口
     * @param args      业务方传递的参数
     * @return 更新成功的数据条数
     */
    int update(CacheKey<?> key, Updater updater, ProtoSchema schema, DBTemplate template, Object... args) throws Exception;

    /**
     * 通过匹配条件批量更新数据，
     * 和{@link CacheX#update(List, Updater, ProtoSchema, Object...)}方法对应
     *
     * @param keys      缓存Key列表
     * @param updater   自定义更新器
     * @param template  数据访问接口
     * @param args      业务方传递的参数
     * @return 更新成功的数据条数
     */
    int update(List<CacheKey<?>> keys, Updater updater, ProtoSchema schema, DBTemplate template, Object... args) throws Exception;

    /**
     * 从数据库中加载所有二级数据并添加到缓存中，底层Cache的数据结构是Hash，即：Key->Hash
     * 和{@link CacheX#hget(CacheKey, Object, ProtoSchema, Object...)}方法对应
     *
     * @param key       缓存Key
     * @param template  数据访问接口
     * @param args      业务方传递的参数
     * @return Key对应的加载数据
     */
    List<V> hload(K key, ProtoSchema schema, DBTemplate template, Object... args) throws Exception;

    /**
     * 添加数据到数据库中并添加到缓存中，底层Cache的数据结构是Hash，即：Key->Hash
     * 和{@link CacheX#hput(CacheKey, Object, Object, ProtoSchema, Object...)}方法对应，
     *
     * @param key       缓存Key
     * @param field     二级缓存Key
     * @param value     缓存数据
     * @param template  数据访问接口
     * @param idRef     数据存储后生成的主键ID，如果返回非空则会设置到{@link K#setPrimary(Object)}中
     * @param args      业务方传递的参数
     * @return 添加成功的数据条数
     */
    int hadd(K key, Object field, V value, ProtoSchema schema, DBTemplate template, Ref<Object> idRef, Object... args) throws Exception;

    /**
     * 删除二级缓存数据，
     * 和{@link CacheX#hdelete(CacheKey, Object, Object...)}方法对应
     *
     * @param key       缓存Key
     * @param field     二级缓存Key
     * @param template  数据访问接口
     * @param args      业务方传递的参数
     * @return 删除成功的数据条数
     */
    int hdelete(K key, Object field, DBTemplate template, Object... args) throws Exception;

    /**
     * 批量删除二级缓存数据，
     * 和{@link CacheX#hdelete(CacheKey, Object, Object...)}方法对应
     *
     * @param key       缓存Key
     * @param fields    二级缓存Key
     * @param template  数据访问接口
     * @param args      业务方传递的参数
     * @return 删除成功的数据条数
     */
    int hdelete(K key, Object[] fields, DBTemplate template, Object... args) throws Exception;

    /**
     * 从数据库中更新数据，
     * 和{@link CacheX#hupdate(CacheKey, Object, Object, ProtoSchema, Object...)}方法对应
     *
     * @param key       缓存Key
     * @param field     二级缓存Key
     * @param value     更新数据
     * @param template  数据访问接口
     * @param args      业务方传递的参数
     * @return 更新成功的数据条数
     */
    int hupdate(K key, Object field, V value, ProtoSchema schema, DBTemplate template, Object... args) throws Exception;

    /**
     * 生成业务二级缓存Key，
     * 主要场景为{@link #hload(CacheKey, ProtoSchema, DBTemplate, Object...)}加载所有数据后，
     * 需要自定义业务方自己的二级缓存Key
     */
    String getField(V info);
}
