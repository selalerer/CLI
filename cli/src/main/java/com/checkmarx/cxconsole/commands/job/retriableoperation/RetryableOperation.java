package com.checkmarx.cxconsole.commands.job.retriableoperation;

import com.checkmarx.cxconsole.commands.job.exceptions.CLIJobException;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import org.apache.log4j.Logger;

/**
 * Created by nirli on 05/11/2017.
 */
public abstract class RetryableOperation {

    private static Logger log = Logger.getLogger(RetryableOperation.class);

    protected boolean finished = false;
    protected String error;

    RetryableOperation() {
    }

    public void run() throws CLIJobException {
        int retries = ConfigMgr.getCfgMgr().getIntProperty(ConfigMgr.KEY_RETIRES);
        int count = 0;
        while (!finished) {
            try {
                operation();
            } catch (Exception e) {
                if (count >= retries) {
                    String errorMsg = e.getMessage() + "\nIn addition, Make sure the installed plugin version is compatible with the CxSAST version according to CxSAST release notes.";
                    log.error(errorMsg);
                    throw new CLIJobException(errorMsg, e);
                }
                count++;
                log.trace("Error occurred during retryable operation", e);
                log.info(" Attempt #" + count + " - Error occurred during " + getOperationName());
            }
        }
    }

    protected abstract void operation() throws CLIJobException;

    public abstract String getOperationName();

    public String getError() {
        return error;
    }
}