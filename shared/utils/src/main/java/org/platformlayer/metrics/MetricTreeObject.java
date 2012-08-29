package org.platformlayer.metrics;

import java.util.List;

import com.google.common.collect.Lists;

public class MetricTreeObject extends MetricTreeBase {
	final List<MetricTreeBase> children = Lists.newArrayList();

	public MetricTreeObject(String key) {
		super(key);
	}

	public void addFloat(String key, double value) {
		MetricTreeBase child = findChild(key);
		if (child != null) {
			throw new IllegalStateException();
		} else {
			child = new MetricTreeFloat(key, value);
			children.add(child);
		}
	}

	public void addInt(String key, long value) {
		MetricTreeBase child = findChild(key);
		if (child != null) {
			throw new IllegalStateException();
		} else {
			child = new MetricTreeInteger(key, value);
			children.add(child);
		}
	}

	public void addString(String valueName, String value) {
		MetricTreeBase child = findChild(key);
		if (child != null) {
			throw new IllegalStateException();
		} else {
			child = new MetricTreeString(key, value);
			children.add(child);
		}
	}

	public MetricTreeObject getSubtree(MetricKey key) {
		MetricTreeObject current = this;
		current = current.getSubtree(key.getClass().getName());
		for (String path : key.getPath()) {
			current = current.getSubtree(path);
		}
		return current;
	}

	public MetricTreeArray getArray(String key) {
		MetricTreeBase child = findChild(key);
		if (child == null) {
			child = new MetricTreeArray(key);
			children.add(child);
		}
		if (!(child instanceof MetricTreeArray)) {
			throw new IllegalStateException();
		}
		return ((MetricTreeArray) child);
	}

	public MetricTreeObject addToArray(String key) {
		MetricTreeArray array = getArray(key);
		MetricTreeObject o = new MetricTreeObject(null);
		array.items.add(o);
		return o;
	}

	public MetricTreeObject getSubtree(String key) {
		MetricTreeBase child = findChild(key);
		if (child == null) {
			child = new MetricTreeObject(key);
			children.add(child);
		}
		if (!(child instanceof MetricTreeObject)) {
			throw new IllegalStateException();
		}
		return ((MetricTreeObject) child);
	}

	private MetricTreeBase findChild(String key) {
		// We use a linear search because we expect there to be a small number of children
		for (MetricTreeBase child : children) {
			if (key.equals(child.key)) {
				return child;
			}
		}

		return null;
	}

	public void mergeTree(MetricTreeObject other) {
		// TODO: Move to visitor?
		for (MetricTreeBase otherChild : other.children) {
			MetricTreeBase thisChild = this.findChild(otherChild.key);

			if (thisChild != null) {
				if (thisChild.getClass() != otherChild.getClass()) {
					throw new IllegalStateException();
				}
			}

			if (otherChild instanceof MetricTreeInteger) {
				addInt(otherChild.key, ((MetricTreeInteger) otherChild).getValue());
			} else if (otherChild instanceof MetricTreeFloat) {
				addFloat(otherChild.key, ((MetricTreeFloat) otherChild).getValue());
			} else if (otherChild instanceof MetricTreeString) {
				addString(otherChild.key, ((MetricTreeString) otherChild).getValue());
			} else if (otherChild instanceof MetricTreeObject) {
				MetricTreeObject thisTarget;
				if (thisChild == null) {
					thisTarget = getSubtree(otherChild.key);
				} else {
					thisTarget = (MetricTreeObject) thisChild;
				}
				thisTarget.mergeTree((MetricTreeObject) otherChild);
			} else if (otherChild instanceof MetricTreeArray) {
				MetricTreeArray thisTarget;
				if (thisChild == null) {
					thisTarget = getArray(otherChild.key);
				} else {
					thisTarget = (MetricTreeArray) thisChild;
				}
				thisTarget.items.addAll(((MetricTreeArray) otherChild).items);
			} else {
				throw new IllegalStateException();
			}
		}
	}

	@Override
	public void accept(MetricTreeVisitor visitor) {
		visitor.visit(this);
	}

	public void visitChildren(MetricTreeVisitor visitor) {
		for (MetricTreeBase child : children) {
			child.accept(visitor);
		}
	}

}