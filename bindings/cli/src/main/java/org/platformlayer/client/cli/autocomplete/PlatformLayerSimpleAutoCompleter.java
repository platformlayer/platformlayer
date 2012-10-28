package org.platformlayer.client.cli.autocomplete;

import java.util.List;

import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.client.cli.PlatformLayerCliContext;
import org.platformlayer.client.cli.commands.PlatformLayerCommandRunnerBase;
import org.platformlayer.common.UntypedItem;
import org.platformlayer.core.model.PlatformLayerKey;

import com.fathomdb.cli.CliContext;
import com.fathomdb.cli.autocomplete.SimpleArgumentAutoCompleter;
import com.google.common.collect.Lists;

public abstract class PlatformLayerSimpleAutoCompleter extends SimpleArgumentAutoCompleter {
	protected PlatformLayerClient getPlatformLayerClient(CliContext context) {
		PlatformLayerCliContext plContext = (PlatformLayerCliContext) context;
		return plContext.getPlatformLayerClient();
	}

	protected List<String> listItems(CliContext context, String itemType) throws PlatformLayerClientException {
		PlatformLayerClient client = getPlatformLayerClient(context);
		PlatformLayerKey key = PlatformLayerCommandRunnerBase.pathToKey(client, itemType);

		List<String> items = Lists.newArrayList();
		for (UntypedItem item : client.listItemsUntyped(key).getItems()) {
			items.add(item.getKey().getItemId().getKey());
		}
		return items;
	}
}
