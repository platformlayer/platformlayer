package org.platformlayer.keystone.cli;

import org.kohsuke.args4j.CmdLineParser;
import org.platformlayer.keystone.cli.commands.KeystoneCommandRegistry;
import org.platformlayer.keystone.cli.model.ProjectName;
import org.platformlayer.keystone.cli.model.UserName;

import com.fathomdb.cli.CliBase;
import com.fathomdb.cli.CliContext;
import com.fathomdb.cli.CliHandler;
import com.fathomdb.cli.CliOptions;
import com.fathomdb.cli.StringWrapperOptionHandler;
import com.fathomdb.cli.commands.CommandRegistry;
import com.martiansoftware.nailgun.NGContext;

public class KeystoneCli extends CliBase {
    static class PlatformLayerCliHandler implements CliHandler {
        @Override
        public CliOptions buildOptionsBean() {
            return new KeystoneCliOptions();
        }

        @Override
        public CliContext buildContext(CommandRegistry commandRegistry, CliOptions options) throws Exception {
            return new KeystoneCliContext((KeystoneCommandRegistry) commandRegistry, (KeystoneCliOptions) options);
        }

        @Override
        public CommandRegistry buildCommandRegistry() {
            return new KeystoneCommandRegistry();
        }
    }

    static {
        CmdLineParser.registerHandler(UserName.class, StringWrapperOptionHandler.class);
        CmdLineParser.registerHandler(ProjectName.class, StringWrapperOptionHandler.class);

        init(new PlatformLayerCliHandler());
    }

    public static void main(String[] args) {
        CliBase.main(args);
    }

    public static void nailMain(NGContext nailgunContext) {
        CliBase.nailMain(nailgunContext);
    }

}
