package com.checkmarx.cxconsole.clients.general.exception;

import com.checkmarx.cxconsole.clients.exception.CxRestClientException;

/**
 * Created by nirli on 26/02/2018.
 */
public class CxScanPrerequisitesValidatorException extends CxRestClientException {

    public CxScanPrerequisitesValidatorException() {
    }

    public CxScanPrerequisitesValidatorException(String message) {
        super(message);
    }

    public CxScanPrerequisitesValidatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public CxScanPrerequisitesValidatorException(Throwable cause) {
        super(cause);
    }

    public CxScanPrerequisitesValidatorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
