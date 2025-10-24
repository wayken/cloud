package cloud.apposs.cachex.database;

import cloud.apposs.cachex.jdbc.ColumnLabel;

/**
 * SQL GROUP BY分组查询
 */
public class GroupBy {
    /**
     * 查询字段，默认为所有
     */
    private String field;

    public GroupBy(String field) {
        this.field = field;
    }

    public GroupBy(ColumnLabel... fields) {
        this.field = "";
        for (int i = 0; i < fields.length; i++) {
            ColumnLabel field = fields[i];
            if (i > 0) {
                this.field += ", ";
            }
            this.field += field.toString();
        }
    }

    public String getField() {
        return field;
    }
}
