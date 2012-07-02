package org.platformlayer.auth;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.crypto.SecretKey;
import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.log4j.Logger;
import org.openstack.utils.Utf8;
import org.platformlayer.RepositoryException;
import org.platformlayer.auth.crypto.SecretStore;
import org.platformlayer.auth.crypto.SecretStore.Writer;
import org.platformlayer.crypto.AesUtils;
import org.platformlayer.crypto.CryptoUtils;
import org.platformlayer.crypto.PasswordHash;
import org.platformlayer.crypto.RsaUtils;
import org.platformlayer.jdbc.DbHelperBase;
import org.platformlayer.jdbc.JdbcTransaction;
import org.platformlayer.jdbc.JdbcUtils;
import org.platformlayer.jdbc.proxy.Query;
import org.platformlayer.jdbc.proxy.QueryFactory;

public class JdbcUserRepository implements UserRepository {
	static final Logger log = Logger.getLogger(JdbcUserRepository.class);

	@Inject
	Provider<Connection> connectionProvider;

	@Override
	@JdbcTransaction
	public void addUserToProject(String username, String projectKey, SecretKey projectSecret)
			throws RepositoryException {
		DbHelper db = new DbHelper();
		try {
			OpsUser user = db.findUserByKey(username);
			if (user == null) {
				throw new RepositoryException("User not found");
			}

			OpsProject project = db.findProjectByKey(projectKey);
			if (project == null) {
				throw new RepositoryException("Project not found");
			}

			byte[] projectSecretData = projectSecret.getEncoded();

			PublicKey userPublicKey = user.getPublicKey();

			byte[] newSecretData;
			try {
				SecretStore store = new SecretStore(project.secretData);
				Writer writer = store.buildWriter();
				writer.writeAsymetricUserKey(projectSecretData, user.id, userPublicKey);
				writer.close();
				store.appendContents(writer);

				newSecretData = store.getEncoded();
			} catch (IOException e) {
				throw new RepositoryException("Error writing secrets", e);
			}

			db.updateProjectSecret(project.id, newSecretData);

			db.insertUserProject(user.id, project.id);
		} catch (SQLException e) {
			throw new RepositoryException("Error reading groups", e);
		} finally {
			db.close();
		}
	}

	@Override
	@JdbcTransaction
	public void grantProjectToProject(String grantToProjectKey, String onProjectKey, SecretKey onProjectSecret)
			throws RepositoryException {
		DbHelper db = new DbHelper();
		try {
			OpsProject grantToProject = db.findProjectByKey(grantToProjectKey);
			if (grantToProject == null) {
				throw new RepositoryException("Project not found");
			}

			OpsProject onProject = db.findProjectByKey(onProjectKey);
			if (onProject == null) {
				throw new RepositoryException("Project not found");
			}

			byte[] projectSecretData = onProjectSecret.getEncoded();

			PublicKey grantToProjectPublicKey = grantToProject.getPublicKey();

			byte[] newSecretData;
			try {
				SecretStore store = new SecretStore(onProject.secretData);
				Writer writer = store.buildWriter();
				writer.writeAsymetricProjectKey(projectSecretData, grantToProject.id, grantToProjectPublicKey);
				writer.close();
				store.appendContents(writer);

				newSecretData = store.getEncoded();
			} catch (IOException e) {
				throw new RepositoryException("Error writing secrets", e);
			}

			db.updateProjectSecret(onProject.id, newSecretData);

			// db.insertUserProject(user.id, project.id);
		} catch (SQLException e) {
			throw new RepositoryException("Error reading groups", e);
		} finally {
			db.close();
		}
	}

	@Override
	@JdbcTransaction
	public List<OpsProject> listProjectsByUserId(int userId) throws RepositoryException {
		DbHelper db = new DbHelper();
		try {
			return db.findProjectsByUserId(userId);
		} catch (SQLException e) {
			throw new RepositoryException("Error reading groups", e);
		} finally {
			db.close();
		}
	}

