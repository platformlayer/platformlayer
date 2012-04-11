package org.platformlayer.client.cli.commands;

import java.io.PrintWriter;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.PlatformLayerUtils;
import org.platformlayer.UntypedItem;
import org.platformlayer.client.cli.model.ItemPath;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;

import com.google.common.collect.Lists;

public class GetEndpoint extends PlatformLayerCommandRunnerBase {
    @Argument
    public ItemPath path;

    public GetEndpoint() {
        super("get", "endpoint");
    }

    @Override
    public Object runCommand() throws PlatformLayerClientException {
        // Should this be a tag?
        PlatformLayerClient client = getPlatformLayerClient();

        PlatformLayerKey key = path.resolve(getContext());

        UntypedItem untypedItem = client.getItemUntyped(key);
        List<String> endpoints = PlatformLayerUtils.findEndpoints(untypedItem.getTags());

        // Tag tag = new Tag(Tag.PARENT, "fathomdb/" + key.getRelativePath());
        // List<PersistentInstance> persistentInstances = client.listItems(PersistentInstance.class, tag);
        // for (PersistentInstance persistentInstance : persistentInstances) {
        // findEndpoints(endpoints, persistentInstance.getTags());
        // }
        //
        // for (PersistentInstance persistentInstance : persistentInstances) {
        // Tags tags = persistentInstance.getTags();
        // String instanceId = tags.findUnique(Tag.INSTANCE_KEY);
        // if (instanceId != null) {
        // PlatformLayerKey instanceKey = PlatformLayerKey.parse(instanceId);
        //
        // UntypedItem instance = client.getUntypedItem(instanceKey);
        // if (instance != null) {
        // findEndpoints(endpoints, instance.getTags());
        // }
        // }
        // }

        return endpoints;
    }

    @Override
    public void formatRaw(Object o, PrintWriter writer) {
        if (o == null)
            return;

        List<String> endpoints = (List<String>) o;
        for (String s : endpoints) {
            writer.println(s);
        }
    }

}
