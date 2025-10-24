package cloud.apposs.cachex.jdbc.builder;

import cloud.apposs.cachex.CacheXConstants;
import cloud.apposs.cachex.database.*;
import cloud.apposs.cachex.jdbc.*;
import cloud.apposs.cachex.database.Metadata.Column;
import cloud.apposs.cachex.database.Where.Condition;
import cloud.apposs.cachex.jdbc.operator.ConditionBuilder;
import cloud.apposs.util.Param;
import cloud.apposs.util.StrUtil;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * JDBC MYSQL实现
 */
public class MysqlBuilder implements SqlBuilder {
    public static final String CRLF = "\r\n";

    @Override
    public String getDialect() {
        return CacheXConstants.DIALECT_MYSQL;
    }

    @Override
    public String generateQuerySql(String table, String primary, Object identity, Query query) {
        String field = Query.DEFAULT_FIELDS;
        if (query != null) {
            field = query.field();
        }
        StringBuilder sql = new StringBuilder(64);
        if (query != null && query.distinct()) {
            sql.append("SELECT DISTINCT ").append(field);
        } else {
            sql.append("SELECT ").append(field);
        }
        sql.append(" FROM ").append(table);
        // 表连接查询
        if (query != null && query.joins() != null && !query.joins().isEmpty()) {
            sql.append(doGenerateJoinSql(query.joins()));
        }
        // 主键查询和条件查询互斥，因为主键查询就表示已经唯一值
        if (!StrUtil.isEmpty(primary) && identity != null) {
            // 主键查询
            sql.append(" WHERE ");
            sql.append("(").append(primary).append("=").append(identity).append(")");
        } else if (query != null && query.where() != null && !query.where().isEmpty()) {
            // 条件查询
            sql.append(" WHERE ");
            Where where = query.where();
            sql.append(doGenerateWhereSql(where));
        }
        // 分组查询
        if (query != null && query.groupBy() != null) {
            sql.append(" GROUP BY " + query.groupBy().getField());
        }
        // 排序
        if (query != null && query.orderBy() != null) {
            sql.append(" ORDER BY " + query.orderBy().getField());
            if (query.orderBy().isDesc()) {
                sql.append(" DESC");
            } else {
                sql.append(" ASC");
            }
        }
        // 分页查询
        if (query != null && query.pager() != null) {
            int start = query.pager().getStart();
            int limit = query.pager().getLimit();
            sql.append(" LIMIT ").append(start).append(", ").append(limit);
        }
        return sql.toString();
    }

    @Override
    public String generateUpdateSql(String table, Entity entity) {
        StringBuilder sql = new StringBuilder(64);
        sql.append("UPDATE ").append(table);
        // 字段更新
        sql.append(" SET ");
        Param fieldDatas = entity.getDatas();
        for (Entry<String, Object> entry : fieldDatas.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            // 主键字段不更新
            if (entry.getKey().equalsIgnoreCase(entity.getPrimary())) {
                continue;
            }
            sql.append(entry.getKey() + "=?, ");
        }
        sql.setLength(sql.length() - 2);
        // 条件范围缩小
        sql.append(" WHERE " + entity.getPrimary() + "=?");
        return sql.toString();
    }

    @Override
    public String generateUpdateSql(String table, Updater updater) {
        StringBuilder sql = new StringBuilder(64);
        sql.append("UPDATE ").append(table);
        // 字段更新
        sql.append(" SET ");
        List<Updater.Data> fieldDatas = updater.getDataList();
        int dataSize = fieldDatas.size();
        for (int i = 0; i < dataSize; i++) {
            Updater.Data data = fieldDatas.get(i);
            String dataKey = data.getKey();
            String dataOp = data.getOperation();
            if (!StrUtil.isEmpty(dataOp)) {
                sql.append(dataKey + "=" + dataKey + dataOp + "?, ");
            } else {
                sql.append(dataKey + "=" + "?, ");
            }
        }
        sql.setLength(sql.length() - 2);
        // 条件范围缩小
        Where where = updater.where();
        sql.append(" WHERE " + doGenerateWhereSql(where));
        return sql.toString();
    }

