package cloud.apposs.cachex.database;

import cloud.apposs.cachex.jdbc.ColumnLabel;

import java.util.LinkedList;
import java.util.List;

/**
 * SQL JOIN条件查询
 */
public class Join {
    /**
     * 表连接类型，默认为INNER类型，
     * 参考：https://www.cnblogs.com/fudashi/p/7491039.html
     */
    public enum Type {
        INNER,
        CROSS,
        LEFT,
        RIGHT,
        OUTER
    }

    /**
     * 多表连接表名
     */
    private final String table;

    /**
     * 表连接类型
     */
    private final Type type;

    /**
     * 条件查询列表，支持多条件查询
     */
    protected final List<On> conditionList = new LinkedList<On>();

    public Join(String table) {
        this(table, Type.INNER);
    }

    public Join(String table, Type type) {
        this.table = table;
        this.type = type;
    }

    public String getTable() {
        return table;
    }

    public Type getType() {
        return type;
    }

    /**
     * 表连接查询条件，查询的字段KEY建议是要走主键索引
     *
     * @param key 查询的字段，建议为主键索引
     * @param operation 条件判断
     * @param value 查询值
     */
    public Join on(ColumnLabel key, String operation, Object value) {
        return and(key, operation, value);
    }

    public Join and(ColumnLabel key, String operation, Object value) {
        conditionList.add(new On(true, key, operation, value));
        return this;
    }

    public Join or(ColumnLabel key, String operation, Object value) {
        conditionList.add(new On(false, key, operation, value));
        return this;
    }

    public List<On> getConditionList() {
        return conditionList;
    }

    /**
     * 判断查询条件是否为空
     */
    public boolean isEmpty() {
        return conditionList.isEmpty();
    }

    /***
     * 查询字段条件封装
     */
    public static class On {
        /**
         * SQL AND，FALSE则为SQL OR
         */
        private final boolean and;

        private final ColumnLabel key;

        private final String operation;

        private final Object value;

        public On(ColumnLabel key, String operation, Object value) {
            this(true, key, operation, value);
        }

        public On(boolean and, ColumnLabel key, String operation, Object value) {
            this.and = and;
            this.key = key;
            this.operation = operation;
            this.value = value;
        }

        public boolean isAnd() {
            return and;
        }

        public ColumnLabel getKey() {
            return key;
        }

        public String getOperation() {
            return operation;
        }

        public Object getValue() {
            return value;
        }
    }
}
