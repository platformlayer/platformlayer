package org.platformlayer.client.cli.commands;

import java.io.PrintWriter;

import org.kohsuke.args4j.Argument;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.UntypedItem;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.client.cli.output.UntypedItemFormatter;
import org.platformlayer.core.model.PlatformLayerKey;

import com.fathomdb.cli.commands.Ansi;

public class ListChildren extends PlatformLayerCommandRunnerBase {
    @Argument(index = 0)
    public ItemPath path;

    public ListChildren() {
        super("list", "children");
    }

    @Override
    public Object runCommand() throws PlatformLayerClientException {
        PlatformLayerClient client = getPlatformLayerClient();

        PlatformLayerKey key = path.resolve(getContext());
        return client.listChildren(key);
    }

    @Override
    public void formatRaw(Object o, PrintWriter writer) {
        Ansi ansi = new Ansi(writer);

        Iterable<UntypedItem> items = (Iterable<UntypedItem>) o;
        for (UntypedItem item : items) {
            UntypedItemFormatter.formatItem(item, ansi, true);
        }

        ansi.reset();
    }

}
