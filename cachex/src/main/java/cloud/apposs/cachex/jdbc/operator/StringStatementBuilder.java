package cloud.apposs.cachex.jdbc.operator;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StringStatementBuilder implements StatementBuilder {
    @Override
    public int build(PreparedStatement statement,
                     int index, Object value) throws SQLException {
        statement.setString(index, (String) value);
        return index;
    }
}
