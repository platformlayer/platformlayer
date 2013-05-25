package org.platformlayer.ops;

public interface OpsItem {
	void setLazyDelete(boolean lazyDelete);

	boolean isLazyDelete();
}
