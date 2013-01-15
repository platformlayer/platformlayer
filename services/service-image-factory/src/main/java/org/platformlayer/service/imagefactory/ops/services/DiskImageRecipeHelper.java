package org.platformlayer.service.imagefactory.ops.services;

import javax.inject.Inject;

import org.platformlayer.images.model.DiskImageRecipe;
import org.platformlayer.images.model.Repository;
import org.platformlayer.images.model.RepositoryKey;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.packages.AptPackageManager;

public class DiskImageRecipeHelper {
	@Inject
	AptPackageManager apt;

	public void applyRecipe(OpsTarget target, DiskImageRecipe recipe) throws OpsException {
		for (RepositoryKey repositoryKey : recipe.repositoryKey) {
			apt.addRepositoryKeyUrl(target, repositoryKey.url);
		}

		for (Repository repository : recipe.repository) {
			apt.addRepository(target, repository.key, repository.source);
		}

		if (recipe.configurePackage != null) {
			apt.preconfigurePackages(target, recipe.configurePackage);
		}

		// We definitely want to update if we added a repository etc above
		apt.update(target, true);
		apt.upgrade(target);

		for (String packageName : recipe.addPackage) {
			apt.install(target, packageName);
		}
	}
}