	@Override
	@JdbcTransaction
	public OpsUser createUser(String userName, String password) throws RepositoryException {
		DbHelper db = new DbHelper();
		try {
			byte[] secretData;

			SecretKey userSecret = AesUtils.generateKey();

			try {
				byte[] plaintext = AesUtils.serialize(userSecret);

				byte[] tokenSecret = CryptoUtils.generateSecureRandom(plaintext.length);

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				SecretStore.Writer writer = new SecretStore.Writer(baos);

				writer.writeUserPassword(plaintext, password);
				writer.writeLockedByToken(plaintext, OpsUser.TOKEN_ID_DEFAULT, tokenSecret);
				writer.close();

				secretData = baos.toByteArray();
			} catch (IOException e) {
				throw new RepositoryException("Error encrypting secrets", e);
			}

			byte[] hashedPassword = PasswordHash.doPasswordHash(password);

			KeyPair userRsaKeyPair = RsaUtils.generateRsaKeyPair(RsaUtils.SMALL_KEYSIZE);

			byte[] privateKeyData = RsaUtils.serialize(userRsaKeyPair.getPrivate());
			privateKeyData = AesUtils.encrypt(userSecret, privateKeyData);
			byte[] publicKeyData = RsaUtils.serialize(userRsaKeyPair.getPublic());

			db.insertUser(userName, hashedPassword, secretData, publicKeyData, privateKeyData);

			return findUser(userName);
		} catch (SQLException e) {
			throw new RepositoryException("Error creating user", e);
		} finally {
			db.close();
		}
	}

	@Override
	@JdbcTransaction
	public OpsUser findUserById(int userId) throws RepositoryException {
		DbHelper db = new DbHelper();
		try {
			OpsUser user = db.findUserById(userId);

			return user;
		} catch (SQLException e) {
			throw new RepositoryException("Error reading user", e);
		} finally {
			db.close();
		}
	}

	@Override
	@JdbcTransaction
	public List<String> listAllUserNames(String prefix) throws RepositoryException {
		String match;
		if (prefix == null) {
			match = "%";
		} else {
			match = prefix + "%";
		}

		DbHelper db = new DbHelper();
		try {
			return db.listUsers(match);
		} catch (SQLException e) {
			throw new RepositoryException("Error listing users", e);
		} finally {
			db.close();
		}
	}

	@Override
	@JdbcTransaction
	public List<String> listAllProjectNames(String prefix) throws RepositoryException {
		String match;
		if (prefix == null) {
			match = "%";
		} else {
			match = prefix + "%";
		}

		DbHelper db = new DbHelper();
		try {
			return db.listProjects(match);
		} catch (SQLException e) {
			throw new RepositoryException("Error listing projects", e);
		} finally {
			db.close();
		}
	}

	static interface Queries {
		@Query("INSERT INTO user_projects (user_id, project_id) VALUES (?,?)")
		int insertUserProject(int userId, int projectId) throws SQLException;

		@Query("INSERT INTO projects (key, secret, metadata, public_key, private_key) VALUES (?,?,?,?,?)")
		int insertProject(String key, byte[] secretData, byte[] metadata, byte[] publicKey, byte[] privateKey)
				throws SQLException;

		@Query("SELECT key FROM users WHERE key LIKE ?")
		List<String> listUsers(String keyLike) throws SQLException;

		@Query("SELECT key FROM projects WHERE key LIKE ?")
		List<String> listProjects(String keyLike) throws SQLException;

		@Query("SELECT * FROM users WHERE key=?")
		OpsUser findUserByKey(String key) throws SQLException;

		@Query("SELECT * FROM users WHERE id=?")
		OpsUser findUserById(int userId) throws SQLException;

		@Query("SELECT p.* FROM projects as p, user_projects as up WHERE up.user_id=? and p.id = up.project_id")
		List<OpsProject> findProjectsByUserId(int userId) throws SQLException;

		@Query("SELECT * FROM projects WHERE key=?")
		OpsProject findProjectByKey(String key) throws SQLException;

		@Query("UPDATE projects SET secret=? WHERE id=?")
		int updateProjectSecret(byte[] secret, int projectId) throws SQLException;

		@Query("SELECT * FROM service_accounts WHERE subject=? and public_key=?")
		OpsServiceAccount findServiceAccount(String subject, byte[] publicKey) throws SQLException;

		@Query("INSERT INTO service_accounts (subject, public_key) VALUES (?, ?)")
		int insertServiceAccount(String subject, byte[] publicKey) throws SQLException;
	}

