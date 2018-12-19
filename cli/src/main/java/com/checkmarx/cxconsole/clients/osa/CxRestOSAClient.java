package com.checkmarx.cxconsole.clients.osa;

import com.checkmarx.cxconsole.clients.arm.dto.CxArmConfig;
import com.checkmarx.cxconsole.clients.general.CxRestClient;
import com.checkmarx.cxconsole.clients.osa.dto.CreateOSAScanRequest;
import com.checkmarx.cxconsole.clients.osa.dto.CreateOSAScanResponse;
import com.checkmarx.cxconsole.clients.osa.dto.OSAScanStatus;
import com.checkmarx.cxconsole.clients.osa.dto.OSASummaryResults;
import com.checkmarx.cxconsole.clients.osa.exceptions.CxRestOSAClientException;

/**
 * Created by nirli on 14/03/2018.
 */
public interface CxRestOSAClient extends CxRestClient {
    CreateOSAScanResponse createOSAScan(CreateOSAScanRequest osaScanRequest) throws CxRestOSAClientException;

    OSASummaryResults getOSAScanSummaryResults(String scanId) throws CxRestOSAClientException;

    void createOsaJson(String scanId, String filePath, OSASummaryResults osaSummaryResults) throws CxRestOSAClientException;

    void close();

    OSAScanStatus waitForOSAScanToFinish(String scanId, long scanTimeoutInMin, ScanWaitHandler<OSAScanStatus> waitHandler, boolean isAsyncOsaScan) throws CxRestOSAClientException;

    CxArmConfig getCxArmConfiguration() throws CxRestOSAClientException;
}
