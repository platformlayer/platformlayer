package org.platformlayer.service.openldap.ops.ldap;

import org.platformlayer.ops.ldap.LdapAttributes;
import org.platformlayer.ops.ldap.LdapDN;

public class OpenLdapServer {
    public static final LdapDN CONFIG_DN = new LdapDN(LdapAttributes.LDAP_ATTRIBUTE_CN, "config");
    public static final LdapDN ADMIN_DN = CONFIG_DN.childDN(LdapAttributes.LDAP_ATTRIBUTE_CN, "admin");

}
