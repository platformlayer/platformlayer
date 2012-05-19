package org.platformlayer;

import org.platformlayer.ops.OpsException;

/**
 * Root exception for problems when calling an PlatformLayer API
 * 
 */
public class PlatformLayerClientException extends OpsException {
	private static final long serialVersionUID = 1L;
	private final String serviceFault;
	private final Integer httpResponseCode;

	public PlatformLayerClientException(String message, String serviceFault, int httpResponseCode) {
		super(message);
		this.serviceFault = serviceFault;
		this.httpResponseCode = httpResponseCode;
	}

	// public PlatformLayerClientException() {
	// this.serviceFault = null;
	// this.httpResponseCode = null;
	// }

	public PlatformLayerClientException(String message, Throwable cause) {
		super(message, cause);
		this.serviceFault = null;
		this.httpResponseCode = null;
	}

	public PlatformLayerClientException(String message, int httpResponseCode) {
		super(message);
		this.httpResponseCode = httpResponseCode;
		this.serviceFault = null;
	}

	public PlatformLayerClientException(String message) {
		super(message);
		this.httpResponseCode = null;
		this.serviceFault = null;
	}

	public PlatformLayerClientException(Throwable cause) {
		super(cause);
		this.httpResponseCode = null;
		this.serviceFault = null;
	}

	public Integer getHttpResponseCode() {
		return httpResponseCode;
	}

	public String getServiceFault() {
		return serviceFault;
	}
}
