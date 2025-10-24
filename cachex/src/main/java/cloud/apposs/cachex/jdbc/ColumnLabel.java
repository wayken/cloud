package cloud.apposs.cachex.jdbc;

import cloud.apposs.util.StrUtil;

/**
 * 数据库表字段封装，一般用于Query/Whereu/Join中使用，
 * 同时如果在On条件查询中需要进行如user.pid = product.id这种操作，则需要将该字段封装为Label对象方便SqlBuilder解析
 */
public class ColumnLabel {
    /**
     * 字段对应表名，在JOIN查询中使用
     */
    private final String table;

    /**
     * 字段名，Query/Whereu/Join中常用
     */
    private final String field;

    /**
     * 查询字段别名，示例：SELECT name AS aname
     */
    private final String alias;

    public ColumnLabel(String table, String field, String alias) {
        this.table = table;
        this.field = field;
        this.alias = alias;
    }

    public String getTable() {
        return table;
    }

    public String getField() {
        return field;
    }

    public static ColumnLabel create(String field) {
        return new ColumnLabel(null, field, null);
    }

    public static ColumnLabel create(String table, String field) {
        return new ColumnLabel(table, field, null);
    }

    public static ColumnLabel create(String table, String field, String alias) {
        return new ColumnLabel(table, field, alias);
    }

    @Override
    public String toString() {
        StringBuilder info = new StringBuilder(12);
        if (!StrUtil.isEmpty(table)) {
            info.append(table).append(".");
        }
        info.append(field);
        if (!StrUtil.isEmpty(alias)) {
            info.append(" AS ").append(alias);
        }
        return info.toString();
    }
}
