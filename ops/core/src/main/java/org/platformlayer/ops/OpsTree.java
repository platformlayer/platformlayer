package org.platformlayer.ops;

import java.util.List;

public interface OpsTree extends OpsItem {
	List<Object> getChildren() throws OpsException;
}
