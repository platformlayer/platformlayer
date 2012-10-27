package org.platformlayer.metrics.model;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.NumberType;
import com.fasterxml.jackson.core.JsonToken;

public class JsonMetricDataStream implements MetricDataStream {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(JsonMetricDataStream.class);

	/*
	 * final JsonReader jsonParser;
	 * 
	 * JsonMetricDataStream(Reader reader) throws IOException { this.jsonParser = new JsonReader(reader); }
	 * 
	 * @Override public void close() throws IOException { jsonParser.close(); }
	 * 
	 * public static MetricDataStream build(Reader reader) throws IOException { return new JsonMetricDataStream(reader);
	 * }
	 * 
	 * public static MetricDataStream build(InputStream is) throws IOException { InputStreamReader reader = new
	 * InputStreamReader(is, Charsets.UTF_8); return new JsonMetricDataStream(reader); }
	 * 
	 * @Override public void accept(MetricDataVisitor visitor) throws IOException { while (true) { JsonToken token =
	 * jsonParser.peek(); if (token == null) { break; }
	 * 
	 * switch (token) { case BEGIN_OBJECT: jsonParser.beginObject(); visitor.startObject(); break;
	 * 
	 * case END_OBJECT: jsonParser.endObject(); visitor.endObject(); break;
	 * 
	 * case STRING: { String s = jsonParser.nextString(); visitor.gotValueString(s); break; } case NAME: { String key =
	 * jsonParser.nextName(); visitor.gotKey(key); break; }
	 * 
	 * case BEGIN_ARRAY: jsonParser.beginArray(); visitor.startArray(); break;
	 * 
	 * case END_ARRAY: jsonParser.endArray(); visitor.endArray(); break;
	 * 
	 * case BOOLEAN: visitor.gotValueBoolean(jsonParser.nextBoolean()); break;
	 * 
	 * case NULL: jsonParser.nextNull(); visitor.gotValueNull(); break;
	 * 
	 * case NUMBER: // Avoid precision problems String v = jsonParser.nextString(); int digits = 0; for (int i = 0; i <
	 * v.length(); i++) { char c = v.charAt(i); if (i == 0 && (c == '-' || c == '+')) { // Ignore the sign continue; }
	 * if (c >= '0' && c <= '9') { digits++; } else { digits = -1; break; } } if (digits == -1) { double value =
	 * jsonParser.nextDouble(); visitor.gotValueDouble(value); } else { if (digits <= 9) { int value =
	 * jsonParser.nextInt(); visitor.gotValueInt(value); } else { long value = jsonParser.nextLong();
	 * visitor.gotValueLong(value); } } break;
	 * 
	 * default: throw new IllegalStateException("Unexpected token: " + token); } } }
	 */

	final JsonParser jsonParser;

	JsonMetricDataStream(InputStream is) throws IOException {
		JsonFactory jsonFactory = new JsonFactory();
		this.jsonParser = jsonFactory.createJsonParser(is);
	}

	@Override
	public void close() throws IOException {
		jsonParser.close();
	}

	public static MetricDataStream build(InputStream is) throws IOException {
		return new JsonMetricDataStream(is);
	}

	@Override
	public void accept(MetricDataVisitor visitor) throws IOException {
		while (true) {
			JsonToken token = jsonParser.nextToken();
			if (token == null) {
				break;
			}

			switch (token) {
			case START_OBJECT:
				visitor.startObject();
				break;

			case END_OBJECT:
				visitor.endObject();
				break;

			case VALUE_STRING: {
				String s = jsonParser.getText();
				visitor.gotValueString(s);
				break;
			}
			case FIELD_NAME: {
				String key = jsonParser.getText();
				visitor.gotKey(key);
				break;
			}

			case START_ARRAY:
				visitor.startArray();
				break;

			case END_ARRAY:
				visitor.endArray();
				break;

			case VALUE_FALSE:
				visitor.gotValueBoolean(false);
				break;

			case VALUE_TRUE:
				visitor.gotValueBoolean(false);
				break;

			case VALUE_NULL:
				visitor.gotValueNull();
				break;

			case VALUE_NUMBER_FLOAT:
			case VALUE_NUMBER_INT: {
				NumberType numberType = jsonParser.getNumberType();
				switch (numberType) {
				case BIG_INTEGER:
				case BIG_DECIMAL: {
					throw new UnsupportedOperationException("Value too large: " + jsonParser.getBigIntegerValue());
				}

				case INT: {
					int v = jsonParser.getIntValue();
					visitor.gotValueInt(v);
					break;
				}

				case FLOAT: {
					float v = jsonParser.getFloatValue();
					visitor.gotValueFloat(v);
					break;
				}

				case DOUBLE: {
					double v = jsonParser.getDoubleValue();
					visitor.gotValueDouble(v);
					break;
				}

				case LONG: {
					long v = jsonParser.getLongValue();
					visitor.gotValueLong(v);
					break;
				}

				default:
					throw new IllegalStateException("Unexpected number type: " + numberType);
				}
				break;
			}

			case NOT_AVAILABLE:
			case VALUE_EMBEDDED_OBJECT:
			default:
				throw new IllegalStateException("Unexpected token: " + token);
			}
		}

	}
}
