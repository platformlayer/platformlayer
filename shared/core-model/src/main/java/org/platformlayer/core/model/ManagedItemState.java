package org.platformlayer.core.model;

import java.io.Serializable;

public enum ManagedItemState implements Serializable {

	/**
	 * Creation has been requested, but no action has yet been taken
	 */
	CREATION_REQUESTED(1),

	/**
	 * Build in progress
	 */
	BUILD(2),

	/**
	 * Build failed
	 */
	BUILD_ERROR(3),

	/**
	 * Build completed
	 * 
	 */
	ACTIVE(4),

	/**
	 * Request for deletion
	 */
	DELETE_REQUESTED(5),

	/**
	 * Object has been deleted
	 */
	DELETED(6);

	final int code;

	ManagedItemState(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static ManagedItemState fromCode(int code) {
		for (ManagedItemState state : ManagedItemState.values()) {
			if (state.code == code) {
				return state;
			}
		}

		throw new IllegalArgumentException("Cannot map code: " + code);
	}

}
