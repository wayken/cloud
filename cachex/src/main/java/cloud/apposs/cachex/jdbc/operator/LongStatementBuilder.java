package cloud.apposs.cachex.jdbc.operator;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LongStatementBuilder implements StatementBuilder {
    @Override
    public int build(PreparedStatement statement,
                     int index, Object value) throws SQLException {
        statement.setLong(index, (Long) value);
        return index;
    }
}
