package org.platformlayer.client.cli.commands;

import java.io.PrintWriter;

import org.kohsuke.args4j.Argument;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.UntypedItem;
import org.platformlayer.client.cli.autocomplete.AutoCompleteItemType;
import org.platformlayer.client.cli.output.UntypedItemFormatter;
import org.platformlayer.core.model.PlatformLayerKey;

import com.fathomdb.cli.autocomplete.AutoCompletor;
import com.fathomdb.cli.autocomplete.SimpleAutoCompleter;
import com.fathomdb.cli.commands.Ansi;

public class ListItems extends PlatformLayerCommandRunnerBase {
    @Argument
    public String path;

    public ListItems() {
        super("list", "items");
    }

    @Override
    public Object runCommand() throws PlatformLayerClientException {
        PlatformLayerClient client = getPlatformLayerClient();

        PlatformLayerKey key = pathToKey(client, path);

        return client.listItemsUntyped(key);
    }

    @Override
    public AutoCompletor getAutoCompleter() {
        return new SimpleAutoCompleter(new AutoCompleteItemType());
    }

    @Override
    public void formatRaw(Object o, PrintWriter writer) {
        Ansi ansi = new Ansi(writer);

        Iterable<UntypedItem> items = (Iterable<UntypedItem>) o;
        for (UntypedItem item : items) {
            UntypedItemFormatter.formatItem(item, ansi, false);
        }

        ansi.reset();
    }

}
