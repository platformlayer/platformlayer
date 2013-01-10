package org.platformlayer.jdbc;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jolbox.bonecp.ConnectionHandle;
import com.jolbox.bonecp.hooks.AbstractConnectionHook;
import com.jolbox.bonecp.hooks.AcquireFailConfig;
import com.jolbox.bonecp.hooks.ConnectionState;

public class BoneCPConnectionHook extends AbstractConnectionHook {

	private static final Logger log = LoggerFactory.getLogger(BoneCPConnectionHook.class);

	final String key;

	public BoneCPConnectionHook(String key) {
		this.key = key;
	}

	@Override
	public void onAcquire(ConnectionHandle connection) {
		log.debug("Acquired JDBC connection to " + key);
		super.onAcquire(connection);
	}

	@Override
	public void onDestroy(ConnectionHandle connection) {
		log.debug("Destroyed JDBC connection to " + key);
		super.onDestroy(connection);
	}

	@Override
	public boolean onAcquireFail(Throwable t, AcquireFailConfig acquireConfig) {
		log.debug("Failed to acquire connection to " + key);

		return super.onAcquireFail(t, acquireConfig);
	}

	@Override
	public ConnectionState onMarkPossiblyBroken(ConnectionHandle connection, String state, SQLException e) {
		ConnectionState ret = super.onMarkPossiblyBroken(connection, state, e);

		log.warn("JDBC connection possibly broken: " + key + " state=" + state + " ret=" + ret, e);

		return ret;
	}

	@Override
	public boolean onConnectionException(ConnectionHandle connection, String state, Throwable t) {
		log.warn("JDBC connection exception: " + key, t);
		return super.onConnectionException(connection, state, t);
	}

	// @Override
	// public void onQueryExecuteTimeLimitExceeded(ConnectionHandle conn, Statement statement, String sql,
	// Map<Object, Object> logParams, long timeElapsedInNs) {
	//
	// }
	//
	// @Override
	// public void onQueryExecuteTimeLimitExceeded(ConnectionHandle conn, Statement statement, String sql,
	// Map<Object, Object> logParams) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void onQueryExecuteTimeLimitExceeded(String sql, Map<Object, Object> logParams) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void onBeforeStatementExecute(ConnectionHandle conn, StatementHandle statement, String sql,
	// Map<Object, Object> params) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void onAfterStatementExecute(ConnectionHandle conn, StatementHandle statement, String sql,
	// Map<Object, Object> params) {
	// // TODO Auto-generated method stub
	//
	// }

}
