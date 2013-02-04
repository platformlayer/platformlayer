package org.platformlayer.xaas.web.resources;

import java.util.List;

import org.platformlayer.RepositoryException;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.model.Authentication;
import org.platformlayer.model.ProjectAuthorization;
import org.platformlayer.model.RoleId;
import org.platformlayer.xaas.repository.ManagedItemRepository;

import com.fathomdb.crypto.CryptoKey;

public class XaasProjectAuthorization implements ProjectAuthorization {
	final ProjectAuthorization inner;

	int projectId;

	private final ManagedItemRepository repository;

	public XaasProjectAuthorization(ManagedItemRepository repository, ProjectAuthorization inner) {
		this.repository = repository;
		this.inner = inner;
	}

	@Override
	public boolean isLocked() {
		return inner.isLocked();
	}

	@Override
	public CryptoKey getProjectSecret() {
		return inner.getProjectSecret();
	}

	@Override
	public String getName() {
		return inner.getName();
	}

	@Override
	public int getId() {
		// We have our own codes for projects

		if (projectId == 0) {
			try {
				projectId = repository.getProjectCode(new ProjectId(getName()));
			} catch (RepositoryException e) {
				throw new IllegalStateException("Error mapping project code", e);
			}
		}
		return projectId;
	}

	@Override
	public Authentication getUser() {
		return inner.getUser();
	}

	@Override
	public List<RoleId> getRoles() {
		return inner.getRoles();
	}

}
