package org.platformlayer.client.cli.model;

import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.client.cli.PlatformLayerCliContext;
import org.platformlayer.client.cli.autocomplete.AutoCompleteItemPath;
import org.platformlayer.core.model.PlatformLayerKey;

import com.fathomdb.cli.StringWrapper;
import com.fathomdb.cli.autocomplete.HasAutoCompletor;

@HasAutoCompletor(AutoCompleteItemPath.class)
public class ItemPath extends StringWrapper {
	public ItemPath(String key) {
		super(key);
	}

	public PlatformLayerKey resolve(PlatformLayerCliContext context) throws PlatformLayerClientException {
		return context.pathToItem(this.getKey());
	}

}
