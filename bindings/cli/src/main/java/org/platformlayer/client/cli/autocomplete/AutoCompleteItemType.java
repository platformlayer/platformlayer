package org.platformlayer.client.cli.autocomplete;

import java.util.List;

import com.fathomdb.cli.CliContext;
import org.platformlayer.client.cli.PlatformLayerCliContext;
import org.platformlayer.core.model.ServiceInfo;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class AutoCompleteItemType extends PlatformLayerSimpleAutoCompleter {
    @Override
    public List<String> doComplete(CliContext context, String prefix) throws Exception {
        Multimap<String, ServiceInfo> items = ((PlatformLayerCliContext) context).listItemTypes();

        List<String> itemTypes = Lists.newArrayList(items.keySet());
        addSuffix(itemTypes, " ");
        return itemTypes;
    }

}
