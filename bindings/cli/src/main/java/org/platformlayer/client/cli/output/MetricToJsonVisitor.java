package org.platformlayer.client.cli.output;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.platformlayer.metrics.model.MetricDataStream;
import org.platformlayer.metrics.model.MetricDataVisitor;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class MetricToJsonVisitor implements MetricDataVisitor, Closeable {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(MetricToJsonVisitor.class);

	final JsonGenerator jsonGenerator;

	public MetricToJsonVisitor(OutputStream os) throws IOException {
		JsonFactory jsonFactory = buildJsonFactory();
		JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(os);

		this.jsonGenerator = jsonGenerator;
	}

	public MetricToJsonVisitor(Writer writer) throws IOException {
		JsonFactory jsonFactory = buildJsonFactory();
		JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(writer);

		this.jsonGenerator = jsonGenerator;
	}

	private JsonFactory buildJsonFactory() {
		return new JsonFactory();
	}

	public static String toString(MetricDataStream s) throws IOException {
		// TODO: This is not very efficient; we deserialize a JSON stream and reserialize it
		StringWriter writer = new StringWriter();

		MetricToJsonVisitor visitor = new MetricToJsonVisitor(writer);
		s.accept(visitor);
		visitor.close();

		return writer.toString();
	}

	@Override
	public void startObject() throws IOException {
		jsonGenerator.writeStartObject();
	}

	@Override
	public void endObject() throws IOException {
		jsonGenerator.writeEndObject();
	}

	@Override
	public void gotValueString(String s) throws IOException {
		jsonGenerator.writeString(s);
	}

	@Override
	public void gotKey(String key) throws IOException {
		jsonGenerator.writeFieldName(key);
	}

	@Override
	public void startArray() throws IOException {
		jsonGenerator.writeStartArray();
	}

	@Override
	public void endArray() throws IOException {
		jsonGenerator.writeEndArray();
	}

	@Override
	public void gotValueBoolean(boolean v) throws IOException {
		jsonGenerator.writeBoolean(v);
	}

	@Override
	public void gotValueNull() throws IOException {
		jsonGenerator.writeNull();
	}

	@Override
	public void gotValueInt(int v) throws IOException {
		jsonGenerator.writeNumber(v);
	}

	@Override
	public void gotValueFloat(float v) throws IOException {
		jsonGenerator.writeNumber(v);
	}

	@Override
	public void gotValueDouble(double v) throws IOException {
		jsonGenerator.writeNumber(v);
	}

	@Override
	public void gotValueLong(long v) throws IOException {
		jsonGenerator.writeNumber(v);
	}

	@Override
	public void close() throws IOException {
		jsonGenerator.close();
	}

}
