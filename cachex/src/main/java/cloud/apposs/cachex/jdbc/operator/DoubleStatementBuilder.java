package cloud.apposs.cachex.jdbc.operator;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DoubleStatementBuilder implements StatementBuilder {
    @Override
    public int build(PreparedStatement statement,
                     int index, Object value) throws SQLException {
        statement.setDouble(index, (Double) value);
        return index;
    }
}
