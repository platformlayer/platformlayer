package org.platformlayer.service.git;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.ServiceProviderBase;
import org.platformlayer.service.git.model.GitRepository;
import org.platformlayer.xaas.Service;

import com.google.common.base.Strings;

@Service("git")
public class GitProvider extends ServiceProviderBase {

	@Override
	public void beforeCreateItem(ItemBase item) throws OpsException {
		super.beforeCreateItem(item);

		// TODO: This doesn't feel like the right place for this
		if (item instanceof GitRepository) {
			GitRepository repo = (GitRepository) item;

			if (Strings.isNullOrEmpty(repo.name)) {
				repo.name = repo.getId();
			}
		}
	}

}
