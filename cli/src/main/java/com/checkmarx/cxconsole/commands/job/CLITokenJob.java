package com.checkmarx.cxconsole.commands.job;

import com.checkmarx.cxconsole.clients.token.CxRestTokenClient;
import com.checkmarx.cxconsole.clients.token.CxRestTokenClientImpl;
import com.checkmarx.cxconsole.commands.job.exceptions.CLITokenJobException;
import com.checkmarx.cxconsole.parameters.CLIScanParametersSingleton;
import org.apache.log4j.Logger;

import java.util.concurrent.Callable;

/**
 * Created by nirli on 05/11/2017.
 */
abstract class CLITokenJob implements Callable<Integer> {

    protected CxRestTokenClient cxRestTokenClient;

    protected static Logger log = Logger.getLogger(CLITokenJob.class);

    protected CLIScanParametersSingleton params;

    CLITokenJob(CLIScanParametersSingleton params) {
        this.params = params;
        cxRestTokenClient = new CxRestTokenClientImpl();
    }

    @Override
    public abstract Integer call() throws CLITokenJobException;

}
