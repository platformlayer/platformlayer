package org.platformlayer.metrics.client;

import java.io.IOException;
import java.io.OutputStream;

import org.platformlayer.metrics.MetricTreeBase;
import org.platformlayer.metrics.MetricTreeBase.MetricTreeArray;
import org.platformlayer.metrics.MetricTreeBase.MetricTreeFloat;
import org.platformlayer.metrics.MetricTreeBase.MetricTreeInteger;
import org.platformlayer.metrics.MetricTreeBase.MetricTreeString;
import org.platformlayer.metrics.MetricTreeObject;
import org.platformlayer.metrics.MetricTreeVisitor;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;

public class MetricTreeSerializer {

	final JsonFactory jsonFactory;

	public MetricTreeSerializer(JsonFactory jsonFactory) {
		this.jsonFactory = jsonFactory;
	}

	public MetricTreeSerializer() {
		this(new JsonFactory());
	}

	public void serialize(MetricTreeBase tree, OutputStream os) throws IOException {
		final JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(os);

		tree.accept(new MetricTreeVisitor() {
			private void writeKey(MetricTreeBase o) throws JsonGenerationException, IOException {
				if (o.getKey() != null) {
					jsonGenerator.writeFieldName(o.getKey());
				}
			}

			@Override
			public void visit(MetricTreeObject o) {
				try {
					writeKey(o);
					jsonGenerator.writeStartObject();
					o.visitChildren(this);
					jsonGenerator.writeEndObject();
				} catch (IOException e) {
					throw new IllegalStateException("Error serializing to JSON", e);
				}
			}

			@Override
			public void visit(MetricTreeString o) {
				try {
					writeKey(o);
					jsonGenerator.writeString(o.getValue());
				} catch (IOException e) {
					throw new IllegalStateException("Error serializing to JSON", e);
				}
			}

			@Override
			public void visit(MetricTreeArray o) {
				try {
					writeKey(o);
					jsonGenerator.writeStartArray();
					o.visitItems(this);
					jsonGenerator.writeEndArray();
				} catch (IOException e) {
					throw new IllegalStateException("Error serializing to JSON", e);
				}
			}

			@Override
			public void visit(MetricTreeInteger o) {
				try {
					writeKey(o);
					jsonGenerator.writeNumber(o.getValue());
				} catch (IOException e) {
					throw new IllegalStateException("Error serializing to JSON", e);
				}
			}

			@Override
			public void visit(MetricTreeFloat o) {
				try {
					writeKey(o);
					jsonGenerator.writeNumber(o.getValue());
				} catch (IOException e) {
					throw new IllegalStateException("Error serializing to JSON", e);
				}
			}
		});

		jsonGenerator.close();
	}

}
