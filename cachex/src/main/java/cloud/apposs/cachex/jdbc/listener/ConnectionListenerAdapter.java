package cloud.apposs.cachex.jdbc.listener;

import cloud.apposs.cachex.jdbc.ConnectionWrapper;

import java.sql.SQLException;

public class ConnectionListenerAdapter implements ConnectionListener {
	@Override
	public void connectionCreated(ConnectionWrapper connection) {
	}

	@Override
	public void connectionInvalid(ConnectionWrapper connection) {
	}

	@Override
	public void connectionRetrived(ConnectionWrapper connection) {
	}

	@Override
	public void poolInvalid(SQLException e) {
	}
}
