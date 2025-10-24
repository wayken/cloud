package cloud.apposs.cachex.jdbc;

import cloud.apposs.cachex.database.*;
import cloud.apposs.cachex.database.Where.Condition;
import cloud.apposs.cachex.jdbc.listener.DaoListener;
import cloud.apposs.cachex.jdbc.listener.DaoListenerSupport;
import cloud.apposs.cachex.jdbc.operator.ByteBufferStatementBuilder;
import cloud.apposs.cachex.jdbc.operator.BytesStatementBuilder;
import cloud.apposs.cachex.jdbc.operator.CalendarStatementBuilder;
import cloud.apposs.cachex.jdbc.operator.ConditionBuilder;
import cloud.apposs.cachex.jdbc.operator.DateStatementBuilder;
import cloud.apposs.cachex.jdbc.operator.DoubleStatementBuilder;
import cloud.apposs.cachex.jdbc.operator.FloatStatementBuilder;
import cloud.apposs.cachex.jdbc.operator.InBuilder;
import cloud.apposs.cachex.jdbc.operator.IntStatementBuilder;
import cloud.apposs.cachex.jdbc.operator.LikeBuilder;
import cloud.apposs.cachex.jdbc.operator.ListStatementBuilder;
import cloud.apposs.cachex.jdbc.operator.LongStatementBuilder;
import cloud.apposs.cachex.jdbc.operator.NullStatementBuilder;
import cloud.apposs.cachex.jdbc.operator.ShortStatementBuilder;
import cloud.apposs.cachex.jdbc.operator.StatementBuilder;
import cloud.apposs.cachex.jdbc.operator.StringStatementBuilder;
import cloud.apposs.logger.Logger;
import cloud.apposs.protobuf.ProtoSchema;
import cloud.apposs.util.Null;
import cloud.apposs.util.Param;
import cloud.apposs.util.Parser;
import cloud.apposs.util.Ref;
import cloud.apposs.util.StrUtil;
import cloud.apposs.util.Table;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 数据访问接口，为不同的数据库存储提供统一的接口调用，
 * 服务于业务方调用，一个实例对应一个数据库连接，
 * 真正的数据增删改查均是通过{@link SqlBuilder}实现，新增新的数据库支持需要做以下几个步骤，
 * <pre>
 * 1、实现连接池以提升性能
 * 2、实现{@link SqlBuilder}接口，包括对{@link Query}/{@link Updater}/{@link Where}/{@link Pager}的解析细节
 * </pre>
 */
public class Dao {
    /**
     * 将JAVA数据类型转换为SQL数据类型的转换器
     */
    protected static final Map<Class<?>, StatementBuilder> statementBuilders =
            new HashMap<Class<?>, StatementBuilder>();
    static {
        statementBuilders.put(int.class, new IntStatementBuilder());
        statementBuilders.put(Integer.class, new IntStatementBuilder());
        statementBuilders.put(long.class, new LongStatementBuilder());
        statementBuilders.put(Long.class, new LongStatementBuilder());
        statementBuilders.put(float.class, new FloatStatementBuilder());
        statementBuilders.put(Float.class, new FloatStatementBuilder());
        statementBuilders.put(double.class, new DoubleStatementBuilder());
        statementBuilders.put(Double.class, new DoubleStatementBuilder());
        statementBuilders.put(short.class, new ShortStatementBuilder());
        statementBuilders.put(Short.class, new ShortStatementBuilder());
        statementBuilders.put(String.class, new StringStatementBuilder());
        statementBuilders.put(List.class, new ListStatementBuilder());
        statementBuilders.put(ByteBuffer.class, new ByteBufferStatementBuilder());
        statementBuilders.put(byte.class, new BytesStatementBuilder());
        statementBuilders.put(GregorianCalendar.class, new CalendarStatementBuilder());
        statementBuilders.put(Calendar.class, new CalendarStatementBuilder());
        statementBuilders.put(Date.class, new DateStatementBuilder());
        statementBuilders.put(Null.class, new NullStatementBuilder());
    }

    /**
     * 查询条件转换器
     */
    protected static final Map<String, ConditionBuilder> conditionBuilders = new HashMap<String, ConditionBuilder>();
    static {
        conditionBuilders.put(Where.LK, new LikeBuilder());
        conditionBuilders.put(Where.NL, new LikeBuilder());
        conditionBuilders.put(Where.IN, new InBuilder());
    }

    /**
     * 数据增删改查SQL生成类
     */
    private final SqlBuilder builder;

    /**
     * 数据库连接
     */
    private final Connection connection;

    /**
     * 开发模式下输出执行的SQL语句
     */
    protected boolean debuggable = false;

