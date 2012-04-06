package org.platformlayer.ops.ldap;
//package org.openstack.ops.ldap;
//
//import javax.naming.InvalidNameException;
//import javax.naming.Name;
//import javax.naming.directory.Attributes;
//import javax.naming.directory.BasicAttribute;
//import javax.naming.directory.BasicAttributes;
//import javax.naming.ldap.LdapName;
//
//public class LdapSearch {
//	private static final LdapName EMPTY_LDAP_NAME;
//
//	private static final boolean IGNORE_ATTRIBUTE_CASE = true;
//	final Attributes matchAttrs;
//	LdapDN searchBaseDN = null;
//
//	static {
//		try {
//			EMPTY_LDAP_NAME = new LdapName("");
//		} catch (InvalidNameException e) {
//			throw new IllegalStateException("Error parsing empty LDAP name", e);
//		}
//	}
//
//	public LdapSearch() {
//		this(null);
//	}
//
//	public LdapSearch(LdapDN searchBaseDN) {
//		this.searchBaseDN = searchBaseDN;
//		this.matchAttrs = new BasicAttributes(IGNORE_ATTRIBUTE_CASE);
//	}
//
//	public Attributes getMatchAttrs() {
//		return matchAttrs;
//	}
//
//	public LdapDN getSearchBaseDN() {
//		return searchBaseDN;
//	}
//
//	public void setSearchBaseDN(LdapDN searchBaseDN) {
//		this.searchBaseDN = searchBaseDN;
//	}
//
//	public void matchGroups() {
//		matchAttribute(LdapAttributes.LDAP_ATTRIBUTE_OBJECT_CLASS, LdapAttributes.OBJECT_CLASS_POSIX_GROUP);
//	}
//
//	public void matchInetOrgPersons() {
//		matchAttribute(LdapAttributes.LDAP_ATTRIBUTE_OBJECT_CLASS, LdapAttributes.OBJECT_CLASS_INETORGPERSON);
//	}
//
//	public void matchAttribute(String attributeName, String attributeValue) {
//		matchAttrs.put(new BasicAttribute(attributeName, attributeValue));
//	}
//
//	public Name getSearchBaseJndi() {
//		Name jndiName = searchBaseDN == null ? EMPTY_LDAP_NAME : searchBaseDN.asJndiName();
//		return jndiName;
//	}
//
//	public void matchUid(String uid) {
//		matchAttribute(LdapAttributes.LDAP_ATTRIBUTE_UID, uid);
//	}
//
// }
