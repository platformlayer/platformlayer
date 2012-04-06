package org.platformlayer.client.cli.commands;

import java.io.PrintWriter;

import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.UntypedItem;
import org.platformlayer.client.cli.output.UntypedItemFormatter;

import com.fathomdb.cli.commands.Ansi;

public class ListRoots extends PlatformLayerCommandRunnerBase {
    public ListRoots() {
        super("list", "roots");
    }

    @Override
    public Object runCommand() throws PlatformLayerClientException {
        PlatformLayerClient client = getPlatformLayerClient();

        return client.listRoots();
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
