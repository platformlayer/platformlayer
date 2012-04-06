package org.platformlayer.service.gitosis.ops;

import java.io.File;

import org.platformlayer.ops.OpsTarget;

public abstract class GitosisOperation {

    public abstract void doOperation(OpsTarget target, File baseDir) throws Exception;

    public abstract String getCommitMessage();

}
