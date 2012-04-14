package org.platformlayer.service.openldap.ops.ldap;

import java.util.List;

import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.ldap.LdapAttributes;
import org.platformlayer.ops.ldap.LdapDN;

import com.google.common.collect.Multimap;

public class OrganizationalRoleLdapEntry extends LdapEntry {
	String cn;
	String userPasssword;

	// # Organizational Role for Directory Manager
	// dn: cn=Manager,dc=fathomdb,dc=test
	// objectClass: organizationalRole
	// cn: Manager
	// description: Directory Manager

	public static OrganizationalRoleLdapEntry build(String cn, LdapDN parentLdapDN) {
		OrganizationalRoleLdapEntry entry = OpsContext.get().getInjector()
				.getInstance(OrganizationalRoleLdapEntry.class);
		entry.setCn(cn);
		entry.setLdapDN(parentLdapDN.childDN(LdapAttributes.LDAP_ATTRIBUTE_CN, cn));
		return entry;
	}

	@Override
	protected void getObjectClasses(List<String> objectClasses) {
		super.getObjectClasses(objectClasses);

		objectClasses.add("organizationalRole");
		objectClasses.add("simpleSecurityObject");
	}

	@Override
	protected void getAdditionalProperties(Multimap<String, String> additionalProperties) {
		super.getAdditionalProperties(additionalProperties);

		additionalProperties.put("cn", getCn());
		if (getUserPasssword() != null) {
			additionalProperties.put("userPassword", getUserPasssword());
		}
	}

	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	public String getUserPasssword() {
		return userPasssword;
	}

	public void setUserPasssword(String userPasssword) {
		this.userPasssword = userPasssword;
	}

}
