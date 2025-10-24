package cloud.apposs.cachex.jdbc.operator;

import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ByteBufferStatementBuilder implements StatementBuilder {
    @Override
    public int build(PreparedStatement statement,
                     int index, Object value) throws SQLException {
        ByteBuffer buffer = (ByteBuffer) value;
        int size = buffer.limit();
        byte[] bytes = new byte[size];
        for (int i = 0; i < size; ++i) {
            bytes[i] = buffer.get(i);
        }
        statement.setBytes(index, bytes);
        return index;
    }
}
