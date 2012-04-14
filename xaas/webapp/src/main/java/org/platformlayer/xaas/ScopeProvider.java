package org.platformlayer.xaas;

import org.platformlayer.Scope;

import com.google.inject.Provider;

public class ScopeProvider implements Provider<Scope> {

	@Override
	public Scope get() {
		Scope contextMap = Scope.get();
		return contextMap;
	}

}
