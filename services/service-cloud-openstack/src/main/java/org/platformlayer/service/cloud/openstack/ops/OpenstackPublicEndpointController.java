package org.platformlayer.service.cloud.openstack.ops;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsProvider;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.metrics.collectd.OpsTreeBase;
import org.platformlayer.ops.tagger.Tagger;
import org.platformlayer.service.cloud.openstack.model.OpenstackInstance;
import org.platformlayer.service.cloud.openstack.model.OpenstackPublicEndpoint;

import com.google.common.base.Strings;

public class OpenstackPublicEndpointController extends OpsTreeBase {
    static final Logger log = Logger.getLogger(OpenstackPublicEndpointController.class);

    @Handler
    public void handler() throws OpsException, IOException {
    }

    @Inject
    PlatformLayerHelpers client;

    // @Inject
    // ImageFactory imageFactory;
    //
    @Override
    protected void addChildren() throws OpsException {
        final OpenstackPublicEndpoint model = OpsContext.get().getInstance(OpenstackPublicEndpoint.class);

        OpenstackInstance instance = client.getItem(OpenstackInstance.class, model.instance);

        CloudInstanceMapper instanceMapper;
        {
            instanceMapper = injected(CloudInstanceMapper.class);
            instanceMapper.instance = instance;
            addChild(instanceMapper);
        }

        final EnsureFirewallIngress ingress;
        {
            ingress = injected(EnsureFirewallIngress.class);
            ingress.model = model;
            instanceMapper.addChild(ingress);
        }

        {
            OpsProvider<TagChanges> tagChanges = new OpsProvider<TagChanges>() {
                @Override
                public TagChanges get() {
                    TagChanges tagChanges = new TagChanges();
                    String address = ingress.getPublicAddress();
                    if (Strings.isNullOrEmpty(address))
                        throw new IllegalStateException();
                    tagChanges.addTags.add(new Tag(Tag.PUBLIC_ENDPOINT, address + ":" + model.publicPort));
                    return tagChanges;
                }
            };

            Tagger tagger = injected(Tagger.class);

            tagger.platformLayerKey = OpsSystem.toKey(model);
            tagger.tagChangesProvider = tagChanges;

            instanceMapper.addChild(tagger);

            Tagger tagInstance = injected(Tagger.class);

            tagInstance.platformLayerKey = null;
            tagInstance.platformLayerKey = model.instance;
            tagInstance.tagChangesProvider = tagChanges;

            instanceMapper.addChild(tagInstance);
        }
    }
}
