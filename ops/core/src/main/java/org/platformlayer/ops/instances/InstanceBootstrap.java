package org.platformlayer.ops.instances;

import javax.inject.Inject;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.AptHelper;

public class InstanceBootstrap {

    @Inject
    AptHelper apt;

    @Handler
    public void handler() throws OpsException {
        if (OpsContext.isConfigure()) {
            OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);

            // We always install curl, because we use it to check for http proxy responsiveness
            // TODO: Switch to netcat, to avoid using curl here
            apt.update(target);
            apt.install(target, "curl");
        }
        // if (OpsContext.isDelete()) {
        // OpenstackComputeMachine machine = OpsContext.get().getInstance(OpenstackComputeMachine.class);
        //
        // OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);
        //
        // OpenstackInstance model = OpsContext.get().getInstance(OpenstackInstance.class);
        //
        // if (model.recipeId != null) {
        // DiskImageRecipe recipe = platformLayerClient.getItem(DiskImageRecipe.class, model.recipeId);
        //
        // diskImageRecipeHelper.applyRecipe(target, recipe);
        // }
        // }
    }
}
