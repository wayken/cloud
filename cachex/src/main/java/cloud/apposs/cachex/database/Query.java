package cloud.apposs.cachex.database;

import cloud.apposs.cachex.jdbc.*;

import java.util.LinkedList;
import java.util.List;

/**
 * 数据库查询语句封装，支持数据库的SELECT/UPDATE等操作
 */
public class Query {
    public static final String DEFAULT_FIELDS = "*";

    public static final String COUNT_FIELDS = "COUNT(*) AS count";

    public static final String COUNT_FIELD = "count";

    public static final String COUNT_ID_FIELDS = "COUNT(ID) AS count";

    /**
     * 查询字段，默认为所有
     */
    protected String field = DEFAULT_FIELDS;

    /**
     * 是否消除重复元素
     */
    protected boolean distinct = false;

    /**
     * 条件查询
     */
    protected Where where = new Where();

    /**
     * 分组查询
     */
    private GroupBy groupBy;

    /**
     * 排序
     */
    private OrderBy orderBy;

    /**
     * 表连接查询，支持多个Join多表查询
     */
    protected final List<Join> joins = new LinkedList<Join>();

    /**
     * 分页查询
     */
    protected Pager pager;

    public Query() {
        this(DEFAULT_FIELDS);
    }

    public Query(String field) {
        this.field = field;
    }

    public Query(ColumnLabel... fields) {
        this.field = "";
        for (int i = 0; i < fields.length; i++) {
            ColumnLabel field = fields[i];
            if (i > 0) {
                this.field += ", ";
            }
            this.field += field.toString();
        }
    }

    public Query(String... fields) {
        this.field = "";
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            if (i > 0) {
                this.field += ", ";
            }
            this.field += field;
        }
    }

    public static Query builder() {
        return builder(DEFAULT_FIELDS);
    }

    public static Query builder(String field) {
        return new Query(field);
    }

    public static Query builder(String... fields) {
        return new Query(fields);
    }

    public static Query builder(ColumnLabel... fields) {
        return new Query(fields);
    }

    public static Query builder(Where where) {
        Query query = new Query();
        query.where(where);
        return query;
    }

    public static Query builder(String field, Where where) {
        Query query = new Query(field);
        query.where(where);
        return query;
    }

    public String field() {
        return field;
    }

    public void field(String field) {
        this.field = field;
    }

    public boolean distinct() {
        return distinct;
    }

    public Query distinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    /**
     * 不加条件的SELECT单条数据查询
     */
    public Query select() {
        return select("*");
    }

    /**
     * 不加条件的SELECT单条数据查询
     *
     * @param field 要查询的字段，示例："id, name, age"
     */
    public Query select(String field) {
        this.field = field;
        return this;
    }

    public Where where() {
        return where;
    }

    public Where where(Where where) {
        for (Where.Condition condition : where.getConditionList()) {
            this.where.add(condition);
        }
        return this.where;
    }

    /**
     * Where与查询
     *
     * @param key       查询字段
     * @param operation 查询操作，可以为=、>=、<=等操作
     * @param value     查询的值
     */
    public Where where(String key, String operation, Object value) {
        return where.and(key, operation, value);
    }

    public Where where(ColumnLabel field, String operation, Object value) {
        String key = field.getTable() + "." + field.getField();
        return where.and(key, operation, value);
    }

    public List<Join> joins() {
        return joins;
    }

    /**
     * 建立表连接查询，
     * 注意：表连接查询建议不超过两个表，且连接查询条件必须主键查询，其他情况一律采用编码做数据整合
     */
    public Join join(String table) {
        return join(new Join(table));
    }

    /**
     * 建立表连接查询，
     * 注意：表连接查询建议不超过两个表，且连接查询条件必须主键查询，其他情况一律采用编码做数据整合
     */
    public Join leftJoin(String table) {
        return join(new Join(table, Join.Type.LEFT));
    }

    /**
     * 建立表连接查询，
     * 注意：表连接查询建议不超过两个表，且连接查询条件必须主键查询，其他情况一律采用编码做数据整合
     */
    public Join join(Join join) {
        joins.add(join);
        return join;
    }

    public GroupBy groupBy() {
        return groupBy;
    }

    public Query groupBy(String field) {
        this.groupBy = new GroupBy(field);
        return this;
    }

    public Query groupBy(ColumnLabel... fields) {
        this.groupBy = new GroupBy(fields);
        return this;
    }

    public Query groupBy(GroupBy groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    public OrderBy orderBy() {
        return orderBy;
    }

    public Query orderBy(String field) {
        this.orderBy = new OrderBy(field);
        return this;
    }

    public Query orderBy(String field, boolean desc) {
        this.orderBy = new OrderBy(field, desc);
        return this;
    }

    public Query orderBy(ColumnLabel field) {
        this.orderBy = new OrderBy(field);
        return this;
    }

    public Query orderBy(ColumnLabel field, boolean desc) {
        this.orderBy = new OrderBy(field, desc);
        return this;
    }

    public Pager pager() {
        return pager;
    }

    public void pager(Pager pager) {
        this.pager = pager;
    }

    /**
     * 分页查询
     *
     * @param start 分页开始位置
     * @param limit 分页条数
     */
    public Query limit(int start, int limit) {
        if (pager == null) {
            pager = new Pager(start, limit);
        } else {
            pager.setStart(start);
            pager.setLimit(limit);
        }
        return this;
    }
}
