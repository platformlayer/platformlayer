package org.platformlayer.keystone.cli.commands;

import com.fathomdb.cli.commands.CommandRunnerBase;
import org.platformlayer.keystone.cli.KeystoneCliContext;

public abstract class KeystoneCommandRunnerBase extends CommandRunnerBase {

    protected KeystoneCommandRunnerBase(String verb, String noun) {
        super(verb, noun);
    }

    @Override
    protected KeystoneCliContext getContext() {
        return (KeystoneCliContext) super.getContext();
    }

}
