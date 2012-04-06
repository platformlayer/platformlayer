package org.platformlayer.client.cli.autocomplete;

import java.util.List;

import org.platformlayer.client.cli.PlatformLayerCliContext;
import org.platformlayer.core.model.ServiceInfo;

import com.fathomdb.cli.CliContext;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class AutoCompleteItemPath extends PlatformLayerSimpleAutoCompleter {

    @Override
    public List<String> doComplete(CliContext context, String prefix) throws Exception {
        if (!prefix.contains("/")) {
            Multimap<String, ServiceInfo> itemTypeMap = ((PlatformLayerCliContext) context).listItemTypes();

            List<String> itemTypes = Lists.newArrayList(itemTypeMap.keySet());
            addSuffix(itemTypes, "/");
            return itemTypes;
        } else {
            String[] pathTokens = prefix.split("/");
            if (pathTokens.length == 1 || pathTokens.length == 2) {
                List<String> items = listItems(context, pathTokens[0]);
                addPrefix(items, pathTokens[0] + "/");
                addSuffix(items, " ");
                return items;
            }
        }

        return null;
    }

}
