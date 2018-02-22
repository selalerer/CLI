package com.checkmarx.cxconsole.clients.sast.client;

import com.checkmarx.cxconsole.clients.login.CxRestLoginClient;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;

/**
 * Created by nirli on 21/02/2018.
 */
public class CxRestSASTClient {

    private static Logger log = Logger.getLogger(CxRestSASTClient.class);

    private HttpClient apacheClient;
    private String hostName;
    private static int waitForScanToFinishRetry = ConfigMgr.getCfgMgr().getIntProperty(ConfigMgr.KEY_PROGRESS_INTERVAL);

    public CxRestSASTClient(CxRestLoginClient restClient) {
        this.apacheClient = restClient.getApacheClient();
        this.hostName = restClient.getHostName();
    }

    public HttpClient getApacheClient() {
        return apacheClient;
    }

    public String getHostName() {
        return hostName;
    }
}
