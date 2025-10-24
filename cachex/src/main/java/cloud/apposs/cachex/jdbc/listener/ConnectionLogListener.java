package cloud.apposs.cachex.jdbc.listener;

import cloud.apposs.logger.Logger;

import java.sql.SQLException;

public class ConnectionLogListener extends ConnectionListenerAdapter {
	@Override
	public void poolInvalid(SQLException e) {
		Logger.error("Database access Error, Killing off all Connections");
	}
}
