package org.platformlayer.ops.strategies;

import java.util.Collections;
import java.util.List;

import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTree;

public class FindChildren {
	public List<Object> findChildren(Object controller) throws OpsException {
		if (controller instanceof OpsTree) {
			return ((OpsTree) controller).getChildren();
		}

		return Collections.emptyList();
	}
}
