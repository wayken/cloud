package cloud.apposs.cachex.database;

import cloud.apposs.cachex.jdbc.ColumnLabel;

import java.util.LinkedList;
import java.util.List;

/**
 * SQL WHERE条件查询
 */
public class Where {
    public static final String EQ = "=";
    public static final String NE = "<>";
    public static final String LT = "<";
    public static final String LE = "<=";
    public static final String GT = ">";
    public static final String GE = ">=";
    public static final String IS = "is";
    public static final String ISNOT = "is not";
    /**
     * 模糊匹配，例如"a bc"，a、b、bc可匹配(等价于sql的like %keyword%)
     */
    public static final String LK = "like";
    /**
     * 模糊匹配，例如"a bc"，a、b、bc可匹配(等价于sql的 not like %keyword%)
     */
    public static final String NL = "not like";
    public static final String IN = "in";

    /**
     * 条件查询列表
     */
    protected final List<Condition> conditionList = new LinkedList<Condition>();

    public Where() {
    }

    public static Where builder(String key, String operation, Object value) {
        return new Where(key, operation, value);
    }

    public static Where builder(ColumnLabel key, String operation, Object value) {
        return new Where(key, operation, value);
    }

    /**
     * Where与查询
     *
     * @param key       查询字段
     * @param operation 查询操作，可以为=、>=、<=等操作
     * @param value     查询的值
     */
    public Where(String key, String operation, Object value) {
        conditionList.add(new Condition(key, operation, value));
    }

    public Where(ColumnLabel key, String operation, Object value) {
        conditionList.add(new Condition(key.toString(), operation, value));
    }

    public Where add(Condition condition) {
        conditionList.add(condition);
        return this;
    }

    /**
     * Where与查询
     *
     * @param key       查询字段
     * @param operation 查询操作，可以为=、>=、<=等操作
     * @param value     查询的值
     */
    public Where and(String key, String operation, Object value) {
        conditionList.add(new Condition(true, key, operation, value));
        return this;
    }

    public Where and(ColumnLabel key, String operation, Object value) {
        conditionList.add(new Condition(true, key.toString(), operation, value));
        return this;
    }

    /**
     * Where与嵌套查询
     */
    public Where and(Where where) {
        conditionList.add(new Condition(true, where));
        return this;
    }

    /**
     * Where或查询
     *
     * @param key       查询字段
     * @param operation 查询操作，可以为=、>=、<=等操作
     * @param value     查询的值
     */
    public Where or(String key, String operation, Object value) {
        conditionList.add(new Condition(false, key, operation, value));
        return this;
    }

    public Where or(ColumnLabel key, String operation, Object value) {
        conditionList.add(new Condition(false, key.toString(), operation, value));
        return this;
    }

    /**
     * Where或嵌套查询
     */
    public Where or(Where where) {
        conditionList.add(new Condition(false, where));
        return this;
    }

    public List<Condition> getConditionList() {
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
    public static class Condition {
        /**
         * SQL AND，FALSE则为SQL OR
         */
        private final boolean and;

        private final String key;

        private final String operation;

        private final Object value;

        private final Where where;

        public Condition(String key, String operation, Object value) {
            this(true, key, operation, value, null);
        }

        public Condition(boolean and, Where where) {
            this(and, null, null, null, where);
        }

        public Condition(boolean and, String key, String operation, Object value) {
            this(and, key, operation, value, null);
        }

        public Condition(boolean and, String key, String operation, Object value, Where where) {
            this.and = and;
            this.key = key;
            this.operation = operation;
            this.value = value;
            this.where = where;
        }

        public boolean isAnd() {
            return and;
        }

        public String getKey() {
            return key;
        }

        public String getOperation() {
            return operation;
        }

        public Object getValue() {
            return value;
        }

        public Where getWhere() {
            return where;
        }
    }
}
