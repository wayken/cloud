package cloud.apposs.cachex.jdbc.operator;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BytesStatementBuilder implements StatementBuilder {
    @Override
    public int build(PreparedStatement statement,
                     int index, Object value) throws SQLException {
        byte[] bytes = (byte[]) value;
        statement.setBytes(index, bytes);
        return index;
    }
}
