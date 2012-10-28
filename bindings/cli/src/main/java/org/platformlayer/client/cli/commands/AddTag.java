package org.platformlayer.client.cli.commands;

import java.io.PrintWriter;

import org.kohsuke.args4j.Argument;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.common.IsTag;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.core.model.Tags;

public class AddTag extends PlatformLayerCommandRunnerBase {
	@Argument(index = 0, required = true, metaVar = "path")
	public ItemPath path;

	@Argument(index = 1, required = true, metaVar = "key")
	public String tagKey;
	@Argument(index = 2, required = true, metaVar = "value")
	public String tagValue;

	public AddTag() {
		super("add", "tag");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException {
		PlatformLayerClient client = getPlatformLayerClient();

		PlatformLayerKey resolved = path.resolve(getContext());

		TagChanges tagChanges = new TagChanges();
		Tag tag = Tag.build(tagKey, tagValue);
		tagChanges.addTags.add(tag);

		return client.changeTags(resolved, tagChanges);
	}

	@Override
	public void formatRaw(Object o, PrintWriter writer) {
		Tags tags = (Tags) o;
		for (IsTag tag : tags.tags) {
			writer.println(tag.getKey() + "\t" + tag.getValue());
		}
	}
}
