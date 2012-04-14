package org.platformlayer.client.cli.commands;

import java.io.PrintWriter;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.platformlayer.Format;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.UntypedItem;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;

public class PutChild extends PlatformLayerCommandRunnerBase {
	@Argument(index = 0)
	public ItemPath parent;

	@Argument(index = 1)
	public ItemPath path;

	@Option(name = "-j", aliases = "--json", usage = "json")
	@Argument(index = 2)
	public String json;

	public PutChild() {
		super("put", "child");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException, JSONException {
		PlatformLayerClient client = getPlatformLayerClient();

		PlatformLayerKey parentKey;

		{
			// We need to resolve to a full key
			parentKey = parent.resolve(getContext());
			UntypedItem parentItem = client.getItemUntyped(parentKey);
			if (parentItem == null) {
				throw new IllegalArgumentException("Parent item not found: " + parent);
			}
			parentKey = parentItem.getPlatformLayerKey();
		}
		PlatformLayerKey myKey = path.resolve(getContext());

		// TODO: Should this be a common function?
		JSONObject create = new JSONObject(json);
		JSONArray jsonTags = null;
		if (create.has("core.tags")) {
			jsonTags = create.getJSONArray("core.tags");
		} else {
			jsonTags = new JSONArray();
			create.put("core.tags", jsonTags);
		}

		Tag parentTag = Tag.buildParentTag(parentKey);

		JSONObject jsonTag = new JSONObject();
		jsonTag.put("core.key", parentTag.getKey());
		jsonTag.put("core.value", parentTag.getValue());
		JSONObject jsonTagWrapper = new JSONObject();
		jsonTagWrapper.put("core.tags", jsonTag);
		jsonTags.put(jsonTagWrapper);

		JSONObject wrapped = new JSONObject();
		wrapped.put(myKey.getItemType().getKey(), create);

		String data = wrapped.toString();

		UntypedItem retval = client.putItem(myKey, data, Format.JSON);

		return retval;
	}

	@Override
	public void formatRaw(Object o, PrintWriter writer) {
		UntypedItem item = (UntypedItem) o;
		writer.println(item.getPlatformLayerKey());
	}

}
