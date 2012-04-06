package org.platformlayer.client.cli.commands;

import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.client.cli.PlatformLayerCliContext;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.ServiceInfo;
import org.platformlayer.ids.FederationKey;
import org.platformlayer.ids.ItemType;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceType;

import com.fathomdb.cli.commands.CommandRunnerBase;
import com.fathomdb.cli.commands.CommandSpecifier;

public abstract class PlatformLayerCommandRunnerBase extends CommandRunnerBase {

    protected PlatformLayerCommandRunnerBase(String verb, String noun) {
        super(verb, noun);
    }

    protected PlatformLayerCommandRunnerBase(CommandSpecifier commandSpecifier) {
        super(commandSpecifier);
    }

    protected PlatformLayerClient getPlatformLayerClient() {
        return getContext().getPlatformLayerClient();
    }

    protected PlatformLayerCliContext getContext() {
        return (PlatformLayerCliContext) super.getContext();
    }

    protected static String getServiceTypeFromItemType(PlatformLayerClient client, String itemType) throws PlatformLayerClientException {
        Iterable<ServiceInfo> serviceInfo = client.listServices(true);
        for (ServiceInfo service : serviceInfo) {
            for (String type : service.publicTypes) {
                if (type.equals(itemType)) {
                    return service.serviceType;
                }
            }
        }
        throw new PlatformLayerClientException("Cannot find service for item: " + itemType);
    }

    public static PlatformLayerKey pathToKey(PlatformLayerClient client, String path) throws PlatformLayerClientException {
        String serviceType;
        String itemType;
        if (path.contains("/")) {
            String[] components = path.split("/");
            if (components.length != 2) {
                throw new IllegalArgumentException("Cannot parse path: " + path);
            }
            serviceType = components[0];
            itemType = components[1];
        } else {
            itemType = path;
            serviceType = getServiceTypeFromItemType(client, itemType);
        }

        FederationKey host = null;
        ProjectId project = client.getProject();
        return new PlatformLayerKey(host, project, new ServiceType(serviceType), new ItemType(itemType), null);
    }
}
