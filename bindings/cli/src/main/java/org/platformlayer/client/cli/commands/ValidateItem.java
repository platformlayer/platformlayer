package org.platformlayer.client.cli.commands;

import org.kohsuke.args4j.Argument;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.core.model.ValidateAction;

public class ValidateItem extends ActionCommandBase {
	@Argument(index = 0, usage = "path", required = true)
	public ItemPath path;

	public ValidateItem() {
		super("validate");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException {
		ValidateAction action = new ValidateAction();
		return runAction(path, action);
	}

}
