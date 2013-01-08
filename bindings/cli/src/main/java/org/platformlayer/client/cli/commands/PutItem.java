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
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.common.UntypedItem;
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

			JSONObject tagsObject = null;
			if (jsonObject.has("tags")) {
				tagsObject = jsonObject.getJSONObject("tags");
			} else {
				tagsObject = new JSONObject();
				jsonObject.put("tags", tagsObject);
			}

			Tag parentTag = Tag.buildParentTag(parentKey);

			JSONObject jsonTag = new JSONObject();
			jsonTag.put("key", parentTag.getKey());
			jsonTag.put("value", parentTag.getValue());

			JSONArray tagsArray;
			if (tagsObject.has("tags")) {
				tagsArray = tagsObject.getJSONArray("tags");
			} else {
				tagsArray = new JSONArray();
				tagsObject.put("tags", tagsArray);
			}

			tagsArray.put(jsonTag);
		}

		PlatformLayerKey key = path.resolve(getContext());

		boolean wrap = false;
		String data;
		if (wrap) {
			JSONObject wrapped = new JSONObject();
			wrapped.put(key.getItemType().getKey(), jsonObject);

			data = wrapped.toString();
		} else {
			data = jsonObject.toString();
		}

		UntypedItem retval = client.putItem(key, data, Format.JSON);

		return retval;
	}

	@Override
	public void formatRaw(Object o, PrintWriter writer) {
		UntypedItem item = (UntypedItem) o;
		writer.println(item.getKey());
	}

}
