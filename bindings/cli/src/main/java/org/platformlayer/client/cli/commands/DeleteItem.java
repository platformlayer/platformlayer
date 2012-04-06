package org.platformlayer.client.cli.commands;

import org.codehaus.jettison.json.JSONException;
import org.kohsuke.args4j.Argument;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.core.model.PlatformLayerKey;

public class DeleteItem extends PlatformLayerCommandRunnerBase {
    @Argument(index = 0)
    public ItemPath path;

    public DeleteItem() {
        super("delete", "item");
    }

    @Override
    public Object runCommand() throws PlatformLayerClientException, JSONException {
        PlatformLayerClient client = getPlatformLayerClient();

        PlatformLayerKey key = path.resolve(getContext());

        client.deleteItem(key);
        return key.getItemId().getKey();
    }

}
