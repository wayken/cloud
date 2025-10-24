package cloud.apposs.cachex.jdbc;

import cloud.apposs.cachex.database.*;

/**
 * 数据查询接口封装，不同数据库语言其封装的SQL查询不一样，全局单例
 */
public interface SqlBuilder {
    /**
     * JDBC驱动配置
     */
    public static final String DRIVER_MYSQL = "com.mysql.cj.jdbc.Driver";
    public static final String DRIVER_SQLITE = "org.sqlite.JDBC";

    /**
     * 数据库方言
     */
    String getDialect();

    /**
     * 生成数据查询语句
     *
     * @param table    查询表格
     * @param primary  查询主键字段名称，可为空
     * @param identity 查询主键值，需要和primary结合使用，可为空
     * @param query    查询条件，可为空
     * @return SQL查询语句
     */
    String generateQuerySql(String table, String primary, Object identity, Query query);

    /**
     * 生成数据更新语句
     *
     * @param table  更新表格
     * @param entity 实体对象
     */
    String generateUpdateSql(String table, Entity entity);

    /**
     * 生成数据更新语句
     *
     * @param table   更新表格
     * @param updater 更新条件
     */
    String generateUpdateSql(String table, Updater updater);

    /**
     * 生成数据库删除语句
     *
     * @param table    查询表格
     * @param primary  查询主键字段名称，可为空
     * @param identity 查询主键值，需要和primary结合使用，可为空
     */
    String generateDeleteSql(String table, String primary, Object identity);

    /**
     * 生成数据库删除语句
     *
     * @param table  查询表格
     * @param entity 实体对象
     */
    String generateDeleteSql(String table, Entity entity);

    /**
     * 生成数据库删除语句
     *
     * @param table 查询表格
     * @param where 查询条件
     */
    String generateDeleteSql(String table, Where where);

    /**
     * 生成数据库插入语句
     *
     * @param table  查询表格
     * @param entity 实体对象
     */
    String generateInertSql(String table, Entity entity);

    /**
     * 生成数据库替换语句
     *
     * @param table  查询表格
     * @param entity 实体对象
     */
    String generateReplaceSql(String table, Entity entity);

    /**
     * 生成数据库表删除语句
     *
     * @param table 表名
     */
    String generateDropSql(String table);

    /**
     * 生成数据库表创建语句
     *
     * @param table    表名
     * @param metadata 表格元信息
     */
    String[] generateCreateSql(String table, Metadata metadata);
}
