package org.platformlayer.ops.ldap;

public class LdapServerUtilities {
    // public static final int PORT = 389;
    // public static final String MANAGER_CN = "Manager";

    public static boolean validateDomainName(String domainName) {
        if (domainName == null || domainName.length() == 0)
            return false;

        for (char c : domainName.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                switch (c) {
                case '.':
                case '-':
                    break;
                default:
                    return false;
                }
            }
        }

        if (domainName.contains(".."))
            return false;

        return true;
    }

    public static LdapDN createBaseDN(String hostName) {
        if (!validateDomainName(hostName))
            throw new IllegalArgumentException("Invalid domain name: " + hostName);

        return LdapDN.fromDomainName(hostName);
    }

    // // public static String getDomainName(PostedDataMap properties) {
    // // String domainName = properties.getStringProperty(JsonProperties.PROP_LDAP_SERVER_DOMAIN_NAME, true);
    // // if (!LdapServerUtilities.validateDomainName(domainName))
    // // throw new IllegalArgumentException("Invalid domain name: " + domainName);
    // //
    // // return domainName;
    // // }
    // //
    // // public static String getHostNamePrefix(PostedDataMap properties) {
    // // String hostName = getDomainName(properties).replace('.', '-') + ".ldap";
    // // return hostName;
    // // }
    // //
    // // public static LdapDN getBaseDN(PostedDataMap properties) {
    // // return LdapDN.parseLdifEncoded(properties.getStringProperty(JsonProperties.PROP_LDAP_BASE_DN, true));
    // // }
    // //
    // // public static LdapDN getManagerDN(PostedDataMap properties) {
    // // return getBaseDN(properties).childDN(LdapAttributes.LDAP_ATTRIBUTE_CN, MANAGER_CN);
    // // }
    // //
    // // public static String getLdapManagerPasswordEncrypted(PostedDataMap properties) {
    // // return properties.getStringProperty(JsonProperties.PROP_LDAP_MANAGER_PASSWORD, true);
    // // }
}