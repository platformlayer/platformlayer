package org.platformlayer.ops.ssh;

import java.io.File;
import java.security.PublicKey;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.SshKeys;

public class SshAuthorizedKey {
    public String user;
    public PublicKey publicKey;

    public static SshAuthorizedKey build(String user, PublicKey publicKey) {
        SshAuthorizedKey item = Injection.getInstance(SshAuthorizedKey.class);
        item.user = user;
        item.publicKey = publicKey;
        return item;
    }

    @Handler
    public void handler(OpsTarget target) throws OpsException {
        ensureSshAuthorization(target, user, publicKey);
    }

    public static void ensureSshAuthorization(OpsTarget target, String user, PublicKey sshPublicKey) throws OpsException {
        File homeDir;
        if (user.equals("root")) {
            homeDir = new File("/root");
        } else {
            homeDir = new File("/home/" + user);
        }

        File sshDir = new File(homeDir, ".ssh");
        if (target.getFilesystemInfoFile(sshDir) == null) {
            target.mkdir(sshDir, "400");
            target.chown(sshDir, user, null, false, false);
        }

        File sshAuthorizationsFile = new File(sshDir, "authorized_keys");
        String sshAuthorizations = target.readTextFile(sshAuthorizationsFile);

        // TODO: Check key not already present??

        if (sshAuthorizations == null) {
            sshAuthorizations = "";
        } else {
            sshAuthorizations += "\n";
        }
        sshAuthorizations += SshKeys.serialize(sshPublicKey);
        target.setFileContents(sshAuthorizationsFile, sshAuthorizations);
    }

}
