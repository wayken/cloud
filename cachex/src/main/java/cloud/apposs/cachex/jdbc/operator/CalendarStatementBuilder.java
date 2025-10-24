package cloud.apposs.cachex.jdbc.operator;

import cloud.apposs.util.Parser;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

public class CalendarStatementBuilder implements StatementBuilder {
    @Override
    public int build(PreparedStatement statement,
                     int index, Object value) throws SQLException {
        Calendar calendar = (Calendar) value;
        statement.setTimestamp(index, Parser.parseTimestamp(calendar));
        return index;
    }
}
