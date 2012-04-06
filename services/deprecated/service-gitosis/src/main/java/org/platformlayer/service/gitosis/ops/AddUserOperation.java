package org.platformlayer.service.gitosis.ops;

import java.io.File;

import org.platformlayer.ops.OpsTarget;
import org.platformlayer.service.gitosis.model.GitUser;

public class AddUserOperation extends GitosisOperation {

    final GitUser user;

    public AddUserOperation(GitUser user) {
        this.user = user;
    }

    @Override
    public void doOperation(OpsTarget target, File baseDir) throws Exception {
        File keysDir = new File(baseDir, "keydir");
        String keyFileName = user.username + ".pub";
        File keyFile = new File(keysDir, keyFileName);

        target.setFileContents(keyFile, user.sshPublicKey);
    }

    @Override
    public String getCommitMessage() {
        return "Adding SSH key for user " + user.username;
    }

}
