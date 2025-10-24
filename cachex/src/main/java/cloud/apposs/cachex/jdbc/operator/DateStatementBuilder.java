package cloud.apposs.cachex.jdbc.operator;

import cloud.apposs.util.Parser;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

public class DateStatementBuilder implements StatementBuilder {
    @Override
    public int build(PreparedStatement statement,
                     int index, Object value) throws SQLException {
        Date date = (Date) value;
        statement.setTimestamp(index, Parser.parseTimestamp(date));
        return index;
    }
}
