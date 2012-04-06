package org.platformlayer.client.cli.commands;

import java.io.PrintWriter;

import org.codehaus.jettison.json.JSONException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.platformlayer.Format;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.UntypedItem;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.core.model.PlatformLayerKey;

public class PutItem extends PlatformLayerCommandRunnerBase {
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

        PlatformLayerKey key = path.resolve(getContext());

        String wrapper = "{ \"" + key.getItemType().getKey() + "\": " + json + " }";
        UntypedItem retval = client.putItem(key, wrapper, Format.JSON);

        return retval;
    }

    @Override
    public void formatRaw(Object o, PrintWriter writer) {
        UntypedItem item = (UntypedItem) o;
        writer.println(item.getPlatformLayerKey());
    }

}
