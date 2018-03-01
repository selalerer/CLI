package com.checkmarx.cxconsole.commands.job.retriableoperation;

import com.checkmarx.cxconsole.clients.login.CxRestLoginClient;
import com.checkmarx.cxconsole.clients.login.exceptions.CxRestLoginClientException;
import com.checkmarx.cxconsole.commands.job.exceptions.CLIJobException;
import com.checkmarx.cxconsole.parameters.CLIScanParametersSingleton;
import org.apache.log4j.Logger;

/**
 * Created by nirli on 06/11/2017.
 */
public class RetryableRESTLogin extends RetryableOperation {

    private static Logger log = Logger.getLogger(RetryableRESTLogin.class);

    private CxRestLoginClient cxRestLoginClient;
    private CLIScanParametersSingleton params;

    public RetryableRESTLogin(CLIScanParametersSingleton parameters, CxRestLoginClient cxRestLoginClient) {
        this.cxRestLoginClient = cxRestLoginClient;
        this.params = parameters;
    }

    @Override
    protected void operation() throws CLIJobException {
        log.info("Logging into Checkmarx server.");

        // Login
        try {
            if (params.getCliMandatoryParameters().isHasUserParam() && params.getCliMandatoryParameters().isHasPasswordParam()) {
                cxRestLoginClient.credentialsLogin();
            } else if (params.getCliMandatoryParameters().isHasTokenParam()) {
                cxRestLoginClient.tokenLogin();
            }

            if (cxRestLoginClient.getApacheClient() == null) {
                throw new CLIJobException("Unsuccessful login.");
            }
        } catch (CxRestLoginClientException e) {
            throw new CLIJobException("Unsuccessful login.");
        }

        log.info("Login was completed successfully");
        finished = true;
    }

    @Override
    public String getOperationName() {
        return "Login";
    }
}
