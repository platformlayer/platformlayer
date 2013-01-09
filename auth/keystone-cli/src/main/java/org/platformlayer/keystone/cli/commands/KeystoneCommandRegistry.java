package org.platformlayer.keystone.cli.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.cli.commands.AutoComplete;
import com.fathomdb.cli.commands.CommandRegistryBase;

public class KeystoneCommandRegistry extends CommandRegistryBase {
	static final Logger log = LoggerFactory.getLogger(KeystoneCommandRegistry.class);

	public KeystoneCommandRegistry() {
		addCommand(new AutoComplete());

		discoverCommands(getClass().getPackage());
	}

}
