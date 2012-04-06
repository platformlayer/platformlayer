package org.platformlayer.ops.helpers;

import javax.inject.Inject;

import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.service.imagefactory.v1.DiskImageRecipe;
import org.platformlayer.service.imagefactory.v1.Repository;
import org.platformlayer.service.imagefactory.v1.RepositoryKey;

public class DiskImageRecipeHelper {
    @Inject
    AptHelper apt;

    public void applyRecipe(OpsTarget target, DiskImageRecipe recipe) throws OpsException {
        for (RepositoryKey repositoryKey : recipe.getRepositoryKey()) {
            apt.addRepositoryKeyUrl(target, repositoryKey.getUrl());
        }

        for (Repository repository : recipe.getRepository()) {
            apt.addRepository(target, repository.getKey(), repository.getSource());
        }

        if (recipe.getConfigurePackage() != null) {
            apt.preconfigurePackages(target, recipe.getConfigurePackage());
        }

        // We definitely want to update if we added a repository etc above
        apt.update(target);
        apt.upgrade(target);

        for (String packageName : recipe.getAddPackage()) {
            apt.install(target, packageName);
        }
    }
}
