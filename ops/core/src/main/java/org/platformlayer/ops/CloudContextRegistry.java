package org.platformlayer.ops;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyPair;

import javax.inject.Inject;

import org.platformlayer.ops.machines.PlatformLayerCloudContext;
import org.platformlayer.ops.ssh.AcceptAllLearningServerKeyVerifier;
import org.platformlayer.ops.ssh.IServerKeyVerifier;
import org.platformlayer.ops.ssh.ISshContext;
import org.platformlayer.ops.ssh.SshConnection;

public class CloudContextRegistry {
    @Inject
    OpsSystem ops;

    public CloudContext getCloudContext(UserInfo userInfo) throws OpsException {
        OpsConfig config = userInfo.getConfig();

        String type = config.getString("compute.type", "platformlayer");
        // if (type.equals("openstack")) {
        // // OpenstackCloudContext currently is stateless (probably shouldn't be!!)
        // return ops.getInjector().getInstance(OpenstackCloudContext.class);
        // }
        if (type.equals("platformlayer")) {
            return ops.getInjector().getInstance(PlatformLayerCloudContext.class);
        }
        throw new OpsException("Unknown compute.type value: " + type);
    }

    private SshConnection getSshConnection(String host, String user, KeyPair sshKeyPair) throws OpsException {
        OpsSystem opsSystem = OpsContext.get().getOpsSystem();
        ISshContext sshContext = opsSystem.getSshContext();

        SshConnection sshConnection = sshContext.getSshConnection(user);
        try {
            sshConnection.setHost(InetAddress.getByName(host));
        } catch (UnknownHostException e) {
            throw new OpsException("Error resolving address: " + host, e);
        }

        sshConnection.setKeyPair(sshKeyPair);

        // TODO: Verify the server key once we've learned it
        IServerKeyVerifier serverKeyVerifier = new AcceptAllLearningServerKeyVerifier();
        sshConnection.setServerKeyVerifier(serverKeyVerifier);

        return sshConnection;
    }
}