    /**
     * 数据库操作监听列表
     */
    private final DaoListenerSupport listenerSupport = new DaoListenerSupport();

    public Dao(Connection connection, SqlBuilder builder) {
        this.connection = connection;
        this.builder = builder;
    }

    /**
     * 添加DAO数据操作监听，可以用于慢日志查询监控等
     */
    public void addListener(DaoListener listener) {
        listenerSupport.addListener(listener);
    }

    public Dao debuggable(boolean debuggable) {
        this.debuggable = debuggable;
        return this;
    }

    public static Map<Class<?>, StatementBuilder> getStatementBuilders() {
        return statementBuilders;
    }

    public static Map<String, ConditionBuilder> getConditionBuilders() {
        return conditionBuilders;
    }

    /**
     * 数据查询，只查询一条数据
     *
     * @param table 数据表
     * @return 实体对象，以Key->Value结构存储
     */
    public Entity select(String table) throws Exception {
        return select(table, null, null, null, null);
    }

    /**
     * 数据查询，只查询一条数据
     *
     * @param table   数据表
     * @param primary 数据主键字段，后续{@link Entity}可依据此主键做删、改操作，可为空
     * @return 实体对象，以Key->Value结构存储
     */
    public Entity select(String table, String primary) throws Exception {
        return select(table, primary, null, null, null);
    }

