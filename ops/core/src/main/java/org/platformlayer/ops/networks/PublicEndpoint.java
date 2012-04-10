package org.platformlayer.ops.networks;

import javax.inject.Inject;
import javax.inject.Provider;

import org.platformlayer.core.model.InstanceBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.PublicEndpointBase;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsProvider;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.UniqueTag;
import org.platformlayer.ops.endpoints.EndpointDnsRecord;
import org.platformlayer.ops.endpoints.EndpointHelpers;
import org.platformlayer.ops.endpoints.EndpointInfo;
import org.platformlayer.ops.firewall.FirewallEntry;
import org.platformlayer.ops.firewall.FirewallRecord;
import org.platformlayer.ops.firewall.FirewallRecord.Protocol;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.machines.PlatformLayerCloudHelpers;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.tagger.Tagger;
import org.platformlayer.ops.tree.OpsTreeBase;

import com.google.common.base.Strings;

public class PublicEndpoint extends OpsTreeBase {
    // public String network;
    public int publicPort;
    public int backendPort;
    // public ItemBase item;
    public String dnsName;
    public PlatformLayerKey tagItem;
    public PlatformLayerKey parentItem;

    public boolean defaultBlocked = true;

    public Protocol protocol = Protocol.Tcp;

    @Inject
    PlatformLayerHelpers platformLayerClient;

    @Inject
    PlatformLayerCloudHelpers platformLayerCloudHelpers;

    @Inject
    InstanceHelpers instanceHelpers;

    @Inject
    OpsContext opsContext;

    @Inject
    EndpointHelpers endpointHelpers;

    private PublicEndpointBase endpoint;

    @Handler
    public void handler(InstanceBase instance) throws OpsException {
        PlatformLayerKey instanceKey = OpsSystem.toKey(instance);

        PublicEndpointBase publicEndpoint = platformLayerCloudHelpers.createPublicEndpoint(instance, parentItem);
        // publicEndpoint.network = network;
        publicEndpoint.publicPort = publicPort;
        publicEndpoint.backendPort = backendPort;
        publicEndpoint.instance = instanceKey;
        publicEndpoint.key = PlatformLayerKey.fromId(instance.getId() + "_" + publicPort);

        // publicEndpoint.getTags().add(OpsSystem.get().createParentTag(instance));

        Tag uniqueTag = UniqueTag.build(instance);
        publicEndpoint.getTags().add(uniqueTag);

        this.endpoint = platformLayerClient.putItemByTag(publicEndpoint, uniqueTag);
    }

    @Override
    protected void addChildren() throws OpsException {
        if (!Strings.isNullOrEmpty(dnsName)) {
            EndpointDnsRecord dns = injected(EndpointDnsRecord.class);
            dns.destinationPort = publicPort;
            dns.endpointProvider = new Provider<PublicEndpointBase>() {
                @Override
                public PublicEndpointBase get() {
                    return endpoint;
                }
            };

            dns.dnsName = dnsName;
            addChild(dns);
        }

        if (tagItem != null) {
            Tagger tagger = injected(Tagger.class);

            OpsProvider<TagChanges> tagChanges = new OpsProvider<TagChanges>() {
                @Override
                public TagChanges get() throws OpsException {
                    TagChanges tagChanges = new TagChanges();

                    EndpointInfo endpointInfo = endpointHelpers.findEndpoint(endpoint.getTags(), publicPort);
                    if (endpointInfo == null) {
                        throw new OpsException("Cannot find endpoint for port: " + publicPort);
                    }

                    tagChanges.addTags.add(new Tag(Tag.PUBLIC_ENDPOINT, endpointInfo.publicIp + ":" + endpointInfo.port));
                    return tagChanges;
                }
            };
            tagger.platformLayerKey = tagItem;
            tagger.tagChangesProvider = tagChanges;

            addChild(tagger);
        }

        if (defaultBlocked) {
            // Block on machine's firewall
            addChild(FirewallEntry.build(FirewallRecord.buildBlockPort(protocol, backendPort)));
        }
    }
}
