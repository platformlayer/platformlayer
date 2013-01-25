package org.platformlayer.client.cli.commands;

import org.kohsuke.args4j.Argument;
import org.platformlayer.Format;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.common.UntypedItem;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.core.model.Tags;

public class DeleteTag extends PlatformLayerCommandRunnerBase {
	@Argument(index = 0)
	public ItemPath path;

	@Argument(index = 1)
	public String tagKey;

	@Argument(index = 2)
	public String tagValue;

	public DeleteTag() {
		super("delete", "tag");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException {
		PlatformLayerClient client = getPlatformLayerClient();

		PlatformLayerKey key = path.resolve(getContext());
		UntypedItem ret = client.getItemUntyped(key, Format.XML);

		TagChanges tagChanges = new TagChanges();
		for (Tag tag : ret.getTags()) {
			if (!tagKey.equals(tag.getKey())) {
				continue;
			}

			if (tagValue != null && !tagValue.equals(tag.getValue())) {
				continue;
			}

			tagChanges.removeTags.add(tag);
		}

		Tags newTags = client.changeTags(key, tagChanges, null);
		return newTags;
	}
}
