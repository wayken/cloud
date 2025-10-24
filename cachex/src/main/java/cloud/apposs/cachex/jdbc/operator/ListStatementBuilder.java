package cloud.apposs.cachex.jdbc.operator;

import cloud.apposs.cachex.jdbc.Dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ListStatementBuilder implements StatementBuilder {
    @Override
    public int build(PreparedStatement statement,
                     int index, Object value) throws SQLException {
        Class<?> type = value.getClass();
        Map<Class<?>, StatementBuilder> statementBuilders = Dao.getStatementBuilders();
        if (List.class.isAssignableFrom(type)) {
            @SuppressWarnings("unchecked")
            List<Object> inList = (List<Object>) value;
            for (int i = 0; i < inList.size(); i++) {
                Object inValue = inList.get(i);

                StatementBuilder stmtBuilder = statementBuilders.get(inValue.getClass());
                if (stmtBuilder == null) {
                    throw new IllegalArgumentException("Unexpected Statement Builder Type '" + inValue.getClass().getName() + "'");
                }
                index = stmtBuilder.build(statement, index, inValue);
                if (i != inList.size() - 1) {
                    index++;
                }
            }
        } else if (type.isArray()) {
            Object[] inList = (Object[]) value;
            for (int i = 0; i < inList.length; i++) {
                Object inValue = inList[i];
                StatementBuilder stmtBuilder = statementBuilders.get(inValue.getClass());
                if (stmtBuilder == null) {
                    throw new IllegalArgumentException("Unexpected Statement Builder Type '" + inValue.getClass().getName() + "'");
                }
                index = stmtBuilder.build(statement, index, inValue);
                if (i != inList.length - 1) {
                    index++;
                }
            }
        }
        return index;
    }
}
