package com.centurylink.cloud.sdk.core.client.errors;

import com.centurylink.cloud.sdk.core.exceptions.ClcException;

/**
 * Base class for all client layer exceptions
 *
 * @author ilya.drabenia
 */
public class ClcClientException extends ClcException {

    public ClcClientException() {
    }

    public ClcClientException(String format, Object... arguments) {
        super(format, arguments);
    }

    public ClcClientException(String message) {
        super(message);
    }

    public ClcClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClcClientException(Throwable cause) {
        super(cause);
    }

}
