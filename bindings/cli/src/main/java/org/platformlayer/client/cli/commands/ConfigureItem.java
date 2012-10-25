package org.platformlayer.client.cli.commands;

import org.kohsuke.args4j.Argument;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.core.model.ConfigureAction;

public class ConfigureItem extends ActionCommandBase {
	@Argument(index = 0, usage = "path", required = true)
	public ItemPath path;

	public ConfigureItem() {
		super("configure");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException {
		ConfigureAction action = new ConfigureAction();
		return runAction(path, action);
	}

}
