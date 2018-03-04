package com.checkmarx.cxconsole.clientsold.soap.login;

import com.checkmarx.cxconsole.clientsold.soap.exceptions.CxSoapClientValidatorException;
import com.checkmarx.cxconsole.clientsold.soap.login.exceptions.CxSoapLoginClientException;
import com.checkmarx.cxconsole.commands.utils.CommandUtils;
import com.checkmarx.cxconsole.utils.CXFConfigurationUtils;
import com.checkmarx.cxconsole.utils.DynamicAuthSupplier;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.cxviewer.ws.generated.Credentials;
import com.checkmarx.cxviewer.ws.generated.CxCLIWebServiceV1;
import com.checkmarx.cxviewer.ws.generated.CxCLIWebServiceV1Soap;
import com.checkmarx.cxviewer.ws.generated.CxWSResponseLoginData;
import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by nirli on 26/10/2017.
 */
public class CxSoapLoginClient {

    private Logger log = Logger.getLogger(CxSoapLoginClient.class);

    private CxCLIWebServiceV1Soap cxSoapClient;

    private String sessionId;

    public void initSoapClient(URL wsdlLocation) throws CxSoapLoginClientException {
        try {
            final URL wsdlLocationWithWSDL = new URL(wsdlLocation.toString() + "?WSDL");
            CxCLIWebServiceV1 ws = new CxCLIWebServiceV1(wsdlLocationWithWSDL);
            cxSoapClient = ws.getCxCLIWebServiceV1Soap();

            CXFConfigurationUtils.disableSchemaValidation(cxSoapClient);

            if ("false".equalsIgnoreCase(ConfigMgr.getCfgMgr().getProperty(ConfigMgr.KEY_USE_KERBEROS_AUTH))) {
                CXFConfigurationUtils.setNTLMAuthentication(cxSoapClient);
            }

            if ("true".equalsIgnoreCase(ConfigMgr.getCfgMgr().getProperty(ConfigMgr.KEY_USE_KERBEROS_AUTH))) {
                initKerberos();
            }

            log.trace("SOAP client was initiated successfully");
        } catch (MalformedURLException e) {
            log.fatal("Error initiate the SOAP client: " + e.getMessage());
            throw new CxSoapLoginClientException("Error initiate the SOAP client: " + e.getMessage());
        }
    }

    public CxWSResponseLoginData login(String userName, String password) throws CxSoapLoginClientException {
        Credentials credentials = new Credentials();
        credentials.setUser(userName);
        credentials.setPass(password);

        CxWSResponseLoginData response = cxSoapClient.login(credentials, 1033);
        try {
            CommandUtils.validateResponse(response);
        } catch (CxSoapClientValidatorException e) {
            log.fatal("Failed to login :" + response.getErrorMessage());
            throw new CxSoapLoginClientException("Failed to login: " + response.getErrorMessage());
        }

        sessionId = response.getSessionId();
        return response;
    }

    public CxWSResponseLoginData ssoLogin(String userName, String sid) throws CxSoapLoginClientException {
        Credentials credentials = new Credentials();
        credentials.setUser(userName);
        credentials.setPass(sid);

        CxWSResponseLoginData response = cxSoapClient.ssoLogin(credentials, 1033);
        try {
            CommandUtils.validateResponse(response);
        } catch (CxSoapClientValidatorException e) {
            log.fatal("Failed to login :" + response.getErrorMessage());
            throw new CxSoapLoginClientException("Failed to login: " + response.getErrorMessage());
        }

        sessionId = response.getSessionId();
        return response;
    }

    public CxCLIWebServiceV1Soap getCxSoapClient() {
        return cxSoapClient;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    private static void initKerberos() {
        final boolean isUsingKerberos = "true".equalsIgnoreCase(ConfigMgr.getCfgMgr().getProperty(ConfigMgr.KEY_USE_KERBEROS_AUTH));
        if (isUsingKerberos) {
            System.setProperty("java.security.auth.login.config", System.class.getResource("/login.conf").toString());
            System.setProperty("java.security.krb5.conf", System.getProperty("user.dir") + "/config/krb5.conf");
            System.setProperty("auth.spnego.requireCredDelegation", "true");

            final String username = ConfigMgr.getCfgMgr().getProperty(ConfigMgr.KEY_KERBEROS_USERNAME);
            System.setProperty("cxf.kerberos.username", username);
            final String password = ConfigMgr.getCfgMgr().getProperty("kerberos.password");
            System.setProperty("cxf.kerberos.password", password);

        }
        DynamicAuthSupplier.setKerberosActive(isUsingKerberos);
    }
}