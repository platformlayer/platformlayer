//package org.platformlayer.ops.tasks;
//
//import org.platformlayer.core.model.Action;
//import org.platformlayer.core.model.PlatformLayerKey;
//
//class JobKey {
//	final PlatformLayerKey targetItemKey;
//	final Action action;
//
//	public JobKey(PlatformLayerKey targetItemKey, Action action) {
//		if (action == null) {
//			throw new IllegalArgumentException();
//		}
//
//		this.targetItemKey = targetItemKey;
//		this.action = action;
//	}
//
//	public PlatformLayerKey getTargetItemKey() {
//		return targetItemKey;
//	}
//
//	@Override
//	public String toString() {
//		return "JobKey [targetItemKey=" + targetItemKey + ", action=" + action + "]";
//	}
//
//	@Override
//	public int hashCode() {
//		throw new UnsupportedOperationException();
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		throw new UnsupportedOperationException();
//	}
//
// }