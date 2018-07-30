package com.checkmarx.cxconsole.clients.arm.exceptions;

import com.checkmarx.cxconsole.clients.exception.CxRestClientException;

/**
 * Created by eyala on 7/12/2018.
 */
public class CxRestARMClientException extends CxRestClientException {

    public CxRestARMClientException() {
    }

    public CxRestARMClientException(String message) {
        super(message);
    }

    public CxRestARMClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public CxRestARMClientException(Throwable cause) {
        super(cause);
    }

    public CxRestARMClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
