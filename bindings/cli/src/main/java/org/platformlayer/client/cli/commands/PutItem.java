package org.platformlayer.client.cli.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;

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

import com.fathomdb.cli.CliException;
import com.fathomdb.io.NoCloseInputStream;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;

public class PutItem extends PlatformLayerCommandRunnerBase {
	@Option(name = "-p", aliases = "--parent", usage = "parent")
	public ItemPath parent;

	@Option(name = "-tag", aliases = "--tag", usage = "tag")
	public List<String> tags = Lists.newArrayList();

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

		if (json == null) {
			InputStream stream = new NoCloseInputStream(System.in);
			byte[] data;
			try {
				data = ByteStreams.toByteArray(stream);

				json = new String(data, Charsets.UTF_8);
			} catch (IOException e) {
				throw new CliException("Error reading stdin", e);
			}
		}

		JSONObject jsonObject = new JSONObject(json);

		PlatformLayerKey parentKey = null;
		if (parent != null || !tags.isEmpty()) {

			JSONObject tagsObject = null;
			if (jsonObject.has("tags")) {
				tagsObject = jsonObject.getJSONObject("tags");
			} else {
				tagsObject = new JSONObject();
				jsonObject.put("tags", tagsObject);
			}

			JSONArray tagsArray;
			if (tagsObject.has("tags")) {
				tagsArray = tagsObject.getJSONArray("tags");
			} else {
				tagsArray = new JSONArray();
				tagsObject.put("tags", tagsArray);
			}

			if (parent != null) {
				parentKey = parent.resolve(getContext());
				Tag parentTag = Tag.buildParentTag(parentKey);

				JSONObject jsonTag = new JSONObject();
				jsonTag.put("key", parentTag.getKey());
				jsonTag.put("value", parentTag.getValue());

				tagsArray.put(jsonTag);
			}

			for (String tag : tags) {
				int equalsIndex = tag.indexOf('=');
				if (equalsIndex == -1) {
					throw new CliException("Expected tagname=tagvalue");
				}

				String tagName = tag.substring(0, equalsIndex);
				String tagValue = tag.substring(equalsIndex + 1);

				JSONObject jsonTag = new JSONObject();
				jsonTag.put("key", tagName);
				jsonTag.put("value", tagValue);

				tagsArray.put(jsonTag);
			}
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
