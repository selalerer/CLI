package com.checkmarx.cxconsole.clients.sast.exceptions;

import com.checkmarx.cxconsole.clients.exception.CxRestClientException;

/**
 * Created by nirli on 25/02/2018.
 */
public class CxRestSASTClientException extends CxRestClientException {

    public CxRestSASTClientException() {
    }

    public CxRestSASTClientException(String message) {
        super(message);
    }

    public CxRestSASTClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public CxRestSASTClientException(Throwable cause) {
        super(cause);
    }

    public CxRestSASTClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
