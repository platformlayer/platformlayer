package org.platformlayer.xaas;

import javax.inject.Inject;

import org.platformlayer.Scope;
import org.platformlayer.ops.auth.OpsAuthentication;

import com.google.inject.Provider;

public class ScopeOpsAuthenticatorProvider implements Provider<OpsAuthentication> {

    @Inject
    Provider<Scope> scopeProvider;

    @Override
    public OpsAuthentication get() {
        OpsAuthentication authentication = null;

        Scope scope = scopeProvider.get();
        if (scope != null) {
            authentication = scope.get(OpsAuthentication.class);
        }

        return authentication;
    }

}
