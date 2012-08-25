package org.platformlayer.metrics.client;

import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class MetricTreeSerializer {

	final JsonFactory jsonFactory;

	public MetricTreeSerializer(JsonFactory jsonFactory) {
		this.jsonFactory = jsonFactory;
	}

	public MetricTreeSerializer() {
		this(new JsonFactory());
	}

	public void serialize(MetricTree tree, OutputStream os) throws IOException {
		JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(os);
		tree.serialize(jsonGenerator);
		jsonGenerator.close();
	}

}
