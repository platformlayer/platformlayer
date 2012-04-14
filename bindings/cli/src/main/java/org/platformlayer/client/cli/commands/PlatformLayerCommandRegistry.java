package org.platformlayer.client.cli.commands;

import com.fathomdb.cli.commands.AutoComplete;
import com.fathomdb.cli.commands.CommandRegistryBase;

public class PlatformLayerCommandRegistry extends CommandRegistryBase {
	public PlatformLayerCommandRegistry() {
		addCommand(new AutoComplete());
		discoverCommands(getClass().getPackage());
	}
}
