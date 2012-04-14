package org.platformlayer.keystone.cli.commands;

import org.apache.log4j.Logger;

import com.fathomdb.cli.commands.AutoComplete;
import com.fathomdb.cli.commands.CommandRegistryBase;

public class KeystoneCommandRegistry extends CommandRegistryBase {
	static final Logger log = Logger.getLogger(KeystoneCommandRegistry.class);

	public KeystoneCommandRegistry() {
		addCommand(new AutoComplete());

		discoverCommands(getClass().getPackage());
	}

}
