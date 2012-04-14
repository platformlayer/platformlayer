package org.platformlayer.service.openldap.ops;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.ops.CustomRecursor;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.filesystem.ManagedFilesystemItem;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.ldap.LdapAttributes;
import org.platformlayer.ops.ldap.LdapDN;
import org.platformlayer.ops.ldap.LdapDN.LdapDNComponent;
import org.platformlayer.ops.ldap.LdapPasswords;
import org.platformlayer.ops.ldap.LdapServerUtilities;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.openldap.model.LdapDomain;
import org.platformlayer.service.openldap.model.LdapService;
import org.platformlayer.service.openldap.ops.ldap.HdbDatabaseEntry;
import org.platformlayer.service.openldap.ops.ldap.OrganizationLdapEntry;
import org.platformlayer.service.openldap.ops.ldap.OrganizationalRoleLdapEntry;
import org.platformlayer.service.openldap.ops.ldap.OrganizationalUnitLdapEntry;

public class LdapDomainController extends OpsTreeBase implements CustomRecursor {
	static final Logger log = Logger.getLogger(LdapDomainController.class);

	@Inject
	ServiceContext service;

	@Handler
	public void doOperation(LdapDomain ldapDomain) throws OpsException, IOException {
	}

	@Override
	protected void addChildren() throws OpsException {
		LdapDomain ldapDomain = OpsContext.get().getInstance(LdapDomain.class);
		String hostName = ldapDomain.organizationName;

		LdapDN ldapBase = LdapServerUtilities.createBaseDN(hostName);

		File dataRoot = new File("/var/ldap/data");
		File dataDir = new File(dataRoot, hostName);

		String ldapBaseOrganization = hostName;

		ManagedFilesystemItem directory = ManagedDirectory.build(dataDir, "0700").setGroup("openldap")
				.setOwner("openldap");
		addChild(directory);

		HdbDatabaseEntry db = buildDatabase(ldapBase, dataDir, hostName);
		addChild(db);

		OrganizationLdapEntry organization = buildOrganization(ldapBase, ldapBaseOrganization);
		organization.setTop(true);
		addChild(organization);

		String ldapAdminPassword = LdapPasswords.getLdapPasswordEncoded(ldapDomain.adminPassword.plaintext());

		OrganizationalRoleLdapEntry organizationalRole = buildOrganizationalRole(ldapBase, LdapAttributes.MANAGER_CN,
				"LDAP Administrator", ldapAdminPassword);
		addChild(organizationalRole);

		OrganizationalUnitLdapEntry users = buildOrganizationUnit(ldapBase, LdapAttributes.LDAP_USERS_CONTAINER_OU,
				"Users");
		addChild(users);

		OrganizationalUnitLdapEntry groups = buildOrganizationUnit(ldapBase, LdapAttributes.LDAP_GROUPS_CONTAINER_OU,
				"Groups");
		addChild(groups);
	}

	private HdbDatabaseEntry buildDatabase(LdapDN ldapBase, File dataDir, String dbName) {
		HdbDatabaseEntry database = Injection.getInstance(HdbDatabaseEntry.class);

		database.setLdapDN(new LdapDN(LdapAttributes.LDAP_ATTRIBUTE_CN, "config").childDN("olcDatabase", "hdb"));
		database.setLdapRoot(ldapBase);
		database.setDataDirectory(dataDir);
		database.setDbName(dbName);

		database.setOnlyConfigureOnForce(true);

		return database;
	}

	private OrganizationalUnitLdapEntry buildOrganizationUnit(LdapDN ldapBase, String ou, String description) {
		OrganizationalUnitLdapEntry entry = OrganizationalUnitLdapEntry.build(ou, ldapBase);
		entry.setDescription(description);
		entry.setOnlyConfigureOnForce(true);

		return entry;
	}

	private OrganizationalRoleLdapEntry buildOrganizationalRole(LdapDN ldapBase, String cn, String description,
			String userPassword) {
		OrganizationalRoleLdapEntry entry = OrganizationalRoleLdapEntry.build(cn, ldapBase);
		entry.setDescription(description);
		entry.setUserPasssword(userPassword);
		entry.setOnlyConfigureOnForce(true);

		return entry;
	}

	private OrganizationLdapEntry buildOrganization(LdapDN ldapBase, String o) {
		LdapDNComponent head = ldapBase.getHead();
		if (!head.attributeName.equals("dc")) {
			throw new IllegalStateException("Unexpected DN: " + ldapBase);
		}

		String dc = head.value;

		OrganizationLdapEntry entry = OrganizationLdapEntry.build(o, dc, ldapBase);
		entry.setOnlyConfigureOnForce(true);

		return entry;
	}

	@Override
	public void doRecurseOperation() throws OpsException {
		RecurseOverAll recursor = Injection.getInstance(RecurseOverAll.class);

		recursor.doRecursion(this, service.getSshKey(), LdapService.class);
	}

}
