package cloud.apposs.cachex.jdbc.operator;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ShortStatementBuilder implements StatementBuilder {
    @Override
    public int build(PreparedStatement statement,
                     int index, Object value) throws SQLException {
        statement.setShort(index, (Short) value);
        return index;
    }
}
