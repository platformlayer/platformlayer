package org.platformlayer.ops.ldap;

//package org.openstack.ops.ldap;
//
//import java.util.Hashtable;
//import java.util.List;
//
//import javax.naming.Context;
//import javax.naming.Name;
//import javax.naming.NamingEnumeration;
//import javax.naming.NamingException;
//import javax.naming.directory.Attributes;
//import javax.naming.directory.DirContext;
//import javax.naming.directory.InitialDirContext;
//import javax.naming.directory.SearchControls;
//import javax.naming.directory.SearchResult;
//import javax.naming.ldap.LdapName;
//
//import com.fathomdb.FathomConfig;
//import com.fathomdb.TimeSpan;
//import com.fathomdb.loggers.Logger;
//import com.fathomdb.loggers.Loggers;
//import com.google.common.collect.Lists;
//import com.google.inject.Singleton;
//import com.sun.jndi.toolkit.dir.SearchFilter;
//
//@Singleton
//public class LdapContext {
//	private static final TimeSpan CONNECTION_POOL_TIMEOUT = FathomConfig.getTimeSpan("ldapContext.connectionPoolTimeout", "5m");
//
//	static final Logger log = LoggerFactory.getLogger(LdapContext.class);
//
//	private static final String DEFAULT_CONFIG_PREFIX = "ldap.";
//	final LdapConnectionInformation ldapConnectionInformation;
//
//	public LdapContext(LdapConnectionInformation ldapConnectionInformation) {
//		this.ldapConnectionInformation = ldapConnectionInformation;
//	}
//
//	public LdapContext() {
//		this(LdapConnectionInformation.fromConfig(DEFAULT_CONFIG_PREFIX));
//	}
//
//	public List<SearchResult> doSearch(LdapSearch search) throws NamingException {
//		return doSearch(search.matchAttrs, search.searchBaseDN);
//	}
//
//	public List<SearchResult> doSearch(Attributes matchAttrs, LdapDN searchBaseDN) throws NamingException {
//		List<SearchResult> items = Lists.newArrayList();
//
//		DirContext connection = getLdapConnection();
//		try {
//			String filter = SearchFilter.format(matchAttrs);
//			SearchControls controls = new SearchControls(SearchControls.SUBTREE_SCOPE, 0, 0, null, false, false);
//
//			Name jndiName = searchBaseDN == null ? new LdapName("") : searchBaseDN.asJndiName();
//			NamingEnumeration<SearchResult> searchResults = connection.search(jndiName, filter, controls);
//
//			while (searchResults.hasMore()) {
//				SearchResult searchResult = searchResults.next();
//
//				items.add(searchResult);
//			}
//
//			return items;
//		} finally {
//			releaseConnection(connection);
//		}
//	}
//
//	private DirContext getLdapConnection() throws NamingException {
//		Hashtable<String, String> env = new Hashtable<String, String>();
//		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
//		env.put(Context.PROVIDER_URL, ldapConnectionInformation.buildJndiUrl());
//
//		switch (ldapConnectionInformation.getProtocol()) {
//		case Ldap:
//			break;
//
//		case LdapSsl:
//			env.put(Context.SECURITY_PROTOCOL, "ssl");
//			break;
//
//		default:
//			throw new IllegalStateException();
//		}
//
//		env.put(Context.SECURITY_AUTHENTICATION, "simple");
//		env.put(Context.SECURITY_PRINCIPAL, ldapConnectionInformation.getLdapBindDN().toLdifEncoded());
//		env.put(Context.SECURITY_CREDENTIALS, ldapConnectionInformation.getLdapBindPassword());
//
//		env.put("com.sun.jndi.ldap.connect.pool", "true");
//		env.put("com.sun.jndi.ldap.connect.pool.timeout", CONNECTION_POOL_TIMEOUT.getTotalMilliseconds() + "");
//
//		DirContext dirContext = new InitialDirContext(env);
//		return dirContext;
//	}
//
//	private void releaseConnection(DirContext connection) {
//		try {
//			connection.close();
//		} catch (NamingException e) {
//			log.info("Ignoring error while closing ldap connection", e);
//		}
//	}
// }
