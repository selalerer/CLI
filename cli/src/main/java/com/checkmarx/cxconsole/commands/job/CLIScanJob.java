package com.checkmarx.cxconsole.commands.job;

import com.checkmarx.cxconsole.clients.general.CxRestGeneralClient;
import com.checkmarx.cxconsole.clients.general.CxRestGeneralClientImpl;
import com.checkmarx.cxconsole.clients.login.CxRestLoginClient;
import com.checkmarx.cxconsole.commands.job.exceptions.CLIJobException;
import com.checkmarx.cxconsole.commands.job.retriableoperation.RetryableOperation;
import com.checkmarx.cxconsole.commands.job.retriableoperation.RetryableRESTLogin;
import com.checkmarx.cxconsole.commands.job.utils.JobUtils;
import com.checkmarx.cxconsole.commands.job.utils.PathHandler;
import com.checkmarx.cxconsole.parameters.CLIScanParametersSingleton;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Created by nirli on 05/11/2017.
 */
public abstract class CLIScanJob implements Callable<Integer> {

    protected static Logger log = Logger.getLogger(CLIScanJob.class);

    CxRestLoginClient cxRestLoginClient;
    CxRestGeneralClient cxRestGeneralClient;
    boolean isAsyncScan;

    private String errorMsg;
    protected CLIScanParametersSingleton params;

    CLIScanJob(CLIScanParametersSingleton params, boolean isAsyncScan) {
        this.params = params;
        this.isAsyncScan = isAsyncScan;
        cxRestLoginClient = ConfigMgr.getRestWSMgr(this.params);
    }

    void login() throws CLIJobException {
        final RetryableOperation login = new RetryableRESTLogin(params, cxRestLoginClient);
        login.run();
        cxRestGeneralClient = new CxRestGeneralClientImpl(cxRestLoginClient);
    }

    void storeXMLResults(String fileName, byte[] resultBytes) throws CLIJobException {
        File resFile = initFile(fileName);
        try (FileOutputStream fOut = new FileOutputStream(resFile.getAbsolutePath())) {
            fOut.write(resultBytes);
        } catch (IOException e) {
            log.error("Saving xml results to file [" + resFile.getAbsolutePath() + "] failed");
            log.trace("", e);
        }
    }

    private File initFile(String fileName) throws CLIJobException {
        String folderPath = JobUtils.gerWorkDirectory(params);
        String resultFilePath = PathHandler.initFilePath(params.getCliMandatoryParameters().getProject().getName(), fileName, ".xml", folderPath);
        return new File(resultFilePath);
    }

    @Override
    public abstract Integer call() throws CLIJobException;

    String getErrorMsg() {
        return errorMsg;
    }

}