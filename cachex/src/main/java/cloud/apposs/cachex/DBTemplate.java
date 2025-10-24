package cloud.apposs.cachex;

import cloud.apposs.cachex.database.*;
import cloud.apposs.protobuf.ProtoSchema;
import cloud.apposs.util.Ref;
import cloud.apposs.util.Table;

import java.util.List;

/**
 * 数据库操作模板接口，定义了常用的数据库操作方法，包括查询、更新和删除等操作，实现类有
 * <pre>
 *     1. JDBCTemplate - 基于JDBC的数据库操作实现
 *     2. MongoTemplate - 基于MongoDB的数据库操作实现
 * </pre>
 */
public interface DBTemplate {
    /**
     * 数据查询，只查询一条数据
     *
     * @param table 数据表
     * @return 实体对象，以Key->Value结构存储
     */
    default Entity select(String table) throws Exception {
        return select(table, null, null, null, null);
    }

    /**
     * 数据查询，只查询一条数据
     *
     * @param table   数据表
     * @param primary 数据主键字段，后续{@link Entity}可依据此主键做删、改操作，可为空
     * @return 实体对象，以Key->Value结构存储
     */
    default Entity select(String table, String primary) throws Exception {
        return select(table, primary, null, null, null);
    }

    /**
     * 数据查询，只查询一条数据
     *
     * @param table 数据表
     * @param query 查询条件，可为空
     * @return 实体对象，以Key->Value结构存储
     */
    default Entity select(String table, Query query) throws Exception {
        return select(table, null, null, null, query);
    }

    /**
     * 数据查询，只查询一条数据
     *
     * @param table   数据表
     * @param primary 数据主键字段，后续{@link Entity}可依据此主键做删、改操作，可为空
     * @param query   查询条件，可为空
     * @return 实体对象，以Key->Value结构存储
     */
    default Entity select(String table, String primary, Query query) throws Exception {
        return select(table, primary, null, null, query);
    }

    /**
     * 数据查询，只查询一条数据，采用主键查询
     *
     * @param table    数据表
     * @param primary  数据主键字段，后续{@link Entity}可依据此主键做删、改操作，可为空
     * @param identity 数据主键值，和primary配合使用根据主键查询，可为空
     * @return 实体对象，以Key->Value结构存储
     */
    default Entity select(String table, String primary, Object identity) throws Exception {
        return select(table, primary, identity, null, null);
    }

    /**
     * 数据查询，只查询一条数据
     *
     * @param table    数据表
     * @param identity 数据主键值，和primary配合使用根据主键查询，可为空
     * @param schema   数据元信息，如果存储的是字节则需要此元信息进行数据序列化/反序列化，可为空
     * @return 实体对象，以Key->Value结构存储
     */
    default Entity select(String table, Object identity, ProtoSchema schema) throws Exception {
        return select(table, null, identity, schema, null);
    }

    /**
     * 数据查询，只查询一条数据
     *
     * @param table    数据表
     * @param primary  数据主键字段，后续{@link Entity}可依据此主键做删、改操作，可为空
     * @param identity 数据主键值，和primary配合使用根据主键查询，可为空
     * @param schema   数据元信息，如果存储的是字节则需要此元信息进行数据序列化/反序列化，可为空
     * @param query    查询条件，可为空
     * @return 实体对象，以Key->Value结构存储
     */
    Entity select(String table, String primary, Object identity, ProtoSchema schema, Query query) throws Exception;

    /**
     * 数据查询，查询表所有数据，
     * 注意表数据量如果很多的话会有OOM爆内存风险，建议任何查询都采用分页查询
     *
     * @param table 数据表
     * @return 实体对象集合，以Key->Value结构存储
     */
    default Table<Entity> query(String table) throws Exception {
        return query(table, null, null, null);
    }

    /**
     * 数据查询，查询表所有数据，
     * 注意表数据量如果很多的话会有OOM爆内存风险，建议任何查询都采用分页查询
     *
     * @param table   数据表
     * @param primary 数据主键字段，后续{@link Entity}可依据此主键做删、改操作，可为空
     * @return 实体对象集合，以Key->Value结构存储
     */
    default Table<Entity> query(String table, String primary) throws Exception {
        return query(table, primary, null, null);
    }

