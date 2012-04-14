package org.platformlayer.model;

public abstract class IdWrapper {
	final int id;

	public int getId() {
		return id;
	}

	protected IdWrapper(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return getClass().getName() + " [" + id + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		IdWrapper other = (IdWrapper) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}

}
