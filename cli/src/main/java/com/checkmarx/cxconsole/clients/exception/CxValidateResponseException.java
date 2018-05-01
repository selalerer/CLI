package com.checkmarx.cxconsole.clients.exception;

/**
 * Created by nirli on 27/02/2018.
 */
public class CxValidateResponseException extends CxRestClientException {

    public CxValidateResponseException() {
    }

    public CxValidateResponseException(String message) {
        super(message);
    }

    public CxValidateResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public CxValidateResponseException(Throwable cause) {
        super(cause);
    }

    public CxValidateResponseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
