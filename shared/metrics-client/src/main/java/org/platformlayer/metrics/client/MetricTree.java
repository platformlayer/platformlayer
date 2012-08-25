package org.platformlayer.metrics.client;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.collect.Lists;
import com.yammer.metrics.core.MetricName;

public class MetricTree {
	public enum ChildType {
		Object, IntegerValue, FloatValue, StringValue;
	}

	// TODO: Just use OO? We're paying the memory price now anyway...
	static class Child {
		final ChildType type;
		final String key;

		final MetricTree subtree;
		final double floatValue;
		final long intValue;
		final String stringValue;

		public Child(String key, MetricTree subtree) {
			this.type = ChildType.Object;
			this.key = key;
			this.subtree = subtree;
			this.floatValue = 0;
			this.intValue = 0;
			this.stringValue = null;
		}

		public Child(String key, double value) {
			this.type = ChildType.FloatValue;
			this.key = key;
			this.floatValue = value;
			this.subtree = null;
			this.intValue = 0;
			this.stringValue = null;
		}

		public Child(String key, String value) {
			this.type = ChildType.StringValue;
			this.key = key;
			this.stringValue = value;
			this.subtree = null;
			this.intValue = 0;
			this.floatValue = 0;
		}

		public Child(String key, long value) {
			this.type = ChildType.IntegerValue;
			this.key = key;
			this.floatValue = 0;
			this.subtree = null;
			this.intValue = value;
			this.stringValue = null;
		}

		@Override
		public String toString() {
			switch (type) {
			case FloatValue:
				return key + ": " + floatValue;
			case StringValue:
				return key + ": " + stringValue;
			case IntegerValue:
				return key + ": " + intValue;
			case Object:
				return key + ": " + "{object}";

			default:
				return key + ":" + type + "???";
			}
		}

	}

	final List<Child> children = Lists.newArrayList();

	public void addFloat(String valueName, double value) {
		Child child = findChild(ChildType.FloatValue, valueName);
		if (child != null) {
			throw new IllegalStateException();
		} else {
			child = new Child(valueName, value);
			children.add(child);
		}
	}

	public void addInt(String valueName, long value) {
		Child child = findChild(ChildType.IntegerValue, valueName);
		if (child != null) {
			throw new IllegalStateException();
		} else {
			child = new Child(valueName, value);
			children.add(child);
		}
	}

	public void addString(String valueName, String value) {
		Child child = findChild(ChildType.StringValue, valueName);
		if (child != null) {
			throw new IllegalStateException();
		} else {
			child = new Child(valueName, value);
			children.add(child);
		}
	}

	// protected void sendObjToGraphite(String name, String valueName, Object value) {
	// add(timestamp, name, valueName + " " + String.format(locale, "%s", value));
	// }

	public MetricTree getSubtree(MetricName name) {
		MetricTree current = this;
		current = current.getSubtree(name.getGroup());
		current = current.getSubtree(name.getType());
		if (name.hasScope()) {
			current = current.getSubtree(name.getScope());
		}
		current = current.getSubtree(name.getName());
		return current;
	}

	// public void addObj(String string, Object value) {
	// todo
	// }

	public MetricTree getSubtree(String key) {
		Child child = findChild(ChildType.Object, key);
		if (child == null) {
			child = new Child(key, new MetricTree());
			children.add(child);
		}
		return child.subtree;
	}

	private Child findChild(ChildType type, String key) {
		// We use a linear search because we expect there to be a small number of children
		for (Child child : children) {
			if (child.key.equals(key)) {
				if (child.type != type) {
					throw new IllegalStateException();
				}
				return child;
			}
		}

		return null;
	}

	protected String sanitizeName(MetricName name) {
		final StringBuilder sb = new StringBuilder().append(name.getGroup()).append('.').append(name.getType())
				.append('.');
		if (name.hasScope()) {
			sb.append(name.getScope()).append('.');
		}
		return sb.append(name.getName()).toString();
	}

	protected String sanitizeString(String s) {
		return s.replace(' ', '-');
	}

	public void serialize(JsonGenerator jsonGenerator) throws IOException {
		jsonGenerator.writeStartObject();

		for (Child child : children) {
			jsonGenerator.writeFieldName(child.key);

			switch (child.type) {
			case StringValue:
				jsonGenerator.writeString(child.stringValue);
				break;
			case FloatValue:
				jsonGenerator.writeNumber(child.floatValue);
				break;
			case IntegerValue:
				jsonGenerator.writeNumber(child.intValue);
				break;
			case Object:
				child.subtree.serialize(jsonGenerator);
				break;
			default:
				throw new IllegalStateException();
			}
		}
		jsonGenerator.writeEndObject();
	}

	public void mergeTree(MetricTree add) {
		for (Child addChild : add.children) {
			Child mine = this.findChild(addChild.type, addChild.key);

			switch (addChild.type) {
			case Object: {
				MetricTree subtree;
				if (mine == null) {
					subtree = getSubtree(addChild.key);
				} else {
					subtree = mine.subtree;
				}
				subtree.mergeTree(addChild.subtree);
				break;
			}

			case FloatValue: {
				addFloat(addChild.key, addChild.floatValue);
				break;
			}

			case StringValue: {
				addString(addChild.key, addChild.stringValue);
				break;
			}

			case IntegerValue: {
				addInt(addChild.key, addChild.intValue);
				break;
			}

			default:
				throw new IllegalStateException();
			}
		}
	}

	// protected void sendToGraphite(long timestamp, String name, String value) {
	// try {
	// if (!prefix.isEmpty()) {
	// writer.write(prefix);
	// }
	// writer.write(sanitizeString(name));
	// writer.write('.');
	// writer.write(value);
	// writer.write(' ');
	// writer.write(Long.toString(timestamp));
	// writer.write('\n');
	// writer.flush();
	// } catch (IOException e) {
	// LOG.error("Error sending to Graphite:", e);
	// }
	// }

}
