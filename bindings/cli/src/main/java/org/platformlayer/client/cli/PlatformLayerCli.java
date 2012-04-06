package org.platformlayer.client.cli;

import org.kohsuke.args4j.CmdLineParser;
import org.platformlayer.client.cli.model.ConfigureAction;
import org.platformlayer.client.cli.model.ItemPath;

import com.fathomdb.cli.CliBase;
import com.fathomdb.cli.CliContext;
import com.fathomdb.cli.CliHandler;
import com.fathomdb.cli.CliOptions;
import com.fathomdb.cli.StringWrapperOptionHandler;
import com.martiansoftware.nailgun.NGContext;

public class PlatformLayerCli extends CliBase {
    static class PlatformLayerCliHandler implements CliHandler {
        @Override
        public CliOptions buildOptionsBean() {
            return new ConfigurationOptions();
        }

        @Override
        public CliContext buildContext(CliOptions options) throws Exception {
            return new PlatformLayerCliContext((ConfigurationOptions) options);
        }
    }

    static {
        CmdLineParser.registerHandler(ConfigureAction.class, StringWrapperOptionHandler.class);
        CmdLineParser.registerHandler(ItemPath.class, StringWrapperOptionHandler.class);

        init(new PlatformLayerCliHandler());
    }

    public static void main(String[] args) {
        CliBase.main(args);
    }

    public static void nailMain(NGContext nailgunContext) {
        CliBase.nailMain(nailgunContext);
    }

}
