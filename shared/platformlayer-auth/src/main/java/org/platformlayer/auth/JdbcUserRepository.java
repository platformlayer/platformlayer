package org.platformlayer.auth;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.Certificate;
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

import com.google.common.collect.Lists;

public class JdbcUserRepository implements UserRepository, UserDatabase {
	static final Logger log = Logger.getLogger(JdbcUserRepository.class);

	@Inject
	Provider<Connection> connectionProvider;

	@Override
	@JdbcTransaction
	public void addUserToProject(String username, String projectKey, SecretKey projectSecret)
			throws RepositoryException {
		DbHelper db = new DbHelper();
		try {
			UserEntity user = db.findUserByKey(username);
			if (user == null) {
				throw new RepositoryException("User not found");
			}

			ProjectEntity project = db.findProjectByKey(projectKey);
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
			ProjectEntity grantToProject = db.findProjectByKey(grantToProjectKey);
			if (grantToProject == null) {
				throw new RepositoryException("Project not found");
			}

			ProjectEntity onProject = db.findProjectByKey(onProjectKey);
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
	public List<ProjectEntity> listProjectsByUserId(int userId) throws RepositoryException {
		DbHelper db = new DbHelper();
		try {
			List<ProjectEntity> projects = Lists.newArrayList();
			projects.addAll(db.findProjectsByUserId(userId));
			return projects;
		} catch (SQLException e) {
			throw new RepositoryException("Error reading groups", e);
		} finally {
			db.close();
		}
	}

	@Override
	@JdbcTransaction
	public OpsUser createUser(String userName, String password, Certificate[] certificateChain)
			throws RepositoryException {
		DbHelper db = new DbHelper();
		try {
			byte[] secretData;

			SecretKey userSecretKey = AesUtils.generateKey();

			try {
				byte[] userSecret = AesUtils.serialize(userSecretKey);

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				SecretStore.Writer writer = new SecretStore.Writer(baos);

				// For password auth
				if (password != null) {
					writer.writeUserPassword(userSecret, password);
				}

				// For token auth
				{
					byte[] tokenSecret = CryptoUtils.generateSecureRandom(userSecret.length);
					writer.writeLockedByToken(userSecret, UserEntity.TOKEN_ID_DEFAULT, tokenSecret);
				}

				// For certificate auth
				if (certificateChain != null) {
					Certificate certificate = certificateChain[0];
					PublicKey publicKey = certificate.getPublicKey();
					writer.writeGenericAsymetricKey(userSecret, publicKey);
				}

				writer.close();

				secretData = baos.toByteArray();
			} catch (IOException e) {
				throw new RepositoryException("Error encrypting secrets", e);
			}

			byte[] hashedPassword = null;
			if (password != null) {
				hashedPassword = PasswordHash.doPasswordHash(password);
			}

			// This keypair is for grants etc. The client doesn't (currently) get access to the private key
			KeyPair userRsaKeyPair = RsaUtils.generateRsaKeyPair(RsaUtils.SMALL_KEYSIZE);
			byte[] privateKeyData = RsaUtils.serialize(userRsaKeyPair.getPrivate());
			privateKeyData = AesUtils.encrypt(userSecretKey, privateKeyData);
			byte[] publicKeyData = RsaUtils.serialize(userRsaKeyPair.getPublic());

			db.insertUser(userName, hashedPassword, secretData, publicKeyData, privateKeyData);

			UserEntity user = findUser(userName);
			user.unlockWithPassword(password);
			return user;
		} catch (SQLException e) {
			throw new RepositoryException("Error creating user", e);
		} finally {
			db.close();
		}
	}

	@Override
	@JdbcTransaction
	public UserEntity findUserById(int userId) throws RepositoryException {
		DbHelper db = new DbHelper();
		try {
			UserEntity user = db.findUserById(userId);

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
		UserEntity findUserByKey(String key) throws SQLException;

		@Query("SELECT * FROM users WHERE id=?")
		UserEntity findUserById(int userId) throws SQLException;

		@Query("SELECT p.* FROM projects as p, user_projects as up WHERE up.user_id=? and p.id = up.project_id")
		List<ProjectEntity> findProjectsByUserId(int userId) throws SQLException;

		@Query("SELECT * FROM projects WHERE key=?")
		ProjectEntity findProjectByKey(String key) throws SQLException;

		@Query("UPDATE projects SET secret=? WHERE id=?")
		int updateProjectSecret(byte[] secret, int projectId) throws SQLException;

		@Query("SELECT * FROM service_accounts WHERE subject=? and public_key=?")
		ServiceAccountEntity findServiceAccount(String subject, byte[] publicKey) throws SQLException;

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
			UserEntity user = findUserByKey(key);
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

		public UserEntity findUserByKey(String key) throws SQLException {
			return queries.findUserByKey(key);
		}

		public List<String> listUsers(String keyLike) throws SQLException {
			return queries.listUsers(keyLike);
		}

		public List<String> listProjects(String keyLike) throws SQLException {
			return queries.listProjects(keyLike);
		}

		public UserEntity findUserById(int userId) throws SQLException {
			return queries.findUserById(userId);
		}

		public List<ProjectEntity> findProjectsByUserId(int userId) throws SQLException {
			return queries.findProjectsByUserId(userId);
		}

		@Override
		public void close() {
		}

		public int createProject(String key, byte[] secretData, byte[] metadata, byte[] publicKeyData,
				byte[] privateKeyData) throws SQLException {
			return queries.insertProject(key, secretData, metadata, publicKeyData, privateKeyData);
		}

		public ProjectEntity findProjectByKey(String key) throws SQLException {
			return queries.findProjectByKey(key);
		}

		public ServiceAccountEntity findServiceAccount(String subject, byte[] publicKey) throws SQLException {
			return queries.findServiceAccount(subject, publicKey);
		}

		public int insertServiceAccount(String subject, byte[] publicKey) throws SQLException {
			return queries.insertServiceAccount(subject, publicKey);
		}

	}

	@Override
	@JdbcTransaction
	public UserEntity findUser(String username) throws RepositoryException {
		if (username == null) {
			return null;
		}

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
	public ProjectEntity findProjectByKey(String key) throws RepositoryException {
		DbHelper db = new DbHelper();
		try {
			return findProjectByKey(db, key);
		} finally {
			db.close();
		}
	}

	ProjectEntity findProjectByKey(DbHelper db, String key) throws RepositoryException {
		try {
			ProjectEntity project = db.findProjectByKey(key);

			return project;
		} catch (SQLException e) {
			throw new RepositoryException("Error reading project", e);
		} finally {
			db.close();
		}
	}

	@Override
	@JdbcTransaction
	public ProjectEntity createProject(String key, OpsUser ownerObject) throws RepositoryException {
		UserEntity owner = (UserEntity) ownerObject;
		if (owner.id == 0 || owner.isLocked()) {
			throw new IllegalArgumentException();
		}

		DbHelper db = new DbHelper();
		try {
			ProjectEntity project;

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

				project = new ProjectEntity();
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

			ProjectEntity created = findProjectByKey(db, key);

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
	public ServiceAccountEntity findServiceAccount(String subject, byte[] publicKey) throws RepositoryException {
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
	public ServiceAccountEntity createServiceAccount(X509Certificate cert) throws RepositoryException {
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

			ServiceAccountEntity existing = db.findServiceAccount(subject, publicKey);
			if (existing == null) {
				db.insertServiceAccount(subject, publicKey);
			} else {
				log.warn("Service account already exists; skipping creation");
			}

			return findServiceAccount(subject, publicKey);
		} catch (SQLException e) {
			throw new RepositoryException("Error creating service account", e);
		} finally {
			db.close();
		}
	}

	@Override
	public UserEntity authenticateWithPassword(String username, String password) throws RepositoryException {
		UserEntity user = findUser(username);

		if (user == null) {
			return null;
		}

		if (!user.isPasswordMatch(password)) {
			return null;
		}

		user.unlockWithPassword(password);

		return user;
	}

	// @Override
	// public OpsProject authenticateProject(int projectId, String projectKey, SecretKey secret)
	// throws RepositoryException {
	// ProjectEntity project = findProjectByKey(projectKey);
	//
	// if (project == null) {
	// return null;
	// }
	//
	// project.setProjectSecret(secret);
	//
	// if (!project.isSecretValid()) {
	// return null;
	// }
	//
	// return project;
	// }

	@Override
	public ProjectEntity findProject(OpsUser user, String projectKey) throws RepositoryException {
		ProjectEntity project = findProjectByKey(projectKey);

		if (project == null) {
			return null;
		}
		project.unlockWithUser(user);

		if (!project.isSecretValid()) {
			return null;
		}

		return project;
	}

	@Override
	public CertificateAuthenticationResponse authenticateWithCertificate(CertificateAuthenticationRequest request)
			throws RepositoryException {
		if (request.username == null) {
			throw new IllegalArgumentException();
		}

		UserEntity user = findUser(request.username);

		if (user == null) {
			return null;
		}

		CertificateAuthenticationResponse response = new CertificateAuthenticationResponse();

		// Check the certificate is (still) valid, and find the encrypted secret
		byte[] challenge = user.findChallenge(request.certificateChain);
		if (challenge == null) {
			return null;
		}

		if (request.challengeResponse != null) {
			user.unlock(AesUtils.deserializeKey(request.challengeResponse));

			response.user = user;

			return response;
		}

		// TODO: Do we need/want to encrypt/obfuscate the challenge?
		response.challenge = challenge;
		return response;
	}
}
