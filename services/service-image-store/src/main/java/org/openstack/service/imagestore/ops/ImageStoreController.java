package org.openstack.service.imagestore.ops;

import java.io.IOException;
import java.security.PublicKey;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.openstack.service.imagestore.model.ImageStore;
import org.platformlayer.Strings;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpaqueMachine;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.helpers.SshKey;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.metrics.collectd.CollectdCollector;
import org.platformlayer.ops.metrics.collectd.ManagedService;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.ssh.SshAuthorizedKey;
import org.platformlayer.ops.tree.OpsTreeBase;

public class ImageStoreController extends OpsTreeBase {
    static final Logger log = Logger.getLogger(ImageStoreController.class);

    @Inject
    ServiceContext service;

    @Handler
    public void handler() throws OpsException, IOException {
    }

    private boolean isFlavorGlance(ImageStore model) {
        return false;
    }

    @Override
    protected void addChildren() throws OpsException {
        ImageStore model = OpsContext.get().getInstance(ImageStore.class);

        Tag tag;

        boolean useGlance = isFlavorGlance(model);
        if (useGlance) {
            if (Strings.isEmpty(model.dnsName)) {
                throw new IllegalArgumentException("dnsName must be specified");
            }

            addChildrenGlance(model);
            tag = new Tag("endpoint", "glance://" + model.dnsName);
        } else {
            addDirectStore(model);
            tag = new Tag("endpoint", "ssh://imagestore@" + model.dnsName);
        }

        addChild(ItemTagger.build(tag));
    }

    private void addDirectStore(ImageStore model) throws OpsException {
        // Serious bootstrapping problem here!!!
        SshKey serviceKey = service.getSshKey();
        PublicKey sshPublicKey = serviceKey.getKeyPair().getPublic();

        OpaqueMachine machine = new OpaqueMachine(NetworkPoint.forPublicHostname(model.dnsName));
        OpsTarget target = machine.getTarget("imagestore", serviceKey.getKeyPair());

        SshAuthorizedKey.ensureSshAuthorization(target, "imagestore", sshPublicKey);

        // addChild(SshAuthorizedKey.build("root", publicKey));

        // TODO: Re-introduce (but we don't have the machine)
        // addChild(PackageDependency.build("socat"));
    }

    protected void addChildrenGlance(ImageStore model) throws OpsException {
        InstanceBuilder instance = InstanceBuilder.build(model.dnsName, DiskImageRecipeBuilder.buildDiskImageRecipe(this));
        addChild(instance);

        // We’ll stick with glance using SQLite (for now)
        instance.addChild(PackageDependency.build("glance"));
        instance.addChild(ManagedService.build("glance"));

        instance.addChild(CollectdCollector.build());
    }
}
