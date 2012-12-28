package org.platformlayer.service.openldap.ops.ldap.schema;

import java.io.IOException;

import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.ldap.LdapAttributes;
import org.platformlayer.ops.ldap.LdapDN;
import org.platformlayer.service.openldap.ops.ldap.LdapEntry;
import org.platformlayer.service.openldap.ops.ldap.LdifRecord;
import org.platformlayer.service.openldap.ops.ldap.OpenLdapServer;

import com.fathomdb.io.IoUtils;
import com.google.common.collect.Iterables;

public class LdapSchemaEntry extends LdapEntry {
	String schemaFile;

	public String getSchemaFile() {
		return schemaFile;
	}

	public void setSchemaFile(String schemaFile) {
		this.schemaFile = schemaFile;
	}

	@Override
	public LdifRecord buildLdif() {
		try {
			String ldif = IoUtils.readAllResource(getClass(), getSchemaFile());
			return Iterables.getOnlyElement(LdifRecord.parse(ldif));
		} catch (IOException e) {
			throw new IllegalStateException("Error building LDIF", e);
		}
	}

	public static LdapSchemaEntry build(String schemaName) {
		LdapSchemaEntry ldapSchemaEntry = OpsContext.get().getOpsSystem().getInjector()
				.getInstance(LdapSchemaEntry.class);
		LdapDN ldapDN = OpenLdapServer.CONFIG_DN.childDN(LdapAttributes.LDAP_ATTRIBUTE_CN, schemaName);
		ldapSchemaEntry.setLdapDN(ldapDN);
		ldapSchemaEntry.setSchemaFile(schemaName + ".schema");
		ldapSchemaEntry.setOnlyConfigureOnForce(true);
		return ldapSchemaEntry;
	}

	// @Override
	// protected MultitenantOpenLdapInstance getOpenLdapInstance() {
	// return getAncestor(MultitenantOpenLdapInstance.class);
	// }
}
