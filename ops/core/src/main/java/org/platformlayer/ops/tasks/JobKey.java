package org.platformlayer.ops.tasks;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.OperationType;

class JobKey {
	final PlatformLayerKey targetItemKey;
	final OperationType operationType;

	public JobKey(PlatformLayerKey targetItemKey, OperationType operationType) {
		if (operationType == null) {
			throw new IllegalArgumentException();
		}

		this.targetItemKey = targetItemKey;
		this.operationType = operationType;
	}

	public PlatformLayerKey getTargetItemKey() {
		return targetItemKey;
	}

	@Override
	public String toString() {
		return "JobKey [targetItemKey=" + targetItemKey + ", operationType=" + operationType + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((targetItemKey == null) ? 0 : targetItemKey.hashCode());
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
		if (targetItemKey == null) {
			if (other.targetItemKey != null) {
				return false;
			}
		} else if (!targetItemKey.equals(other.targetItemKey)) {
			return false;
		}
		if (operationType != other.operationType) {
			return false;
		}
		return true;
	}

}