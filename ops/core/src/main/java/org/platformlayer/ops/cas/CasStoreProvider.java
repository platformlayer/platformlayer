package org.platformlayer.ops.cas;

import org.platformlayer.cas.CasStore;
import org.platformlayer.ops.OpsException;

public interface CasStoreProvider {
	public CasStore getCasStore() throws OpsException;
}
