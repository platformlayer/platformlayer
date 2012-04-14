package org.platformlayer.ops.guice;

import org.platformlayer.ops.OpsContext;

import com.google.inject.Provider;

public class OpsContextProvider implements Provider<OpsContext> {
	@Override
	public OpsContext get() {
		return OpsContext.get();
	}
}
