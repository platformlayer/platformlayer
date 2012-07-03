package org.platformlayer.client.cli.commands;

import java.io.PrintWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.platformlayer.Format;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.UntypedItem;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;

public class PutItem extends PlatformLayerCommandRunnerBase {
	@Option(name = "-p", aliases = "--parent", usage = "parent")
	public ItemPath parent;

	@Argument(index = 0)
	public ItemPath path;

	@Option(name = "-j", aliases = "--json", usage = "json")
	@Argument(index = 1)
	public String json;

	public PutItem() {
		super("put", "item");
	}

	@Override
	public Object runCommand() throws PlatformLayerClientException, JSONException {
		PlatformLayerClient client = getPlatformLayerClient();

		JSONObject jsonObject = new JSONObject(json);

		PlatformLayerKey parentKey = null;
		if (parent != null) {
			parentKey = parent.resolve(getContext());

			JSONArray jsonTags = null;
			if (jsonObject.has("core.tags")) {
				jsonTags = jsonObject.getJSONArray("core.tags");
			} else {
				jsonTags = new JSONArray();
				jsonObject.put("core.tags", jsonTags);
			}

			Tag parentTag = Tag.buildParentTag(parentKey);

			JSONObject jsonTag = new JSONObject();
			jsonTag.put("core.key", parentTag.getKey());
			jsonTag.put("core.value", parentTag.getValue());
			JSONObject jsonTagWrapper = new JSONObject();
			jsonTagWrapper.put("core.tags", jsonTag);
			jsonTags.put(jsonTagWrapper);

		}

		PlatformLayerKey key = path.resolve(getContext());

		JSONObject wrapped = new JSONObject();
		wrapped.put(key.getItemType().getKey(), jsonObject);

		String data = wrapped.toString();

		UntypedItem retval = client.putItem(key, data, Format.JSON);

		return retval;
	}

	@Override
	public void formatRaw(Object o, PrintWriter writer) {
		UntypedItem item = (UntypedItem) o;
		writer.println(item.getPlatformLayerKey());
	}

}
