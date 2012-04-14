package org.platformlayer.ops.strategies;

import java.util.List;

import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;

public class Strategies {

	public static List<Object> findChildren(Object controller) throws OpsException {
		FindChildren findChildren = Injection.getInstance(FindChildren.class);
		return findChildren.findChildren(controller);
	}
}
