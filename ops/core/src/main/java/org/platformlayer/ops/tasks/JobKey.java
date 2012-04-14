package org.platformlayer.ops.tasks;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.OperationType;

class JobKey {
	final PlatformLayerKey itemKey;
	final OperationType operationType;

	public JobKey(PlatformLayerKey itemKey, OperationType operationType) {
		if (operationType == null) {
			throw new IllegalArgumentException();
		}

		this.itemKey = itemKey;
		this.operationType = operationType;
	}

	public PlatformLayerKey getTargetItemKey() {
		return itemKey;
	}

	@Override
	public String toString() {
		return "JobKey [itemKey=" + itemKey + ", operationType=" + operationType + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((itemKey == null) ? 0 : itemKey.hashCode());
		result = prime * result + ((operationType == null) ? 0 : operationType.hashCode());
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
		JobKey other = (JobKey) obj;
		if (itemKey == null) {
			if (other.itemKey != null) {
				return false;
			}
		} else if (!itemKey.equals(other.itemKey)) {
			return false;
		}
		if (operationType != other.operationType) {
			return false;
		}
		return true;
	}

}