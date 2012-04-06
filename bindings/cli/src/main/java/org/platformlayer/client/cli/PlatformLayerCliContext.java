package org.platformlayer.client.cli;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.platformlayer.PlatformLayerAuthenticationException;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.client.cli.commands.PlatformLayerCommandRegistry;
import org.platformlayer.client.cli.output.PlatformLayerFormatterRegistry;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.ServiceInfo;
import org.platformlayer.ids.FederationKey;
import org.platformlayer.ids.ItemType;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.ops.OpsException;

import com.fathomdb.cli.CliContextBase;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class PlatformLayerCliContext extends CliContextBase {
    final ConfigurationOptions options;
    final PlatformLayerClient platformLayer;

    public PlatformLayerCliContext(ConfigurationOptions options) throws IOException, OpsException {
        super(new PlatformLayerCommandRegistry(), new PlatformLayerFormatterRegistry());

        this.options = options;
        this.platformLayer = options.buildPlatformLayerClient();
    }

    public synchronized PlatformLayerClient getPlatformLayerClient() {
        return platformLayer;
    }

    public ConfigurationOptions getOptions() {
        return options;
    }

    public void connect() throws PlatformLayerAuthenticationException {
        getPlatformLayerClient().ensureLoggedIn();
    }

    public static PlatformLayerCliContext get() {
        return (PlatformLayerCliContext) CliContextBase.get();
    }

    public Map<String, ServiceInfo> listServices() throws PlatformLayerClientException {
        Iterable<ServiceInfo> allServices = getAllServiceInfos();
        Map<String, ServiceInfo> services = Maps.newHashMap();
        for (ServiceInfo service : allServices) {
            services.put(service.getServiceType(), service);
        }
        return services;
    }

    private Iterable<ServiceInfo> getAllServiceInfos() throws PlatformLayerClientException {
        PlatformLayerClient client = getPlatformLayerClient();
        Iterable<ServiceInfo> allServices = client.listServices(true);
        return allServices;
    }

    public Multimap<String, ServiceInfo> listItemTypes() throws PlatformLayerClientException {
        Iterable<ServiceInfo> allServices = getAllServiceInfos();
        Multimap<String, ServiceInfo> items = HashMultimap.create();
        for (ServiceInfo service : allServices) {
            for (String itemType : service.getPublicTypes()) {
                items.put(itemType, service);
            }
        }
        return items;
    }

    public PlatformLayerKey pathToItem(String path) throws PlatformLayerClientException {
        String serviceType = null;
        String itemType = null;
        String itemId = null;

        if (path.contains("://")) {
            return PlatformLayerKey.parse(path);
        }

        List<String> components = Lists.newArrayList(path.split("/"));

        if (components.size() <= 1)
            throw new IllegalArgumentException("Cannot resolve path: " + path);

        String head = components.get(0);

        {
            Map<String, ServiceInfo> services = listServices();

            if (services.containsKey(head)) {
                serviceType = services.get(head).serviceType;
                components.remove(0);

                if (components.size() <= 1) {
                    throw new IllegalArgumentException("Cannot resolve path: " + path);
                }

                head = components.get(0);
            }
        }

        Multimap<String, ServiceInfo> items = listItemTypes();
        if (items.containsKey(head)) {
            Collection<ServiceInfo> services = items.get(head);
            if (services.size() > 1) {
                throw new IllegalArgumentException("Cannot resolve path (ambiguous item type): " + path);
            }
            ServiceInfo serviceInfo = Iterables.getOnlyElement(services);
            itemType = head;
            if (serviceType != null) {
                if (!Objects.equal(serviceType, serviceInfo.serviceType)) {
                    throw new IllegalArgumentException("Cannot resolve path (service/item type mismatch): " + path);
                }
            } else {
                serviceType = serviceInfo.serviceType;
            }
            components.remove(0);
        } else {
            throw new IllegalArgumentException("Cannot resolve path (unknown item type): " + path);
        }

        itemId = Joiner.on('/').join(components);

        if (serviceType == null || itemType == null || Strings.isNullOrEmpty(itemId)) {
            throw new IllegalArgumentException("Cannot resolve path: " + path);
        }

        FederationKey host = null;
        ProjectId project = null;
        return new PlatformLayerKey(host, project, new ServiceType(serviceType), new ItemType(itemType), new ManagedItemId(itemId));
    }

}
