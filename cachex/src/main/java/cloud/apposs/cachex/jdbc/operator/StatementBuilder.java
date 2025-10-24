package cloud.apposs.cachex.jdbc.operator;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * PreparedStatement属性值注入，不同的属性值类型不同其调用PreparedStatement设置的方法也不一样，
 * 例如Integer是调用PreparedStatement.setInt();
 */
public interface StatementBuilder {
    int build(PreparedStatement statement,
              int index, Object value) throws SQLException;
}