	@Inject
	QueryFactory queryFactory;

	class DbHelper extends DbHelperBase {
		final Queries queries;

		public DbHelper() {
			super(connectionProvider.get());
			this.queries = queryFactory.get(Queries.class);
		}

		public void updateProjectSecret(int projectId, byte[] secret) throws SQLException {
			int updateCount = queries.updateProjectSecret(secret, projectId);
			if (updateCount != 1) {
				throw new IllegalStateException("Unexpected number of rows inserted");
			}
		}

		public void insertUserProject(int userId, int projectId) throws SQLException {
			int updateCount = queries.insertUserProject(userId, projectId);
			if (updateCount != 1) {
				throw new IllegalStateException("Unexpected number of rows inserted");
			}
		}

		public Integer findUserId(String key) throws SQLException {
			OpsUser user = findUserByKey(key);
			if (user == null) {
				return null;
			}
			return user.id;
		}

		public int insertUser(String userName, byte[] hashedPassword, byte[] secretData, byte[] publicKeyData,
				byte[] privateKeyData) throws SQLException {
			Integer userId = null;
			final String sql = "INSERT INTO users (key, password, secret, public_key, private_key) VALUES (?, ?, ?, ?, ?)";

			PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
			ResultSet rs = null;
			try {
				ps.setString(1, userName);
				ps.setBytes(2, hashedPassword);
				ps.setBytes(3, secretData);
				ps.setBytes(4, publicKeyData);
				ps.setBytes(5, privateKeyData);

				int updateCount = ps.executeUpdate();
				if (updateCount != 1) {
					throw new IllegalStateException("Unexpected number of rows inserted");
				}

				rs = ps.getGeneratedKeys();
				while (rs.next()) {
					if (userId != null) {
						throw new IllegalStateException();
					}

					userId = rs.getInt(1);
				}
			} finally {
				JdbcUtils.safeClose(rs);
				JdbcUtils.safeClose(ps);
			}

			if (userId == null) {
				throw new IllegalStateException();
			}
			return userId;
		}

		public OpsUser findUserByKey(String key) throws SQLException {
			return queries.findUserByKey(key);
		}

		public List<String> listUsers(String keyLike) throws SQLException {
			return queries.listUsers(keyLike);
		}

		public List<String> listProjects(String keyLike) throws SQLException {
			return queries.listProjects(keyLike);
		}

		public OpsUser findUserById(int userId) throws SQLException {
			return queries.findUserById(userId);
		}

		public List<OpsProject> findProjectsByUserId(int userId) throws SQLException {
			return queries.findProjectsByUserId(userId);
		}

		@Override
		public void close() {
		}

		public int createProject(String key, byte[] secretData, byte[] metadata, byte[] publicKeyData,
				byte[] privateKeyData) throws SQLException {
			return queries.insertProject(key, secretData, metadata, publicKeyData, privateKeyData);
		}

		public OpsProject findProjectByKey(String key) throws SQLException {
			return queries.findProjectByKey(key);
		}

		public OpsServiceAccount findServiceAccount(String subject, byte[] publicKey) throws SQLException {
			return queries.findServiceAccount(subject, publicKey);
		}

		public int insertServiceAccount(String subject, byte[] publicKey) throws SQLException {
			return queries.insertServiceAccount(subject, publicKey);
		}
	}

	@Override
	@JdbcTransaction
	public OpsUser findUser(String username) throws RepositoryException {
		DbHelper db = new DbHelper();
		try {
			return db.findUserByKey(username);
		} catch (SQLException e) {
			throw new RepositoryException("Error reading user", e);
		} finally {
			db.close();
		}
	}

	@Override
	@JdbcTransaction
	public OpsProject findProjectByKey(String key) throws RepositoryException {
		DbHelper db = new DbHelper();
		try {
			return findProjectByKey(db, key);
		} finally {
			db.close();
		}
	}

	OpsProject findProjectByKey(DbHelper db, String key) throws RepositoryException {
		try {
			OpsProject project = db.findProjectByKey(key);

			return project;
		} catch (SQLException e) {
			throw new RepositoryException("Error reading project", e);
		} finally {
			db.close();
		}
	}

