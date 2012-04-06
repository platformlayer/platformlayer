package org.platformlayer.exceptions;

import org.platformlayer.PlatformLayerClientException;

public class OpenstackClientConnectionException extends PlatformLayerClientException {
    private static final long serialVersionUID = 1L;

    public OpenstackClientConnectionException() {
        super();
    }

    public OpenstackClientConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public OpenstackClientConnectionException(String message) {
        super(message);
    }

    public OpenstackClientConnectionException(Throwable cause) {
        super(cause);
    }

}