    @Override
    public String generateInertSql(String table, Entity entity) {
        StringBuilder sql = new StringBuilder(64);
        sql.append("INSERT INTO ").append(table);
        // 字段更新
        int index = 0;
        Param fieldDatas = entity.getDatas();
        String fieldKeys = "";
        String fieldValues = "";
        int fieldTotal = fieldDatas.size();
        for (String key : fieldDatas.keySet()) {
            fieldKeys += key;
            fieldValues += "?";
            if (index < fieldTotal - 1) {
                fieldKeys += ", ";
                fieldValues += ", ";
            }
            index++;
        }
        sql.append(" (").append(fieldKeys).append(")");
        sql.append(" VALUES(").append(fieldValues).append(")");
        return sql.toString();
    }

    @Override
    public String generateReplaceSql(String table, Entity entity) {
        StringBuilder sql = new StringBuilder(64);
        sql.append("REPLACE INTO ").append(table);
        // 字段更新
        int index = 0;
        Param fieldDatas = entity.getDatas();
        String fieldKeys = "";
        String fieldValues = "";
        int fieldTotal = fieldDatas.size();
        for (String key : fieldDatas.keySet()) {
            fieldKeys += key;
            fieldValues += "?";
            if (index < fieldTotal - 1) {
                fieldKeys += ", ";
                fieldValues += ", ";
            }
            index++;
        }
        sql.append(" (").append(fieldKeys).append(")");
        sql.append(" VALUES(").append(fieldValues).append(")");
        return sql.toString();
    }

    @Override
    public String generateDeleteSql(String table, String primary, Object identity) {
        StringBuilder sql = new StringBuilder(64);
        sql.append("DELETE FROM ").append(table);
        // 条件范围缩小
        sql.append(" WHERE " + primary + "=?");
        return sql.toString();
    }

    @Override
    public String generateDeleteSql(String table, Entity entity) {
        StringBuilder sql = new StringBuilder(64);
        sql.append("DELETE FROM ").append(table);
        // 条件范围缩小
        sql.append(" WHERE " + entity.getPrimary() + "=?");
        return sql.toString();
    }

    @Override
    public String generateDeleteSql(String table, Where where) {
        StringBuilder sql = new StringBuilder(64);
        sql.append("DELETE FROM ").append(table);
        // WHERE条件匹配
        sql.append(" WHERE " + doGenerateWhereSql(where));
        return sql.toString();
    }

    @Override
    public String generateDropSql(String table) {
        return "DROP TABLE IF EXISTS " + table;
    }

