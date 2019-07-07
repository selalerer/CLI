package com.checkmarx.cxconsole.commands.job;

import com.checkmarx.cxconsole.clients.sast.CxRestSASTClient;
import com.checkmarx.cxconsole.clients.sast.CxRestSASTClientImpl;
import com.checkmarx.cxconsole.commands.job.exceptions.CLITokenJobException;
import com.checkmarx.cxconsole.parameters.CLIScanParametersSingleton;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import org.apache.log4j.Logger;

import java.util.concurrent.Callable;

/**
 * Created by nirli on 05/11/2017.
 */
abstract class CLITokenJob implements Callable<Integer> {

    protected CxRestSASTClient cxRestTokenClient;

    protected static Logger log = Logger.getLogger(CLITokenJob.class);

    protected CLIScanParametersSingleton params;

    CLITokenJob(CLIScanParametersSingleton params) {
        this.params = params;
        cxRestTokenClient = new CxRestSASTClientImpl(ConfigMgr.getRestWSMgr(this.params));
    }

    @Override
    public abstract Integer call() throws CLITokenJobException;

}
