package org.platformlayer.choice;

import java.util.List;

import org.platformlayer.ops.OpsException;

public interface Chooser<T> {
	T choose(List<T> choices) throws OpsException;
}
