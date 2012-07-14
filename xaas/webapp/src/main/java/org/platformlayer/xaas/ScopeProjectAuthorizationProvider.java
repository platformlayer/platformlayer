package org.platformlayer.xaas;

import javax.inject.Inject;

import org.platformlayer.Scope;
import org.platformlayer.model.ProjectAuthorization;

import com.google.inject.Provider;

public class ScopeProjectAuthorizationProvider implements Provider<ProjectAuthorization> {

	@Inject
	Provider<Scope> scopeProvider;

	@Override
	public ProjectAuthorization get() {
		ProjectAuthorization authentication = null;

		Scope scope = scopeProvider.get();
		if (scope != null) {
			authentication = scope.get(ProjectAuthorization.class);
		}

		return authentication;
	}

}