	@Override
	@JdbcTransaction
	public OpsProject createProject(String key, OpsUser owner) throws RepositoryException {
		if (owner.id == 0 || owner.isLocked()) {
			throw new IllegalArgumentException();
		}

		DbHelper db = new DbHelper();
		try {
			OpsProject project;

			byte[] secretData;
			byte[] metadata;
			try {
				SecretKey projectSecret = AesUtils.generateKey();
				byte[] plaintext = AesUtils.serialize(projectSecret);

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				SecretStore.Writer writer = new SecretStore.Writer(baos);

				writer.writeLockedByUserKey(plaintext, owner.id, owner.getUserSecret());
				writer.close();

				secretData = baos.toByteArray();

				String metadataString = key + "\0";
				byte[] metadataPlaintext = Utf8.getBytes(metadataString);
				metadata = AesUtils.encrypt(projectSecret, metadataPlaintext);

				project = new OpsProject();
				project.setProjectSecret(projectSecret);

				KeyPair projectRsaKeyPair = RsaUtils.generateRsaKeyPair(RsaUtils.SMALL_KEYSIZE);
				project.setPublicKey(projectRsaKeyPair.getPublic());
				project.setPrivateKey(projectRsaKeyPair.getPrivate());
			} catch (IOException e) {
				throw new RepositoryException("Error encrypting secrets", e);
			}

			int rows = db.createProject(key, secretData, metadata, project.publicKeyData, project.privateKeyData);
			if (rows != 1) {
				throw new RepositoryException("Unexpected number of rows inserted");
			}

			OpsProject created = findProjectByKey(db, key);

			if (created == null) {
				throw new RepositoryException("Created project not found");
			}

			db.insertUserProject(owner.id, created.id);

			return created;
		} catch (SQLException e) {
			throw new RepositoryException("Error creating project", e);
		} finally {
			db.close();
		}
	}

	@Override
	@JdbcTransaction
	public OpsServiceAccount findServiceAccount(String subject, byte[] publicKey) throws RepositoryException {
		if (publicKey == null || subject == null) {
			throw new IllegalArgumentException();
		}

		DbHelper db = new DbHelper();
		try {
			return db.findServiceAccount(subject, publicKey);
		} catch (SQLException e) {
			throw new RepositoryException("Error reading system account", e);
		} finally {
			db.close();
		}
	}

	@Override
	@JdbcTransaction
	public OpsServiceAccount createServiceAccount(X509Certificate cert) throws RepositoryException {
		DbHelper db = new DbHelper();
		try {
			// byte[] secretData;
			//
			// SecretKey userSecret = AesUtils.generateKey();
			//
			// try {
			// byte[] plaintext = AesUtils.serialize(userSecret);
			//
			// byte[] tokenSecret = CryptoUtils.generateSecureRandom(plaintext.length);
			//
			// ByteArrayOutputStream baos = new ByteArrayOutputStream();
			// SecretStore.Writer writer = new SecretStore.Writer(baos);
			//
			// writer.writeUserPassword(plaintext, password);
			// writer.writeLockedByToken(plaintext, OpsUser.TOKEN_ID_DEFAULT, tokenSecret);
			// writer.close();
			//
			// secretData = baos.toByteArray();
			// } catch (IOException e) {
			// throw new RepositoryException("Error encrypting secrets", e);
			// }
			//
			// byte[] hashedPassword = PasswordHash.doPasswordHash(password);
			//
			// KeyPair userRsaKeyPair = RsaUtils.generateRsaKeyPair(RsaUtils.SMALL_KEYSIZE);
			//
			// byte[] privateKeyData = RsaUtils.serialize(userRsaKeyPair.getPrivate());
			// privateKeyData = AesUtils.encrypt(userSecret, privateKeyData);
			// byte[] publicKeyData = RsaUtils.serialize(userRsaKeyPair.getPublic());
			String subject = cert.getSubjectDN().getName();
			byte[] publicKey = cert.getPublicKey().getEncoded();

			db.insertServiceAccount(subject, publicKey);

			return findServiceAccount(subject, publicKey);
		} catch (SQLException e) {
			throw new RepositoryException("Error creating service account", e);
		} finally {
			db.close();
		}
	}
}
