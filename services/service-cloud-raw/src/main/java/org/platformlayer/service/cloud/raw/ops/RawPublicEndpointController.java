package org.platformlayer.service.cloud.raw.ops;

import javax.inject.Inject;

import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsProvider;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.tagger.Tagger;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.raw.model.RawInstance;
import org.platformlayer.service.cloud.raw.model.RawPublicEndpoint;

public class RawPublicEndpointController extends OpsTreeBase {
    @Inject
    PlatformLayerHelpers platformLayerClient;

    @Inject
    InstanceHelpers instanceHelpers;

    @Override
    protected void addChildren() throws OpsException {
        // We can't actually do anything; we can tag it to mark that the port is open

        final RawPublicEndpoint model = OpsContext.get().getInstance(RawPublicEndpoint.class);

        if (model.publicPort != model.backendPort) {
            throw new OpsException("Port remapping not supported by raw cloud");
        }

        {
            Tagger tagger = injected(Tagger.class);

            OpsProvider<TagChanges> tagChanges = new OpsProvider<TagChanges>() {
                @Override
                public TagChanges get() throws OpsException {
                    RawInstance instance = platformLayerClient.getItem(RawInstance.class, model.instance);

                    String publicAddress = null;
                    for (String tagValue : instance.getTags().find(Tag.NETWORK_ADDRESS)) {
                        publicAddress = tagValue;
                    }

                    if (publicAddress == null) {
                        throw new OpsException("Cannot find address for instance: " + model.instance);
                    }

                    TagChanges tagChanges = new TagChanges();
                    tagChanges.addTags.add(new Tag(Tag.PUBLIC_ENDPOINT, publicAddress + ":" + model.publicPort));
                    return tagChanges;
                }
            };
            tagger.platformLayerKey = OpsSystem.toKey(model);
            tagger.tagChangesProvider = tagChanges;

            addChild(tagger);
        }
    }

}
