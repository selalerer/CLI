package com.checkmarx.cxconsole.clients.general.exception;

import com.checkmarx.cxconsole.clients.exception.CxRestClientException;

/**
 * Created by nirli on 27/02/2018.
 */
public class CxRestGeneralClientException extends CxRestClientException {

    public CxRestGeneralClientException() {
    }

    public CxRestGeneralClientException(String message) {
        super(message);
    }

    public CxRestGeneralClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public CxRestGeneralClientException(Throwable cause) {
        super(cause);
    }

    public CxRestGeneralClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
