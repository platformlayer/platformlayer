package org.platformlayer.client.cli.output;

import org.platformlayer.client.cli.PlatformLayerCliContext;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ids.ProjectId;

import com.google.common.base.Objects;

public class Utils {
	public static String formatUrl(PlatformLayerCliContext context, PlatformLayerKey key) {
		String text = key.getUrl();

		if (key.getHost() == null) {
			ProjectId project = context.getProject();
			if (Objects.equal(project, context.getProject())) {
				text = "pl:" + key.getItemTypeString() + "/" + key.getItemIdString();
			}
		}

		return text;
	}

	public static String reformatText(PlatformLayerCliContext context, String text) {
		if (text.startsWith("platform://")) {
			// This looks like a PlatformLayerKey
			try {
				PlatformLayerKey key = PlatformLayerKey.parse(text);
				text = formatUrl(context, key);
			} catch (Exception e) {
				// Ignore
			}
		}
		return text;
	}
}