    @Override
    public String[] generateCreateSql(String table, Metadata metadata) {
        StringBuilder sql = new StringBuilder(128);
        sql.append("CREATE TABLE " + table + "(").append(CRLF);
        List<Column> columnList = metadata.getColumnList();
        // 生成表字段
        String primaryColumn = null;
        for (int i = 0; i < columnList.size(); i++) {
            Column column = columnList.get(i);
            String columnName = column.getName();
            int columnType = column.getType();
            int columnLength = column.getLength();
            boolean columnPrimary = column.isPrimary();
            boolean columnNotNull = column.isNotNull();
            String columnDefaultVal = column.getDefaultVal();
            sql.append("    " + columnName + " ");
            if (columnType == Metadata.COLUMN_TYPE_INT) {
                sql.append("INT");
            } else if (columnType == Metadata.COLUMN_TYPE_STRING) {
                sql.append("VARCHAR");
            } else if (columnType == Metadata.COLUMN_TYPE_BINARY) {
                sql.append("BINARY");
            } else if (columnType == Metadata.COLUMN_TYPE_DATE) {
                sql.append("DATETIME");
            }
            if (columnLength > 0) {
                sql.append("(").append(columnLength).append(")");
            }
            if (columnNotNull) {
                sql.append(" NOT NULL");
            } else {
                sql.append(" DEFAULT NULL");
            }
            if (!StrUtil.isEmpty(columnDefaultVal)) {
                sql.append(" DEFAULT '").append(columnDefaultVal).append("'");
            }
            if (columnPrimary) {
                sql.append(" AUTO_INCREMENT");
                primaryColumn = columnName;
            }
            sql.append(",").append(CRLF);
        }
        // 生成索引
        List<Metadata.Index> indexList = metadata.getIndexList();
        if (indexList == null || indexList.isEmpty()) {
            if (!StrUtil.isEmpty(primaryColumn)) {
                sql.append("    PRIMARY KEY (" + primaryColumn + "),").append(CRLF);
            }
        } else {
            for (Metadata.Index index : indexList) {
                boolean isPrimaryIndex = index.isPrimary();
                String indexKey = index.getIndex();
                String indexName = index.getName();
                if (isPrimaryIndex) {
                    sql.append("    PRIMARY KEY (").append(indexKey).append("),").append(CRLF);
                } else {
                    sql.append("    KEY ").append(indexName).append(" (").append(indexKey).append("),").append(CRLF);
                }
            }
        }
        String newSql = sql.substring(0, sql.length() - (CRLF.length() + 1));
        sql.setLength(0);
        sql.append(newSql);
        sql.append(CRLF).append(")");
        if (!StrUtil.isEmpty(metadata.getCharset())) {
            sql.append(" CHARSET=").append(metadata.getCharset());
        }

        return new String[]{sql.toString()};
    }

    private String doGenerateJoinSql(List<Join> joins) {
        StringBuilder sql = new StringBuilder(32);
        for (Join join : joins) {
            List<Join.On> conditionList = join.getConditionList();
            if (conditionList.isEmpty()) {
                continue;
            }
            sql.append(" ").append(join.getType() + " JOIN " + join.getTable() + " ON (");
            int index = 0;
            for (Join.On condition : conditionList) {
                if (index != 0) {
                    if (condition.isAnd()) {
                        sql.append(" AND ");
                    } else {
                        sql.append(" OR ");
                    }
                }
                ColumnLabel key = condition.getKey();
                String operation = condition.getOperation();
                Object value = condition.getValue();
                Map<String, ConditionBuilder> conditionBuilders = Dao.getConditionBuilders();
                ConditionBuilder conditionBuilder = conditionBuilders.get(operation);
                if (conditionBuilder != null) {
                    sql.append(conditionBuilder.operationBuild(key.toString(), value));
                } else if (value instanceof ColumnLabel) {
                    sql.append(key + " " + operation + " " + value.toString());
                } else {
                    sql.append(key + " " + operation + " ?");
                }

                index++;
            }
            sql.append(")");
        }
        return sql.toString();
    }

    private String doGenerateWhereSql(Where where) {
        StringBuilder sql = new StringBuilder(32);
        List<Condition> conditionList = where.getConditionList();
        int index = 0;
        for (Condition condition : conditionList) {
            if (index != 0) {
                if (condition.isAnd()) {
                    sql.append(" AND ");
                } else {
                    sql.append(" OR ");
                }
            }
            Where whereNext = condition.getWhere();
            if (whereNext != null && !whereNext.isEmpty()) {
                sql.append("(").append(doGenerateWhereSql(whereNext)).append(")");
            } else {
                String key = condition.getKey();
                String operation = condition.getOperation();
                Object value = condition.getValue();
                Map<String, ConditionBuilder> conditionBuilders = Dao.getConditionBuilders();
                ConditionBuilder conditionBuilder = conditionBuilders.get(operation);
                if (conditionBuilder != null) {
                    sql.append(conditionBuilder.operationBuild(key, value));
                } else {
                    sql.append("(" + key + " " + operation + " ?)");
                }
            }
            index++;
        }
        return sql.toString();
    }
}
