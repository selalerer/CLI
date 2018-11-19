package com.checkmarx.cxconsole.commands.job;

import com.checkmarx.cxconsole.clients.general.CxRestGeneralClient;
import com.checkmarx.cxconsole.clients.general.CxRestGeneralClientImpl;
import com.checkmarx.cxconsole.clients.login.CxRestLoginClient;
import com.checkmarx.cxconsole.commands.job.exceptions.CLIJobException;
import com.checkmarx.cxconsole.commands.job.retriableoperation.RetryableOperation;
import com.checkmarx.cxconsole.commands.job.retriableoperation.RetryableRESTLogin;
import com.checkmarx.cxconsole.parameters.CLIScanParametersSingleton;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import org.apache.log4j.Logger;

import java.util.concurrent.Callable;

/**
 * Created by nirli on 05/11/2017.
 */
public abstract class CLIScanJob implements Callable<Integer> {

    protected static Logger log = Logger.getLogger(CLIScanJob.class);

    static CxRestLoginClient cxRestLoginClient;
    static CxRestGeneralClient cxRestGeneralClient;
    boolean isAsyncScan;

    private String errorMsg;
    protected CLIScanParametersSingleton params;

    CLIScanJob(CLIScanParametersSingleton params, boolean isAsyncScan) {
        this.params = params;
        this.isAsyncScan = isAsyncScan;
        cxRestLoginClient = ConfigMgr.getRestWSMgr(this.params);
    }

    protected void login() throws CLIJobException {
        final RetryableOperation login = new RetryableRESTLogin(params, cxRestLoginClient);
        login.run();
        cxRestGeneralClient = new CxRestGeneralClientImpl(cxRestLoginClient);
    }

    @Override
    public abstract Integer call() throws CLIJobException;

    String getErrorMsg() {
        return errorMsg;
    }

}