package org.platformlayer.metrics.model;

import java.io.Closeable;
import java.io.IOException;

public interface MetricDataStream extends Closeable {
	void accept(MetricDataVisitor visitor) throws IOException;
}
