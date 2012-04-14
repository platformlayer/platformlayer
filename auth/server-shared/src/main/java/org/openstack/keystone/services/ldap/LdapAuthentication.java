package org.openstack.keystone.services.ldap;

//package org.platformlayer.keystone.services;
//
//import java.util.Hashtable;
//
//import javax.naming.Context;
//import javax.naming.ldap.InitialLdapContext;
//import javax.naming.ldap.LdapContext;
//
//public class LdapAuthentication {
//    public void test() {
//        Hashtable env = new Hashtable();
//        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
//        env.put(Context.PROVIDER_URL, "ldap://ourldap.warwick.ac.uk");
//        env.put("com.sun.jndi.ldap.connect.pool", "true");
//
//        LdapContext ctx = new InitialLdapContext(env, null);
//        // do something useful with ctx
//        ctx.close();
//    }
//
//    // <bean id="ldapContextFactory" class="com.sun.jndi.ldap.LdapCtxFactory" singleton="true"/>
//    //
//    // <bean id="ldapEnv" class="java.util.Hashtable">
//    // <constructor-arg>
//    // <map>
//    // <entry key="java.naming.ldap.derefAliases"><value>never</value></entry>
//    // <entry key="com.sun.jndi.ldap.connect.timeout"><value>5000</value></entry>
//    // <entry key="java.naming.ldap.version"><value>3</value></entry>
//    // <entry key="com.sun.jndi.ldap.connect.pool"><value>true</value></entry>
//    // </map>
//    // </constructor-arg>
//    // </bean>
// }
