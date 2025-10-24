package cloud.apposs.cachex.database;

import cloud.apposs.cachex.jdbc.ColumnLabel;

/**
 * SQL ORDER BY排序
 */
public class OrderBy {
    /**
     * 查询字段，默认为所有
     */
    private String field;

    /**
     * 升、降排序
     */
    private boolean desc = true;

    public OrderBy(String field) {
        this(field, true);
    }

    public OrderBy(ColumnLabel field) {
        this(field, true);
    }

    public OrderBy(String field, boolean desc) {
        this.field = field;
        this.desc = desc;
    }

    public OrderBy(ColumnLabel field, boolean desc) {
        this.field = field.toString();
        this.desc = desc;
    }

    public String getField() {
        return field;
    }

    public boolean isDesc() {
        return desc;
    }
}
