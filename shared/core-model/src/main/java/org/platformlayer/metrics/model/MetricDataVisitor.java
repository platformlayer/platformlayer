package org.platformlayer.metrics.model;

import java.io.IOException;

public interface MetricDataVisitor {

	void startObject() throws IOException;

	void endObject() throws IOException;

	void gotValueString(String s) throws IOException;

	void gotKey(String key) throws IOException;

	void startArray() throws IOException;

	void endArray() throws IOException;

	void gotValueBoolean(boolean v) throws IOException;

	void gotValueNull() throws IOException;

	void gotValueInt(int v) throws IOException;

	void gotValueFloat(float v) throws IOException;

	void gotValueDouble(double v) throws IOException;

	void gotValueLong(long v) throws IOException;

}
