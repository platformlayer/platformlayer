package org.platformlayer.service.graphite.ops;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.ImageFactory;
import org.platformlayer.OpenstackClientException;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.service.graphite.model.GraphiteService;
import org.platformlayer.service.imagefactory.v1.DiskImageRecipe;
import org.platformlayer.service.instancesupervisor.v1.PersistentInstance;
import org.platformlayer.xaas.model.Managed;

public class GraphiteServiceController {
    static final Logger log = Logger.getLogger(GraphiteServiceController.class);

    @Inject
    OpsContext opsContext;

    @Inject
    ImageFactory imageFactory;

    @Inject
    PlatformLayerClient platformLayer;

    public void doOperation(Managed<GraphiteService> managed) throws OpsException {
        DiskImageRecipe recipe = imageFactory.loadDiskImageResource(getClass(), "DiskImageRecipe.xml");
        String imageId = imageFactory.getOrCreateImage(recipe);

        GraphiteService model = (GraphiteService) managed.getModel();
        PersistentInstance persistentInstance = new PersistentInstance();
        persistentInstance.setImageId(imageId);
        persistentInstance.setDnsName(model.dnsName);

        try {
            platformLayer.create(persistentInstance);
        } catch (OpenstackClientException e) {
            throw new OpsException("Error registering persistent instance", e);
        }
    }
}
