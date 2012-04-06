package org.platformlayer.ops.ldap;
//package org.openstack.ops.ldap;
//
//import java.util.Properties;
//
//import com.fathomdb.FathomConfig;
//import com.fathomdb.ldap.LdapDN;
//
//public class LdapConnectionInformation {
//	LdapDN ldapBindDN; // e.g. cn=Manager,dc=fathomdb,dc=test
//	String ldapBindPassword;
//
//	String hostname;
//	int port;
//
//	public enum Protocol {
//		Ldap, LdapSsl
//	};
//
//	Protocol protocol;
//
//	LdapDN baseDN;
//
//	public LdapConnectionInformation() {
//
//	}
//
//	public LdapConnectionInformation(LdapDN ldapBindDN, String ldapBindPassword, String hostname, int port, Protocol protocol, LdapDN baseDN) {
//		super();
//		this.ldapBindDN = ldapBindDN;
//		this.ldapBindPassword = ldapBindPassword;
//		this.hostname = hostname;
//		this.port = port;
//		this.protocol = protocol;
//		this.baseDN = baseDN;
//	}
//
//	public String buildJndiUrl() {
//		String url;
//		switch (protocol) {
//		case Ldap:
//		case LdapSsl:
//			url = "ldap://";
//			break;
//		default:
//			throw new IllegalArgumentException();
//		}
//
//		url += hostname + ":" + port;
//
//		url += "/";
//		if (baseDN != null)
//			url += baseDN.toLdifEncoded();
//		return url;
//	}
//
//	public String buildApacheLdapUrl() {
//		String url;
//		switch (protocol) {
//		case Ldap:
//			url = "ldap://";
//			break;
//		case LdapSsl:
//			url = "ldaps://";
//			break;
//		default:
//			throw new IllegalArgumentException();
//		}
//
//		url += hostname + ":" + port;
//
//		url += "/";
//		url += baseDN.toLdifEncoded();
//		return url;
//	}
//
//	public LdapDN getLdapBindDN() {
//		return ldapBindDN;
//	}
//
//	public void setLdapBindDN(LdapDN ldapBindDN) {
//		this.ldapBindDN = ldapBindDN;
//	}
//
//	public Protocol getProtocol() {
//		return protocol;
//	}
//
//	public void setProtocol(Protocol protocol) {
//		this.protocol = protocol;
//	}
//
//	public LdapDN getBaseDN() {
//		return baseDN;
//	}
//
//	public void setBaseDN(LdapDN baseDN) {
//		this.baseDN = baseDN;
//	}
//
//	public String getLdapBindPassword() {
//		return ldapBindPassword;
//	}
//
//	public void setLdapBindPassword(String ldapBindPassword) {
//		this.ldapBindPassword = ldapBindPassword;
//	}
//
//	public String getHostname() {
//		return hostname;
//	}
//
//	// public void setHostname(String hostname) {
//	// this.hostname = hostname;
//	// }
//
//	public int getPort() {
//		return port;
//	}
//
//	public void setPort(int port) {
//		this.port = port;
//	}
//
//	public static LdapConnectionInformation fromConfig(String configPrefix) {
//		LdapDN ldapBindDN = LdapDN.parseLdifEncoded(FathomConfig.getRequiredString(configPrefix + "bindDN"));
//		String ldapBindPassword = FathomConfig.getRequiredString(configPrefix + "bindPassword");
//		String hostname = FathomConfig.getRequiredString(configPrefix + "host");
//		int port = FathomConfig.getInt(configPrefix + "port", 389);
//		Protocol protocol = FathomConfig.getEnum(Protocol.class, configPrefix + "protocol", Protocol.Ldap);
//		LdapDN baseDN = LdapDN.parseLdifEncoded(FathomConfig.getRequiredString(configPrefix + "baseDN"));
//
//		LdapConnectionInformation connectionInformation = new LdapConnectionInformation(ldapBindDN, ldapBindPassword, hostname, port, protocol, baseDN);
//		return connectionInformation;
//	}
//
// }