    /**
     * 数据查询，只查询一条数据
     *
     * @param table 数据表
     * @param query 查询条件，可为空
     * @return 实体对象，以Key->Value结构存储
     */
    public Entity select(String table, Query query) throws Exception {
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
    public Entity select(String table, String primary, Query query) throws Exception {
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
    public Entity select(String table, String primary, Object identity) throws Exception {
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
    public Entity select(String table, Object identity, ProtoSchema schema) throws Exception {
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
    public Entity select(String table, String primary, Object identity, ProtoSchema schema, Query query) throws Exception {
        if (StrUtil.isEmpty(table)) {
            throw new IllegalArgumentException("table");
        }

        if (query == null) {
            query = new Query().limit(0, 1);
        } else {
            query.limit(0, 1);
        }

        try {
            listenerSupport.selectStart(table, primary, identity, query);
            String sql = builder.generateQuerySql(table, primary, identity, query);
            Entity entity = doExecuteSelect(sql, primary, query);
            listenerSupport.selectComplete(table, primary, identity, query, null);
            return entity;
        } catch (Exception e) {
            listenerSupport.selectComplete(table, primary, identity, query, e);
            throw e;
        }
    }

    /**
     * 数据查询，查询表所有数据，
     * 注意表数据量如果很多的话会有OOM爆内存风险，建议任何查询都采用分页查询
     *
     * @param table 数据表
     * @return 实体对象集合，以Key->Value结构存储
     */
    public Table<Entity> query(String table) throws Exception {
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
    public Table<Entity> query(String table, String primary) throws Exception {
        return query(table, primary, null, null);
    }

    /**
     * 数据查询，根据查询条件查询表所有数据
     */
    public Table<Entity> query(String table, Query query) throws Exception {
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
    public Table<Entity> query(String table, ProtoSchema schema, Query query) throws Exception {
        return query(table, null, schema, query);
    }

    /**
     * 数据查询，根据查询条件查询表所有数据
     *
     * @param table  数据表
     * @param schema 数据元信息，如果存储的是字节则需要此元信息进行数据序列化/反序列化，可为空
     * @return 实体对象集合，以Key->Value结构存储
     */
    public Table<Entity> query(String table, ProtoSchema schema) throws Exception {
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
    public Table<Entity> query(String table, String primary, ProtoSchema schema, Query query) throws Exception {
        if (StrUtil.isEmpty(table)) {
            throw new IllegalArgumentException("table");
        }

        try {
            listenerSupport.queryStart(table, primary, query);
            String sql = builder.generateQuerySql(table, primary, null, query);
            Table<Entity> entities = doExecuteQuery(sql, primary, query);
            listenerSupport.queryComplete(table, primary, query, null);
            return entities;
        } catch (Exception e) {
            listenerSupport.queryComplete(table, primary, query, e);
            throw e;
        }
    }

    /**
     * 数据更新，根据{@link Entity}主键更新，注意主键不能为空
     *
     * @param table  数据表
     * @param entity 实体对象，必须存在主键
     * @return 成功更新的数据条数，更新失败返回-1
     */
    public int update(String table, Entity entity) throws Exception {
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
    public int update(String table, Entity entity, ProtoSchema schema) throws Exception {
        if (StrUtil.isEmpty(table)) {
            throw new IllegalArgumentException("table");
        }
        if (entity == null || entity.isEmpty()) {
            throw new IllegalArgumentException("entity");
        }
        // Entity必须要有主键来做WHERE范围判断，不然一不小心更新全部就GG了
        if (StrUtil.isEmpty(entity.getPrimary()) || entity.getIdentity() == null) {
            throw new IllegalArgumentException("Entity has no identity");
        }

        try {
            listenerSupport.updateStart(table, entity);
            String sql = builder.generateUpdateSql(table, entity);
            int count = doExecuteUpdate(sql, entity);
            listenerSupport.updateComplete(table, entity, null);
            return count;
        } catch (Exception e) {
            listenerSupport.updateComplete(table, entity, e);
            throw e;
        }
    }

    /**
     * 数据更新，根据{@link Entity}主键更新，注意主键不能为空
     *
     * @param table    数据表
     * @param entities 实体对象列表，每个实体对象必须存在主键
     * @return 成功更新数据条数，更新失败返回-1
     */
    public int update(String table, List<Entity> entities) throws Exception {
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
    public int update(String table, List<Entity> entities, ProtoSchema schema) throws Exception {
        if (StrUtil.isEmpty(table)) {
            throw new IllegalArgumentException("table");
        }
        if (entities == null || entities.isEmpty()) {
            throw new IllegalArgumentException("entities");
        }
        // Entity必须要有主键来做WHERE范围判断，不然一不小心更新全部就GG了
        String primary = entities.get(0).getPrimary();
        for (Entity entity : entities) {
            if (StrUtil.isEmpty(entity.getPrimary()) || !primary.equals(entity.getPrimary())) {
                throw new IllegalArgumentException("Entity Primary No The Same");
            }
            if (entity.getIdentity() == null) {
                throw new IllegalArgumentException("Entity Has No Identity");
            }
        }

        try {
            listenerSupport.updateStart(table, entities);
            String sql = builder.generateUpdateSql(table, entities.get(0));
            int count = doExecuteUpdateBatch(sql, entities);
            listenerSupport.updateComplete(table, entities, null);
            return count;
        } catch (Exception e) {
            listenerSupport.updateComplete(table, entities, e);
            throw e;
        }
    }

    /**
     * 数据更新，根据{@link Updater}条件更新
     *
     * @return 成功更新数据条数，更新失败返回-1
     */
    public int update(String table, Updater updater) throws Exception {
        if (StrUtil.isEmpty(table)) {
            throw new IllegalArgumentException("table");
        }
        if (updater == null || updater.isEmpty()) {
            throw new IllegalArgumentException("updater");
        }
        // Updater必须要有WHERE范围判断，不然一不小心更新全部就GG了
        if (updater.where() == null || updater.where().isEmpty()) {
            throw new IllegalArgumentException("Where Not Specified");
        }

        try {
            listenerSupport.updateStart(table, updater);
            String sql = builder.generateUpdateSql(table, updater);
            int count = doExecuteUpdate(sql, updater);
            listenerSupport.updateComplete(table, updater, null);
            return count;
        } catch (Exception e) {
            listenerSupport.updateComplete(table, updater, e);
            throw e;
        }
    }

    /**
     * 数据删除，根据{@link Entity}主键删除，注意主键不能为空
     *
     * @param  table  数据库表
     * @param  entity 实体对象，必须存在主键
     * @return 成功删除返回true
     */
    public int delete(String table, Entity entity) throws Exception {
        if (StrUtil.isEmpty(table)) {
            throw new IllegalArgumentException("table");
        }
        if (entity == null) {
            throw new IllegalArgumentException("entity");
        }
        // Entity必须要有主键来做WHERE范围判断，不然一不小心删除全部就GG了
        if (StrUtil.isEmpty(entity.getPrimary()) || entity.getIdentity() == null) {
            throw new IllegalArgumentException("Entity Has No Identity");
        }

        try {
            listenerSupport.deleteStart(table, entity);
            String sql = builder.generateDeleteSql(table, entity);
            int count = doExecuteUpdate(sql, entity);
            listenerSupport.deleteComplete(table, entity, null);
            return count;
        } catch (Exception e) {
            listenerSupport.deleteComplete(table, entity, e);
            throw e;
        }
    }

    /**
     * 数据删除，根据主键删除
     *
     * @param table    数据库表
     * @param primary  表主键字段
     * @param identity 表主键值
     * @return 成功删除的数据条数
     */
    public int delete(String table, String primary, Object identity) throws Exception {
        if (StrUtil.isEmpty(table)) {
            throw new IllegalArgumentException("table");
        }
        // Entity必须要有主键来做WHERE范围判断，不然一不小心删除全部就GG了
        if (StrUtil.isEmpty(primary) || identity == null) {
            throw new IllegalArgumentException("Entity Has No Identity");
        }

        try {
            listenerSupport.deleteStart(table, primary, identity);
            String sql = builder.generateDeleteSql(table, primary, identity);
            int count = doExecuteUpdate(sql, primary, identity);
            listenerSupport.deleteComplete(table, primary, identity, null);
            return count;
        } catch (Exception e) {
            listenerSupport.deleteComplete(table, primary, identity, e);
            throw e;
        }
    }

    /**
     * 批量数据删除，根据{@link Entity}主键删除，注意主键不能为空
     *
     * @return 成功删除数据条数，删除失败返回-1
     */
    public int delete(String table, List<Entity> entities) throws Exception {
        if (StrUtil.isEmpty(table)) {
            throw new IllegalArgumentException("table");
        }
        if (entities == null || entities.isEmpty()) {
            throw new IllegalArgumentException("entities");
        }
        String primary = entities.get(0).getPrimary();
        // Entity必须要有主键来做WHERE范围判断，不然一不小心删除全部就GG了
        for (Entity entity : entities) {
            if (StrUtil.isEmpty(entity.getPrimary()) ||
                    !primary.equals(entity.getPrimary())) {
                throw new IllegalArgumentException("Entity Primary No The Same");
            }
            if (entity.getIdentity() == null) {
                throw new IllegalArgumentException("Entity Has No Identity");
            }
        }

        try {
            listenerSupport.deleteStart(table, primary, entities);
            String sql = builder.generateDeleteSql(table, entities.get(0));
            int count = doExecuteUpdateBatch(sql, entities);
            listenerSupport.deleteComplete(table, primary, entities, null);
            return count;
        } catch (Exception e) {
            listenerSupport.deleteComplete(table, primary, entities, e);
            throw e;
        }
    }

    /**
     * 数据删除，根据{@link Where}条件删除
     *
     * @return 成功删除数据条数，删除失败返回-1
     */
    public int delete(String table, Where where) throws Exception {
        if (StrUtil.isEmpty(table)) {
            throw new IllegalArgumentException("table");
        }
        // 必须要有WHERE范围判断，不然一不小心删除全部就GG了
        if (where == null || where.isEmpty()) {
            throw new IllegalArgumentException("'Where' parameter required");
        }

        try {
            listenerSupport.deleteStart(table, where);
            String sql = builder.generateDeleteSql(table, where);
            int count = doExecuteUpdate(sql, where);
            listenerSupport.deleteComplete(table, where, null);
            return count;
        } catch (Exception e) {
            listenerSupport.deleteComplete(table, where, e);
            throw e;
        }
    }

    /**
     * 数据插入
     *
     * @param table  数据库表
     * @param entity 数据条目
     * @return 成功插入数据条数，插入失败返回-1
     */
    public int insert(String table, Entity entity) throws Exception {
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
    public int insert(String table, Entity entity, Ref<Object> idRef) throws Exception {
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
    public int insert(String table, Entity entity, ProtoSchema schema, Ref<Object> idRef) throws Exception {
        if (StrUtil.isEmpty(table)) {
            throw new IllegalArgumentException("table");
        }
        if (entity == null) {
            throw new IllegalArgumentException("entity");
        }

        try {
            listenerSupport.insertStart(table, entity);
            String sql = builder.generateInertSql(table, entity);
            int count = doExecuteInsert(sql, entity, idRef);
            listenerSupport.insertComplete(table, entity, null);
            return count;
        } catch (Exception e) {
            listenerSupport.insertComplete(table, entity, e);
            throw e;
        }
    }

    /**
     * 数据批量插入
     *
     * @param table    数据库表
     * @param entities 数据条目列表
     * @return 成功插入数据条数，插入失败返回-1
     */
    public int insert(String table, List<Entity> entities) throws Exception {
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
    public int insert(String table, List<Entity> entities, List<Object> idList) throws Exception {
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
    public int insert(String table, List<Entity> entities, ProtoSchema schema) throws Exception {
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
    public int insert(String table, List<Entity> entities, ProtoSchema schema, List<Object> idList) throws Exception {
        try {
            listenerSupport.insertStart(table, entities);
            String sql = builder.generateInertSql(table, entities.get(0));
            int count = doExecuteInsertBatch(sql, entities, idList);
            listenerSupport.insertComplete(table, entities, null);
            return count;
        } catch (Exception e) {
            listenerSupport.insertComplete(table, entities, e);
            throw e;
        }
    }

    /**
     * 数据替换，注意entity必须要有主键才能替换
     *
     * @param table  数据库表
     * @param entity 数据条目
     * @return 成功插入数据条数，插入失败返回-1
     */
    public int replace(String table, Entity entity) throws Exception {
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
    public int replace(String table, Entity entity, Ref<Object> idRef) throws Exception {
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
    public int replace(String table, Entity entity, ProtoSchema schema, Ref<Object> idRef) throws Exception {
        if (StrUtil.isEmpty(table)) {
            throw new IllegalArgumentException("table");
        }
        if (entity == null || entity.isEmpty()) {
            throw new IllegalArgumentException("entity");
        }
        // Entity必须要有主键来做替换，否则replace会变成不断插入数据
        if (StrUtil.isEmpty(entity.getPrimary()) || entity.getIdentity() == null) {
            throw new IllegalArgumentException("Entity has no identity");
        }

        try {
            // 设置主键到更新数据列表中，这样才会直接做替换操作
            entity.setObject(entity.getPrimary(), entity.getIdentity());
            listenerSupport.insertStart(table, entity);
            String sql = builder.generateReplaceSql(table, entity);
            int count = doExecuteReplace(sql, entity, idRef);
            listenerSupport.replaceComplete(table, entity, null);
            return count;
        } catch (Exception e) {
            listenerSupport.replaceComplete(table, entity, e);
            throw e;
        }
    }

    /**
     * SQL数据查询，只返回第一条数据，
     * 注意执行此方法即表示该环境已经和指定的SQL服务绑定，无法做到动态更换不同的SQL服务
     *
     * @param sql SQL查询语句
     * @return 实体对象，以Key->Value结构存储
     */
    public Entity executeSelect(String sql) throws Exception {
        return executeSelect(sql, null);
    }

    /**
     * SQL数据查询，只返回第一条数据，
     * 注意执行此方法即表示该环境已经和指定的SQL服务绑定，无法做到动态更换不同的SQL服务
     *
     * @param sql     SQL查询语句
     * @param primary 数据主键字段，后续{@link Entity}可依据此主键做删、改操作，可为空
     * @return 实体对象，以Key->Value结构存储
     */
    public Entity executeSelect(String sql, String primary) throws Exception {
        try {
            listenerSupport.selectStart(sql, primary);
            Entity entity = doExecuteSelect(sql, primary, null);
            listenerSupport.selectComplete(sql, primary, null);
            return entity;
        } catch (Exception e) {
            listenerSupport.selectComplete(sql, primary, e);
            throw e;
        }
    }

    /**
     * SQL数据查询，查询所有匹配条件的数据，
     * 注意执行此方法即表示该环境已经和指定的SQL服务绑定，无法做到动态更换不同的SQL服务
     *
     * @param sql SQL查询语句
     * @return 实体对象集合，以Key->Value结构存储
     */
    public List<Entity> executeQuery(String sql) throws Exception {
        return executeQuery(sql, null);
    }

    /**
     * SQL数据查询，查询所有匹配条件的数据，
     * 注意执行此方法即表示该环境已经和指定的SQL服务绑定，无法做到动态更换不同的SQL服务
     *
     * @param sql     SQL查询语句
     * @param primary 数据主键字段，后续{@link Entity}可依据此主键做删、改操作，可为空
     * @return 实体对象集合，以Key->Value结构存储
     */
    public List<Entity> executeQuery(String sql, String primary) throws Exception {
        try {
            listenerSupport.selectStart(sql, primary);
            List<Entity> entities = doExecuteQuery(sql, primary, null);
            listenerSupport.selectComplete(sql, primary, null);
            return entities;
        } catch (Exception e) {
            listenerSupport.selectComplete(sql, primary, e);
            throw e;
        }
    }

    /**
     * 数据更新，根据SQL语句更新，
     * 注意执行此方法即表示该环境已经和指定的SQL服务绑定，无法做到动态更换不同的SQL服务
     *
     * @return 成功更新数据条数，更新失败返回-1
     */
    public int executeUpdate(String sql) throws Exception {
        try {
            listenerSupport.updateStart(sql);
            int count = doExecuteUpdate(sql, (Updater) null);
            listenerSupport.updateComplete(sql, null);
            return count;
        } catch (Exception e) {
            listenerSupport.updateComplete(sql, e);
            throw e;
        }
    }

    /**
     * 创建数据表
     *
     * @param metadata 表格元信息
     */
    public boolean create(Metadata metadata) throws Exception {
        return create(metadata, false);
    }

    /**
     * 创建数据表
     *
     * @param metadata    表格元信息
     * @param dropIfExist 当表格已经存在是否删除
     * @return 创建成功返回true
     */
    public boolean create(Metadata metadata, boolean dropIfExist) throws Exception {
        if (metadata == null || StrUtil.isEmpty(metadata.getTable())) {
            throw new IllegalArgumentException("metadata");
        }

        String table = metadata.getTable();
        if (dropIfExist && exist(metadata)) {
            String dropSql = builder.generateDropSql(table);
            executeUpdate(dropSql);
        }
        String[] sqls = builder.generateCreateSql(table, metadata);
        if (sqls == null || sqls.length <= 0) {
            throw new SQLException("generate '" + table + "' create sql failure");
        }
        return doExecuteUpdateBatch(sqls) >= 0;
    }

    /**
     * 数据数据表是否已经存在
     *
     * @param metadata 表格元信息
     */
    public boolean exist(Metadata metadata) throws Exception {
        if (metadata == null || StrUtil.isEmpty(metadata.getTable())) {
            throw new IllegalArgumentException("metadata");
        }

        String table = metadata.getTable();
        ResultSet rs = connection.getMetaData().getTables(null, null, table, null);
        return rs.next();
    }

    public boolean getAutoCommit() throws SQLException {
        return connection.getAutoCommit();
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        connection.setAutoCommit(autoCommit);
    }

    /**
     * 提交事务
     */
    public void commit() throws SQLException {
        connection.commit();
    }

    /**
     * 回滚事务
     */
    public void rollback() throws SQLException {
        connection.rollback();
    }

    /**
     * 关闭连接，实际上将连接回归数据库连接池，同时该Dao对象回收
     */
    public void close() throws SQLException {
        connection.close();
    }

    /**
     * 获取接口层内部是用何种方言实现
     */
    public String getDialect() {
        return builder.getDialect();
    }

    private Entity doExecuteSelect(String sql, String primary, Query query) throws SQLException {
        if (debuggable) {
            Logger.info("Execute Query Sql: %s", sql);
        }
        Entity entity = new Entity(primary);
        PreparedStatement statement = connection.prepareStatement(sql);
        if (query != null) {
            int index = buildStatement(statement, 1, query.joins());
            buildStatement(statement, index, query.where());
        }
        ResultSet result = statement.executeQuery();
        ResultSetMetaData metadata = result.getMetaData();
        int columns = metadata.getColumnCount();

        if (result.next()) {
            // 遍历数据库行数据字段并设置到实体类中
            for (int i = 1; i <= columns; i++) {
                Object value = result.getObject(i);
                // 数据库自身特殊对象类型需要进行转换以匹配框架类型
                if (value instanceof Timestamp) {
                    entity.setCalendar(metadata.getColumnLabel(i), Parser.parseCalendar((Timestamp) value));
                } else if (value instanceof LocalDateTime) {
                    Instant instant = ((LocalDateTime) value).atZone(ZoneId.systemDefault()).toInstant();
                    GregorianCalendar calendar = new GregorianCalendar();
                    calendar.setTimeInMillis(instant.toEpochMilli());
                    entity.setCalendar(metadata.getColumnLabel(i), calendar);
                } else {
                    entity.setObject(metadata.getColumnLabel(i), value);
                }
            }
            // 设置实体类主键便于之后做更新/删除操作
            if (!StrUtil.isEmpty(primary)) {
                entity.setIdentity(entity.getObject(primary));
            }
            return entity;
        }
        return null;
    }

    private Table<Entity> doExecuteQuery(String sql, String primary, Query query) throws SQLException {
        if (debuggable) {
            Logger.info("Execute Query Sql: %s", sql);
        }
        Table<Entity> entities = new Table<Entity>();
        PreparedStatement statement = connection.prepareStatement(sql);
        if (query != null) {
            int index = buildStatement(statement, 1, query.joins());
            buildStatement(statement, index, query.where());
        }
        ResultSet result = statement.executeQuery();
        ResultSetMetaData metadata = result.getMetaData();
        int columns = metadata.getColumnCount();

        while (result.next()) {
            Entity entity = new Entity(primary);
            for (int i = 1; i <= columns; i++) {
                Object value = result.getObject(i);
                // 数据库自身特殊对象类型需要进行转换以匹配框架类型
                if (value instanceof Timestamp) {
                    entity.setCalendar(metadata.getColumnLabel(i), Parser.parseCalendar((Timestamp) value));
                } else if (value instanceof LocalDateTime) {
                    Instant instant = ((LocalDateTime) value).atZone(ZoneId.systemDefault()).toInstant();
                    GregorianCalendar calendar = new GregorianCalendar();
                    calendar.setTimeInMillis(instant.toEpochMilli());
                    entity.setCalendar(metadata.getColumnLabel(i), calendar);
                } else {
                    entity.setObject(metadata.getColumnLabel(i), value);
                }
                // 设置实体类主键便于之后做更新/删除操作
                if (!StrUtil.isEmpty(primary)) {
                    entity.setIdentity(entity.getObject(primary));
                }
            }
            entities.add(entity);
        }
        return entities;
    }

    private int doExecuteUpdate(String sql, Updater updater) throws SQLException {
        if (debuggable) {
            Logger.info("Execute Update Sql: %s", sql);
        }
        PreparedStatement statement = connection.prepareStatement(sql);
        if (updater != null) {
            List<Updater.Data> dataList = updater.getDataList();
            int index = 1;
            for (int i = 0; i < dataList.size(); i++) {
                Updater.Data data = dataList.get(i);

                Object value = data.getValue();
                StatementBuilder stmtBuilder = doGetStatementBuilder(value.getClass());
                index = stmtBuilder.build(statement, index, value);
                index++;
            }
            buildStatement(statement, index, updater.where());
        }

        return statement.executeUpdate();
    }

    private int doExecuteUpdate(String sql, Entity entity) throws SQLException {
        if (debuggable) {
            Logger.info("Execute Update Sql: %s", sql);
        }
        PreparedStatement statement = connection.prepareStatement(sql);
        if (entity != null) {
            Param dataList = entity.getDatas();
            int index = 1;
            for (Entry<String, Object> entry : dataList.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                // Entity主键字段不更新
                if (key.equalsIgnoreCase(entity.getPrimary())) {
                    continue;
                }
                StatementBuilder stmtBuilder = doGetStatementBuilder(value.getClass());
                index = stmtBuilder.build(statement, index, value);
                index++;
            }
            Where where = new Where(entity.getPrimary(), Where.EQ, entity.getIdentity());
            buildStatement(statement, index, where);
        }

        return statement.executeUpdate();
    }

    private int doExecuteUpdate(String sql, String primary, Object identity) throws SQLException {
        if (debuggable) {
            Logger.info("Execute Update Sql: %s", sql);
        }
        PreparedStatement statement = connection.prepareStatement(sql);
        Where where = new Where(primary, Where.EQ, identity);
        buildStatement(statement, 1, where);

        return statement.executeUpdate();
    }

    private int doExecuteUpdate(String sql, Where where) throws SQLException {
        if (debuggable) {
            Logger.info("Execute Update Sql: %s", sql);
        }
        PreparedStatement statement = connection.prepareStatement(sql);
        buildStatement(statement, 1, where);

        return statement.executeUpdate();
    }

    private int doExecuteUpdateBatch(String sql, List<Entity> entities) throws SQLException {
        if (debuggable) {
            Logger.info("Execute Update Batch Sql: %s", sql);
        }
        PreparedStatement statement = connection.prepareStatement(sql);
        for (Entity entity : entities) {
            Param dataList = entity.getDatas();
            int index = 1;
            for (Entry<String, Object> entry : dataList.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                // Entity主键字段不更新
                if (key.equalsIgnoreCase(entity.getPrimary())) {
                    continue;
                }
                StatementBuilder stmtBuilder = doGetStatementBuilder(value.getClass());
                index = stmtBuilder.build(statement, index, value);
                index++;
            }
            Where where = new Where(entity.getPrimary(), Where.EQ, entity.getIdentity());
            buildStatement(statement, index, where);
            statement.addBatch();
        }

        try {
            int result = statement.executeBatch().length;
            statement.clearBatch();
            return result;
        } finally {
            statement.close();
        }
    }

    private int doExecuteUpdateBatch(String[] sqls) throws SQLException {
        if (debuggable) {
            for (int i = 0; i < sqls.length; i++) {
                String sql = sqls[i];
                Logger.info("Execute Update Batch Sql: %s", sql);
            }
        }
        Statement statement = connection.createStatement();
        for (int i = 0; i < sqls.length; i++) {
            String sql = sqls[i];
            statement.addBatch(sql);
        }

        try {
            int result = statement.executeBatch().length;
            statement.clearBatch();
            return result;
        } finally {
            statement.close();
        }
    }

    private int doExecuteInsert(String sql, Entity entity, Ref<Object> idRef) throws SQLException {
        if (debuggable) {
            Logger.info("Execute Insert Sql: %s", sql);
        }
        PreparedStatement statement = null;
        if (idRef != null) {
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        } else {
            statement = connection.prepareStatement(sql);
        }
        if (entity != null) {
            Param dataList = entity.getDatas();
            int index = 1;
            for (Object value : dataList.values()) {
                StatementBuilder stmtBuilder = doGetStatementBuilder(value.getClass());
                if (stmtBuilder == null) {
                    throw new SQLException("No matched statement builder for " + value.getClass());
                }
                index = stmtBuilder.build(statement, index, value);
                index++;
            }
        }

        int count = statement.executeUpdate();
        if (idRef != null) {
            ResultSet resultSet = statement.getGeneratedKeys();
            try {
                if (resultSet != null && resultSet.next()) {
                    idRef.value(resultSet.getObject(1));
                }
            } finally {
                if (resultSet != null) {
                    resultSet.close();
                }
            }
        }
        return count;
    }

    private int doExecuteInsertBatch(String sql, List<Entity> entities, List<Object> idList) throws SQLException {
        if (debuggable) {
            Logger.info("Execute Insert Batch Sql: %s", sql);
        }

        PreparedStatement statement = null;
        if (idList != null) {
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        } else {
            statement = connection.prepareStatement(sql);
        }
        for (Entity entity : entities) {
            Param dataList = entity.getDatas();
            int index = 1;
            for (Object value : dataList.values()) {
                StatementBuilder stmtBuilder = doGetStatementBuilder(value.getClass());
                index = stmtBuilder.build(statement, index, value);
                index++;
            }
            statement.addBatch();
        }

        int count = statement.executeBatch().length;
        if (idList != null) {
            ResultSet resultSet = statement.getGeneratedKeys();
            try {
                if (resultSet != null) {
                    while (resultSet.next()) {
                        idList.add(resultSet.getObject(1));
                    }
                }
            } finally {
                if (resultSet != null) {
                    resultSet.close();
                }
            }
        }
        return count;
    }

    private int doExecuteReplace(String sql, Entity entity, Ref<Object> idRef) throws SQLException {
        if (debuggable) {
            Logger.info("Execute Replace Sql: %s", sql);
        }
        PreparedStatement statement = null;
        if (idRef != null) {
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        } else {
            statement = connection.prepareStatement(sql);
        }
        if (entity != null) {
            Param dataList = entity.getDatas();
            int index = 1;
            for (Object value : dataList.values()) {
                StatementBuilder stmtBuilder = doGetStatementBuilder(value.getClass());
                if (stmtBuilder == null) {
                    throw new SQLException("No matched statement builder for " + value.getClass());
                }
                index = stmtBuilder.build(statement, index, value);
                index++;
            }
        }

        int count = statement.executeUpdate();
        if (idRef != null) {
            ResultSet resultSet = statement.getGeneratedKeys();
            try {
                if (resultSet != null && resultSet.next()) {
                    idRef.value(resultSet.getObject(1));
                }
            } finally {
                if (resultSet != null) {
                    resultSet.close();
                }
            }
        }
        return count;
    }

    private static int buildStatement(PreparedStatement statement, int index, Where where) throws SQLException {
        if (where == null) {
            return index;
        }
        List<Condition> conditionList = where.getConditionList();
        for (Condition condition : conditionList) {
            if (condition.getWhere() != null) {
                // 查询条件嵌套，例如WHERER ID = 1 AND (CLASS = 1 OR CLASS = 2)
                index = buildStatement(statement, index, condition.getWhere());
            } else {
                String operation = condition.getOperation();
                Object value = condition.getValue();
                ConditionBuilder valueBuilder = conditionBuilders.get(operation);
                if (valueBuilder != null) {
                    value = valueBuilder.valueBuild(value);
                }
                StatementBuilder stmtBuilder = doGetStatementBuilder(value.getClass());
                index = stmtBuilder.build(statement, index, value);
                index++;
            }
        }
        return index;
    }

    private static int buildStatement(PreparedStatement statement, int index, List<Join> joinList) throws SQLException {
        if (joinList == null || joinList.isEmpty()) {
            return index;
        }
        for (Join join : joinList) {
            List<Join.On> conditionList = join.getConditionList();
            if (conditionList.isEmpty()) {
                continue;
            }
            for (Join.On condition : conditionList) {
                String operation = condition.getOperation();
                Object value = condition.getValue();
                if (value instanceof ColumnLabel) {
                    continue;
                }
                ConditionBuilder valueBuilder = conditionBuilders.get(operation);
                if (valueBuilder != null) {
                    value = valueBuilder.valueBuild(value);
                }
                // Join操作只是表字段判断，则不用进行Statement#set
                StatementBuilder stmtBuilder = doGetStatementBuilder(value.getClass());
                index = stmtBuilder.build(statement, index, value);
                index++;
            }
        }
        return index;
    }

    private static StatementBuilder doGetStatementBuilder(Class<?> clazz) {
        if (clazz.isArray()) {
            Class<?> elementType = clazz.getComponentType();
            if (elementType.isAssignableFrom(byte.class)) {
                return statementBuilders.get(byte.class);
            }
            return statementBuilders.get(List.class);
        } else if (ByteBuffer.class.isAssignableFrom(clazz)) {
            return statementBuilders.get(ByteBuffer.class);
        } else if (List.class.isAssignableFrom(clazz)) {
            return statementBuilders.get(List.class);
        }
        return statementBuilders.get(clazz);
    }
}
