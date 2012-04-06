package org.openstack.keystone;

import org.junit.Test;
import org.openstack.docs.identity.api.v2.Access;
import org.openstack.docs.identity.api.v2.PasswordCredentials;
import org.openstack.docs.identity.api.v2.Token;
import org.openstack.keystone.auth.client.KeystoneAuthenticationClient;
import org.openstack.keystone.auth.client.KeystoneAuthenticationException;
import org.openstack.keystone.auth.client.KeystoneAuthenticationToken;

public class SmokeTest {

    // private static StandaloneUserServer server;
    //
    // @BeforeClass
    // public static void startServer() throws Exception {
    // File base = new File(".").getCanonicalFile();
    //
    // while (!base.getName().equals("auth")) {
    // base = base.getParentFile();
    // }
    //
    // base = new File(base, "server-user");
    //
    // server = new StandaloneUserServer();
    // server.start(base, 8081);
    // }
    //
    // @AfterClass
    // public static void stopServer() throws Exception {
    // server.stop();
    // }

    @Test
    public void test() throws Exception {
        KeystoneAuthenticationClient client = new KeystoneAuthenticationClient();
        PasswordCredentials passwordCredentials = new PasswordCredentials();
        passwordCredentials.setUsername("user1");
        passwordCredentials.setPassword("secretuser1");

        String tenantId = null;
        client.authenticate(tenantId, passwordCredentials);
    }

    @Test(expected = KeystoneAuthenticationException.class)
    public void listTenants_forgedToken() throws Exception {
        KeystoneAuthenticationClient client = new KeystoneAuthenticationClient();
        KeystoneAuthenticationToken fakeToken = buildFakeToken("HELLOWORLD");
        client.listTenants(fakeToken);
    }

    private KeystoneAuthenticationToken buildFakeToken(String tokenCode) {
        Access auth = new Access();
        Token tokenObject = new Token();
        tokenObject.setId(tokenCode);
        auth.setToken(tokenObject);
        return new KeystoneAuthenticationToken(auth);
    }

}
