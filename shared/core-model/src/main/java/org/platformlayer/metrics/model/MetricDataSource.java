package org.platformlayer.metrics.model;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

public interface MetricDataSource extends Closeable {

	void serializeAsJson(OutputStream out) throws IOException;

	void serializeAsXml(OutputStream out);

}