    /**
     * 数据查询，根据查询条件查询表所有数据
     */
    default Table<Entity> query(String table, Query query) throws Exception {
        return query(table, null, null, query);
    }

    /**
     * 数据查询，根据查询条件查询表所有数据
     *
     * @param table  数据表
     * @param schema 数据元信息，如果存储的是字节则需要此元信息进行数据序列化/反序列化，可为空
     * @param query  查询条件，可为空
     * @return 实体对象集合，以Key->Value结构存储
     */
    default Table<Entity> query(String table, ProtoSchema schema, Query query) throws Exception {
        return query(table, null, schema, query);
    }

    /**
     * 数据查询，根据查询条件查询表所有数据
     *
     * @param table  数据表
     * @param schema 数据元信息，如果存储的是字节则需要此元信息进行数据序列化/反序列化，可为空
     * @return 实体对象集合，以Key->Value结构存储
     */
    default Table<Entity> query(String table, ProtoSchema schema) throws Exception {
        return query(table, null, schema, null);
    }

    /**
     * 数据查询，查询所有匹配条件的数据
     *
     * @param table   数据表
     * @param primary 数据主键字段，后续{@link Entity}可依据此主键做删、改操作，可为空
     * @param schema  数据元信息，如果存储的是字节则需要此元信息进行数据序列化/反序列化，可为空
     * @param query   查询条件，可为空
     * @return 实体对象集合，以Key->Value结构存储
     */
    Table<Entity> query(String table, String primary, ProtoSchema schema, Query query) throws Exception;

    /**
     * 数据更新，根据{@link Entity}主键更新，注意主键不能为空
     *
     * @param table  数据表
     * @param entity 实体对象，必须存在主键
     * @return 成功更新的数据条数，更新失败返回-1
     */
    default int update(String table, Entity entity) throws Exception {
        return update(table, entity, null);
    }

    /**
     * 数据更新，根据{@link Entity}主键更新，注意主键不能为空
     *
     * @param table  数据表
     * @param entity 实体对象，必须存在主键
     * @param schema 数据元信息，如果存储的是字节则需要此元信息进行数据序列化/反序列化
     * @return 成功更新的数据条数，更新失败返回-1
     */
    int update(String table, Entity entity, ProtoSchema schema) throws Exception;

    /**
     * 数据更新，根据{@link Entity}主键更新，注意主键不能为空
     *
     * @param table    数据表
     * @param entities 实体对象列表，每个实体对象必须存在主键
     * @return 成功更新数据条数，更新失败返回-1
     */
    default int update(String table, List<Entity> entities) throws Exception {
        return update(table, entities, null);
    }

    /**
     * 数据更新，根据{@link Entity}主键更新，注意主键不能为空
     *
     * @param table    数据表
     * @param entities 实体对象列表，每个实体对象必须存在主键
     * @param schema   数据元信息，如果存储的是字节则需要此元信息进行数据序列化/反序列化
     * @return 成功更新数据条数，更新失败返回-1
     */
    int update(String table, List<Entity> entities, ProtoSchema schema) throws Exception;

    /**
     * 数据更新，根据{@link Updater}条件更新
     *
     * @return 成功更新数据条数，更新失败返回-1
     */
    int update(String table, Updater updater) throws Exception;

    /**
     * 数据删除，根据{@link Entity}主键删除，注意主键不能为空
     *
     * @param  table  数据库表
     * @param  entity 实体对象，必须存在主键
     * @return 成功删除返回true
     */
    int delete(String table, Entity entity) throws Exception;

    /**
     * 数据删除，根据主键删除
     *
     * @param table    数据库表
     * @param primary  表主键字段
     * @param identity 表主键值
     * @return 成功删除的数据条数
     */
    int delete(String table, String primary, Object identity) throws Exception;

    /**
     * 批量数据删除，根据{@link Entity}主键删除，注意主键不能为空
     *
     * @return 成功删除数据条数，删除失败返回-1
     */
    int delete(String table, List<Entity> entities) throws Exception;

    /**
     * 数据删除，根据{@link Where}条件删除
     *
     * @return 成功删除数据条数，删除失败返回-1
     */
    int delete(String table, Where where) throws Exception;

    /**
     * 数据插入
     *
     * @param table  数据库表
     * @param entity 数据条目
     * @return 成功插入数据条数，插入失败返回-1
     */
    default int insert(String table, Entity entity) throws Exception {
        return insert(table, entity, null, null);
    }

