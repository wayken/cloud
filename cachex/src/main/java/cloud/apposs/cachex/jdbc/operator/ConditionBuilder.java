package cloud.apposs.cachex.jdbc.operator;

/**
 * 查询条件自定义解析
 */
public interface ConditionBuilder {
    /**
     * 解析PreparedStatement.setXXX(value)对应的值，
     * 一般直接返回即可，但像LIKE等操作符需要返回%VALUE%以便于设置值
     */
    Object valueBuild(Object value);

    /**
     * 解析查询条件拼成SQL的字符串，
     * 例如LIKE操作符，生成的SQL语句为XXX LIKE ?
     */
    String operationBuild(String key, Object value);
}
