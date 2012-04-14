package org.platformlayer.service.openldap.ops.ldap;

import java.io.File;
import java.util.List;

import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.ldap.LdapAttributes;
import org.platformlayer.ops.ldap.LdapDN;

import com.google.common.collect.Multimap;

public class HdbDatabaseEntry extends LdapEntry {
	private static final String LDAP_PROPERTY_DB_DIRECTORY = "olcDbDirectory";

	LdapDN ldapRoot;
	File dataDirectory;

	String dbName;

	@Override
	protected void getObjectClasses(List<String> objectClasses) {
		super.getObjectClasses(objectClasses);

		objectClasses.add("olcDatabaseConfig");
		objectClasses.add("olcHdbConfig");
	}

	@Override
	protected void getAdditionalProperties(Multimap<String, String> additionalProperties) {
		super.getAdditionalProperties(additionalProperties);

		additionalProperties.put("olcDatabase", "hdb");

		additionalProperties.put(LDAP_PROPERTY_DB_DIRECTORY, getDataDirectory().toString());

		additionalProperties.put("olcSuffix", getLdapRoot().toLdifEncoded());
		additionalProperties.put("olcDbIndex", "uid pres,eq");
		additionalProperties.put("olcDbIndex", "cn,sn pres,eq,approx,sub");
		additionalProperties.put("olcDbIndex", "objectClass eq");

		// Protected userPassword to self
		// additionalProperties.put("olcAccess", "to dn.subtree=\"" + getLdapRoot() +
		// "\" attrs=userPassword,shadowLastChange by dn=\"" + OpenLdapServer.ADMIN_DN + "\" write by dn=\"" +
		// getManagerDN()
		// + "\" write by anonymous auth by self write by * none");
		additionalProperties.put("olcAccess", "to * attrs=userPassword,shadowLastChange by dn=\""
				+ OpenLdapServer.ADMIN_DN + "\" write by dn=\"" + getManagerDN()
				+ "\" write by anonymous auth by self write by * none");
		additionalProperties.put(
				"olcAccess",
				"to * by dn=\""
						+ OpenLdapServer.ADMIN_DN
						+ "\" write by dn=\""
						+ getManagerDN()
						+ "\" write by dn.subtree=\""
						+ getLdapRoot().childDN(LdapAttributes.LDAP_ATTRIBUTE_OU,
								LdapAttributes.LDAP_USERS_CONTAINER_OU) + "\" read by anonymous auth by * none");

		// olcAccess: {0}to attrs=userPassword,shadowLastChange by dn.base="cn=admin,cn=
		// // config" peername.ip="127.0.0.1" write by anonymous auth by self write by *
		// // none
	}

	@Override
	protected LdifRecord queryCurrentRecord(OpsTarget target) throws OpsException {
		throw new UnsupportedOperationException();

		// // We can't query by DN here, because the DN is prepended with an index number e.g. {1}hdb
		// MultitenantOpenLdapInstance openLdapInstance = getOpenLdapInstance();
		// String ldapPassword = getOpsSystem().getPlaintext(openLdapInstance.getLdapPassword());
		//
		// String filter = "(objectClass=olcHdbConfig)";
		// LdapDN searchBaseDN = new LdapDN(LdapAttributes.LDAP_ATTRIBUTE_CN, "config");
		// List<LdifRecord> records = OpenLdapManager.doLdapQueryChildren(openLdapInstance,
		// openLdapInstance.getAdminDN(), ldapPassword, searchBaseDN, filter);
		//
		// final String findDirectory = getDataDirectory().asString();
		//
		// return LdifRecord.whereUnique(records, new Predicate<LdifRecord>() {
		// @Override
		// public boolean apply(LdifRecord record) {
		// String dbDirectory = record.getPropertyUnique(LDAP_PROPERTY_DB_DIRECTORY);
		// return findDirectory.equals(dbDirectory);
		// }
		// });
	}

	private LdapDN getManagerDN() {
		return getLdapRoot().childDN(LdapAttributes.LDAP_ATTRIBUTE_CN, "Manager");
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public LdapDN getLdapRoot() {
		return ldapRoot;
	}

	public void setLdapRoot(LdapDN ldapRoot) {
		this.ldapRoot = ldapRoot;
	}

	public File getDataDirectory() {
		return dataDirectory;
	}

	public void setDataDirectory(File dataDirectory) {
		this.dataDirectory = dataDirectory;
	}

}
