package org.platformlayer.service.gitosis.ops;

import java.io.File;

import org.platformlayer.ops.OpsTarget;
import org.platformlayer.service.gitosis.model.GitRepository;

import com.google.common.base.Joiner;

public class AddRepositoryOperation extends GitosisOperation {

    final GitRepository repository;

    public AddRepositoryOperation(GitRepository repository) {
        this.repository = repository;
    }

    @Override
    public void doOperation(OpsTarget target, File baseDir) throws Exception {
        File confFile = new File(baseDir, "gitosis.conf");
        String conf = target.readTextFile(confFile);

        String key = repository.name;

        // TODO: We need to be able to cope with mutations (we have to replace the [group x] section)

        StringBuilder sb = new StringBuilder();
        sb.append("[group " + key + "]\n");
        sb.append("members = " + Joiner.on(" ").join(repository.user) + "\n");
        sb.append("writable = " + key + "\n");

        conf = conf + "\n\n" + sb.toString();

        target.setFileContents(confFile, conf);
    }

    @Override
    public String getCommitMessage() {
        return "Managing repository: " + repository.name;
    }

}
