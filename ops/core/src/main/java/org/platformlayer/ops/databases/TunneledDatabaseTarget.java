package org.platformlayer.ops.databases;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.Secret;
import org.platformlayer.jdbc.JdbcUtils;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.SshOpsTarget;
import org.platformlayer.ops.ssh.SshPortForward;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import com.google.common.net.InetAddresses;

public class TunneledDatabaseTarget extends DatabaseTarget {
	private static final Logger log = Logger.getLogger(TunneledDatabaseTarget.class);

	public static final int POSTGRES_PORT = 5432;

	final String username;
	final Secret password;

	private final OpsTarget target;

	private final String databaseName;

	public TunneledDatabaseTarget(OpsTarget target, String username, Secret password, String databaseName) {
		this.target = target;
		this.username = username;
		this.databaseName = databaseName;
		this.password = password;
	}

	private static SqlResults execute(SshOpsTarget target, String username, Secret password, String databaseName,
			String sql, String maskedSql) throws SQLException, OpsException {
		int port = POSTGRES_PORT;

		InetAddress address = InetAddresses.forString("127.0.0.1");
		InetSocketAddress remoteSocketAddress = new InetSocketAddress(address, port);

		// Socket socket = ((SshOpsTarget) target).buildTunneledSocket();
		//
		// try {
		// socket.connect(remoteSocketAddress);
		// } catch (IOException e) {
		// throw new OpsException("Error connecting tunneled SSH socket", e);
		// }

		SshPortForward forwardLocalPort = null;
		Connection conn = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			forwardLocalPort = target.forwardLocalPort(remoteSocketAddress);

			InetSocketAddress localSocketAddress = forwardLocalPort.getLocalSocketAddress();

			// QueryServerInstanceModel template = Injection.getInstance(QueryServerInstanceModel.class);
			// CloudataQueryServer model = template.getModel();

			String jdbcUrl = "jdbc:postgresql://" + InetAddresses.toAddrString(localSocketAddress.getAddress()) + ":"
					+ localSocketAddress.getPort() + "/" + databaseName;

			Properties props = new Properties();
			props.setProperty("user", username);
			props.setProperty("password", password.plaintext());

			conn = DriverManager.getConnection(jdbcUrl, props);

			statement = conn.createStatement();
			boolean isResultset = statement.execute(sql);

			if (isResultset) {
				rs = statement.getResultSet();
				List<Map<String, Object>> rows = Lists.newArrayList();
				ResultSetMetaData rsmd = rs.getMetaData();
				while (rs.next()) {
					Map<String, Object> row = Maps.newHashMap();
					for (int i = 0; i < rsmd.getColumnCount(); i++) {
						row.put(rsmd.getColumnName(i + 1), rs.getObject(i + 1));
					}
					rows.add(row);
				}

				return new SqlResults(rows);
			} else {
				int updateCount = statement.getUpdateCount();
				return new SqlResults(updateCount);
			}

		} finally {
			JdbcUtils.safeClose(rs);
			JdbcUtils.safeClose(statement);
			JdbcUtils.safeClose(conn);
			Closeables.closeQuietly(forwardLocalPort);
		}

	}

	public SqlResults execute(String sql, String maskedSql) throws SQLException, OpsException {
		return execute((SshOpsTarget) target, username, password, databaseName, sql, maskedSql);
	}

	@Override
	public SqlResults execute(String sql) throws SQLException, OpsException {
		return execute(sql, sql);
	}

	@Override
	public boolean createDatabase(String databaseName) throws OpsException {
		try {
			String sql = "SELECT 1";

			execute((SshOpsTarget) target, username, password, databaseName, sql, sql);
			return false;
		} catch (SQLException e) {
			String sqlState = e.getSQLState();
			// if (execution.getExitCode() == 1 && execution.getStdErr().contains("already exists")) {
			// log.info("Database already exists");
			// return;
			// }

			if (Objects.equal(sqlState, "3D000")) {
				log.info("Database does not already exist");
			} else {
				// if (Objects.equal(sqlState, "42P04")) {
				// log.info("Database already exists");
				// return false;
				// }
				log.info("Unknown code: " + sqlState);
				throw new OpsException("Error checking if database exists", e);
			}
		}

		try {
			String sql = "CREATE DATABASE " + databaseName;
			String maskedSql = sql;

			// No point trying to connect to the database we're trying to create!
			String runInDatabase = "postgres";

			execute((SshOpsTarget) target, username, password, runInDatabase, sql, maskedSql);

			return true;
		} catch (SQLException e) {
			String sqlState = e.getSQLState();
			// if (execution.getExitCode() == 1 && execution.getStdErr().contains("already exists")) {
			// log.info("Database already exists");
			// return;
			// }

			if (Objects.equal(sqlState, "XX000")) {
				log.info("Database already exists");
				return false;
			}
			if (Objects.equal(sqlState, "42P04")) {
				log.info("Database already exists");
				return false;
			}
			log.info("Unknown code: " + sqlState);
			throw new OpsException("Error creating database", e);
		}
	}

}