    /**
     * 数据插入
     *
     * @param table  数据库表
     * @param entity 数据条目
     * @param idRef  是否获取生成的ID，为NULL则不获取
     * @return 成功插入数据条数，插入失败返回-1
     */
    default int insert(String table, Entity entity, Ref<Object> idRef) throws Exception {
        return insert(table, entity, null, idRef);
    }

    /**
     * 数据插入
     *
     * @param table  数据库表
     * @param entity 数据条目
     * @param schema 数据元信息，如果存储的是字节则需要此元信息进行数据序列化/反序列化
     * @param idRef  是否获取生成的ID，为NULL则不获取
     * @return 成功插入数据条数，插入失败返回-1
     */
    int insert(String table, Entity entity, ProtoSchema schema, Ref<Object> idRef) throws Exception;

    /**
     * 数据批量插入
     *
     * @param table    数据库表
     * @param entities 数据条目列表
     * @return 成功插入数据条数，插入失败返回-1
     */
    default int insert(String table, List<Entity> entities) throws Exception {
        return insert(table, entities, null, null);
    }

    /**
     * 数据批量插入
     *
     * @param table    数据库表
     * @param entities 数据条目列表
     * @param idList   是否获取生成的ID列表，为NULL则不获取
     * @return 成功插入数据条数，插入失败返回-1
     */
    default int insert(String table, List<Entity> entities, List<Object> idList) throws Exception {
        return insert(table, entities, null, idList);
    }

    /**
     * 数据批量插入
     *
     * @param table    数据库表
     * @param entities 数据条目列表
     * @param schema   数据元信息，如果存储的是字节则需要此元信息进行数据序列化/反序列化
     * @return 成功插入数据条数，插入失败返回-1
     */
    default int insert(String table, List<Entity> entities, ProtoSchema schema) throws Exception {
        return insert(table, entities, schema, null);
    }

    /**
     * 数据批量插入
     *
     * @param table    数据库表
     * @param entities 数据条目列表
     * @param schema   数据元信息，如果存储的是字节则需要此元信息进行数据序列化/反序列化
     * @param idList   是否获取生成的ID列表，为NULL则不获取
     * @return 成功插入数据条数，插入失败返回-1
     */
    int insert(String table, List<Entity> entities, ProtoSchema schema, List<Object> idList) throws Exception;

    /**
     * 数据替换，注意entity必须要有主键才能替换
     *
     * @param table  数据库表
     * @param entity 数据条目
     * @return 成功插入数据条数，插入失败返回-1
     */
    default int replace(String table, Entity entity) throws Exception {
        return replace(table, entity, null, null);
    }

    /**
     * 数据替换，注意entity必须要有主键才能替换
     *
     * @param table  数据库表
     * @param entity 数据条目
     * @param idRef  是否获取生成的ID，为NULL则不获取
     * @return 成功插入数据条数，插入失败返回-1
     */
    default int replace(String table, Entity entity, Ref<Object> idRef) throws Exception {
        return replace(table, entity, null, idRef);
    }

    /**
     * 数据替换，注意entity必须要有主键才能替换
     *
     * @param table  数据库表
     * @param entity 数据条目
     * @param schema 数据元信息，如果存储的是字节则需要此元信息进行数据序列化/反序列化
     * @param idRef  是否获取生成的ID，为NULL则不获取
     * @return 成功替换数据条数，替换失败返回-1
     */
    int replace(String table, Entity entity, ProtoSchema schema, Ref<Object> idRef) throws Exception;

    /**
     * 创建数据表
     *
     * @param metadata 表格元信息
     */
    default boolean create(Metadata metadata) throws Exception {
        return create(metadata, false);
    }

    /**
     * 创建数据表
     *
     * @param metadata    表格元信息
     * @param dropIfExist 当表格已经存在是否删除
     * @return 创建成功返回true
     */
    boolean create(Metadata metadata, boolean dropIfExist) throws Exception;

    /**
     * 数据数据表是否已经存在
     *
     * @param metadata 表格元信息
     */
    boolean exist(Metadata metadata) throws Exception;

    /**
     * 关闭数据库连接，释放资源
     */
    void shutdown();
}
