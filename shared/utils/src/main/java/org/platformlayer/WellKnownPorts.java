package org.platformlayer;

public class WellKnownPorts {

    /**
     * We bump the keystone ports by one, because we're not Keystone compatible. Keystone went through a Redux, and we
     * use our authentication for secrets. Hopefully the two can converge again.
     */
    public static final int PORT_PLATFORMLAYER_AUTH_ADMIN = 35357 + 1;
    public static final int PORT_PLATFORMLAYER_AUTH_USER = 5000 + 1;

}
