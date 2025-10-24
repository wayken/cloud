package cloud.apposs.cachex.jdbc.builder;

import cloud.apposs.cachex.CacheXConstants;
import cloud.apposs.cachex.jdbc.SqlBuilder;
import cloud.apposs.util.StrUtil;

public class SqlBuilderFactory {
    /**
     * 判断是否为JDBC接口
     */
    public static boolean isJdbcDialect(String dialect) {
        if (StrUtil.isEmpty(dialect)) {
            return false;
        }
        dialect = dialect.toUpperCase();
        return dialect.equalsIgnoreCase(CacheXConstants.DIALECT_MYSQL) ||
                dialect.equalsIgnoreCase(CacheXConstants.DIALECT_ORACLE) ||
                dialect.equalsIgnoreCase(CacheXConstants.DIALECT_SQLITE);
    }

    /**
     * 创建SQL编译器，注意该方法内部可能创建了连接池，调用时必须只调用一次，单例模式
     *
     * @param dialect SQL方言
     */
    public static SqlBuilder getSqlBuilder(String dialect) {
        if (StrUtil.isEmpty(dialect)) {
            throw new IllegalArgumentException("dialect");
        }

        if (dialect.equalsIgnoreCase(CacheXConstants.DIALECT_MYSQL)) {
            return new MysqlBuilder();
        } else if (dialect.equalsIgnoreCase(CacheXConstants.DIALECT_SQLITE)) {
            return new SqliteBuilder();
        }
        return null;
    }
}
