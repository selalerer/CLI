package com.checkmarx.cxconsole.clients.osa.client;

import com.checkmarx.cxconsole.clients.login.CxRestLoginClient;
import com.checkmarx.cxconsole.clients.osa.ScanWaitHandler;
import com.checkmarx.cxconsole.clients.osa.client.exceptions.CxRestOSAClientException;
import com.checkmarx.cxconsole.clients.osa.dto.*;
import com.checkmarx.cxconsole.clients.utils.RestClientUtils;
import com.checkmarx.cxconsole.clientsold.rest.exceptions.CxRestClientException;
import com.checkmarx.cxconsole.clientsold.rest.exceptions.CxRestClientValidatorException;
import com.checkmarx.cxconsole.clientsold.rest.utils.RestHttpEntityBuilder;
import com.checkmarx.cxconsole.clientsold.rest.utils.RestResourcesURIBuilder;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.log4j.Logger;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static com.checkmarx.cxconsole.clients.utils.RestClientUtils.parseJsonFromResponse;
import static com.checkmarx.cxconsole.clients.utils.RestClientUtils.parseJsonListFromResponse;

/**
 * Created by: Dorg.
 * Date: 16/06/2016.
 */
public class CxRestOSAClient {

    private static Logger log = Logger.getLogger(CxRestOSAClient.class);

    private HttpClient apacheClient;
    private String hostName;
    private static int waitForScanToFinishRetry = ConfigMgr.getCfgMgr().getIntProperty(ConfigMgr.KEY_OSA_PROGRESS_INTERVAL);

    private static final String OSA_SUMMARY_NAME = "CxOSASummary";
    private static final String OSA_LIBRARIES_NAME = "CxOSALibraries";
    private static final String OSA_VULNERABILITIES_NAME = "CxOSAVulnerabilities";
    private static final String JSON_FILE = ".json";
    private ObjectMapper objectMapper = new ObjectMapper();

    public CxRestOSAClient(CxRestLoginClient restClient) {
        this.apacheClient = restClient.getApacheClient();
        this.hostName = restClient.getHostName();
    }

    public CreateOSAScanResponse createOSAScan(CreateOSAScanRequest osaScanRequest) throws CxRestOSAClientException {
        HttpPost post = null;
        HttpResponse response = null;

        try {
            post = new HttpPost(String.valueOf(RestResourcesURIBuilder.buildCreateOSAFSScanURL(new URL(hostName))));
            post.setEntity(RestHttpEntityBuilder.createOsaFSAEntity(osaScanRequest));

            //send scan request
            response = apacheClient.execute(post);
            //verify scan request
            RestClientUtils.validateTokenResponse(response, 201, "Fail to create OSA scan");

            //extract response as object and return the link
            return parseJsonFromResponse(response, CreateOSAScanResponse.class);
        } catch (IOException | CxRestClientException e) {
            log.error(e.getMessage());
            throw new CxRestOSAClientException(e.getMessage());
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
            HttpClientUtils.closeQuietly(response);
        }
    }

    public OSASummaryResults getOSAScanSummaryResults(String scanId) throws CxRestOSAClientException {
        HttpGet getRequest = null;
        HttpResponse response = null;

        try {
            getRequest = createHttpRequest(String.valueOf(RestResourcesURIBuilder.buildGetOSAScanSummaryResultsURL(new URL(hostName), scanId)), MediaType.APPLICATION_JSON);
            response = apacheClient.execute(getRequest);
            RestClientUtils.validateTokenResponse(response, 200, "fail get OSA scan summary results");

            return parseJsonFromResponse(response, OSASummaryResults.class);
        } catch (IOException | CxRestClientValidatorException e) {
            log.error("Failed to get OSA scan summary results: " + e.getMessage());
            throw new CxRestOSAClientException("Failed to get OSA scan summary results: " + e.getMessage());
        } finally {
            if (getRequest != null) {
                getRequest.releaseConnection();
            }
            HttpClientUtils.closeQuietly(response);
        }
    }

    public void createOsaJson(String scanId, String filePath, OSASummaryResults osaSummaryResults) throws CxRestOSAClientException {
        try {
            String specificFilePath = filePath.replace(JSON_FILE, "_" + OSA_SUMMARY_NAME + JSON_FILE);
            writeReport(osaSummaryResults, specificFilePath, "summary json");

            List<Library> libraries = getOSALibraries(scanId);
            specificFilePath = filePath.replace(JSON_FILE, "_" + OSA_LIBRARIES_NAME + JSON_FILE);
            writeReport(libraries, specificFilePath, "libraries json");

            List<CVE> osaVulnerabilities = getOSAVulnerabilities(scanId);
            specificFilePath = filePath.replace(JSON_FILE, "_" + OSA_VULNERABILITIES_NAME + JSON_FILE);
            writeReport(osaVulnerabilities, specificFilePath, "vulnerabilities json");
        } catch (IOException | CxRestClientValidatorException e) {
            throw new CxRestOSAClientException("Failed to create OSA JSON report: " + e.getMessage());
        }
    }

    public void close() {
        HttpClientUtils.closeQuietly(apacheClient);
    }

    private void writeReport(Object data, String filePath, String toLog) throws IOException {
        File file = new File(filePath);
        String s = FilenameUtils.getExtension(filePath);
        if (s.equals("json")) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
        } else {
            log.error("OSA " + toLog + " location is invalid");
            return;
        }
        log.info("OSA " + toLog + " location: " + file.getAbsolutePath());
    }

    private List<Library> getOSALibraries(String scanId) throws IOException, CxRestClientValidatorException {
        HttpGet getRequest = createHttpRequest(String.valueOf(RestResourcesURIBuilder.buildGetOSAScanLibrariesResultsURL(new URL(hostName), scanId)), MediaType.APPLICATION_JSON);
        HttpResponse response = null;
        try {
            response = apacheClient.execute(getRequest);
            RestClientUtils.validateTokenResponse(response, 200, "Failed to get OSA libraries");

            return parseJsonListFromResponse(response, TypeFactory.defaultInstance().constructCollectionType(List.class, Library.class));
        } finally {
            getRequest.releaseConnection();
            HttpClientUtils.closeQuietly(response);
        }
    }

    private List<CVE> getOSAVulnerabilities(String scanId) throws IOException, CxRestClientValidatorException {
        HttpGet getRequest = createHttpRequest(String.valueOf(RestResourcesURIBuilder.buildGetOSAScanVulnerabilitiesResultsURL(new URL(hostName), scanId)), MediaType.APPLICATION_JSON);
        HttpResponse response = null;
        try {
            response = apacheClient.execute(getRequest);
            RestClientUtils.validateTokenResponse(response, 200, "Failed to get OSA vulnerabilities");

            return parseJsonListFromResponse(response, TypeFactory.defaultInstance().constructCollectionType(List.class, CVE.class));
        } finally {
            getRequest.releaseConnection();
            HttpClientUtils.closeQuietly(response);
        }
    }

    private HttpGet createHttpRequest(String path, String mediaType) {
        HttpGet getRequest = new HttpGet(path);
        getRequest.setHeader("Accept", mediaType);
        return getRequest;
    }

    private OSAScanStatus getOSAScanStatus(String scanId) throws CxRestOSAClientException, CxRestClientValidatorException {
        HttpResponse response = null;
        HttpGet getRequest = null;

        try {
            getRequest = new HttpGet(String.valueOf(RestResourcesURIBuilder.buildGetOSAScanStatusURL(new URL(hostName), scanId)));
            response = apacheClient.execute(getRequest);
            RestClientUtils.validateTokenResponse(response, 200, "Failed to get OSA scan status");

            return parseJsonFromResponse(response, OSAScanStatus.class);
        } catch (IOException e) {
            throw new CxRestOSAClientException("Failed to get OSA scan status: " + e.getMessage());
        } finally {
            if (getRequest != null) {
                getRequest.releaseConnection();
            }
            HttpClientUtils.closeQuietly(response);
        }
    }

    public OSAScanStatus waitForOSAScanToFinish(String scanId, long scanTimeoutInMin, ScanWaitHandler<OSAScanStatus> waitHandler, boolean isAsyncOsaScan) throws CxRestOSAClientException {
        long timeToStop = (System.currentTimeMillis() / 60000) + scanTimeoutInMin;
        long startTime = System.currentTimeMillis();
        OSAScanStatus scanStatus = null;
        OSAScanStatusEnum status = null;
        waitHandler.onStart(startTime, scanTimeoutInMin);
        int retry = waitForScanToFinishRetry;
        while (scanTimeoutInMin <= 0 || (System.currentTimeMillis() / 60000) <= timeToStop) {
            if (!isAsyncOsaScan) {
                try {
                    Thread.sleep(10000); //Get status every 10 sec
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            try {
                scanStatus = getOSAScanStatus(scanId);
            } catch (Exception e) {
                retry = checkRetry(retry, e.getMessage());
                continue;
            }

            retry = waitForScanToFinishRetry;

            status = scanStatus.getStatus();
            if (OSAScanStatusEnum.FAILED.equals(status)) {
                waitHandler.onFail(scanStatus);
                throw new CxRestOSAClientException("OSA scan cannot be completed. status: [" + status.uiValue() + "]. message: [" + StringUtils.defaultString(scanStatus.getMessage()) + "]");
            }
            if (isAsyncOsaScan && (OSAScanStatusEnum.QUEUED.equals(status) || OSAScanStatusEnum.IN_PROGRESS.equals(status))) {
                waitHandler.onQueued(scanStatus);
                return scanStatus;
            }
            if (OSAScanStatusEnum.FINISHED.equals(status)) {
                waitHandler.onSuccess(scanStatus);
                return scanStatus;
            }
            waitHandler.onIdle(scanStatus);
        }

        if (!OSAScanStatusEnum.FINISHED.equals(status)) {
            waitHandler.onTimeout(scanStatus);
            log.error("OSA scan has reached the time limit. (" + scanTimeoutInMin + " minutes).");
            throw new CxRestOSAClientException("OSA scan has reached the time limit. (" + scanTimeoutInMin + " minutes).");
        }

        return scanStatus;
    }

    private int checkRetry(int retry, String errorMessage) throws CxRestOSAClientException {
        log.debug("fail to get status from scan. retrying (" + (retry - 1) + " tries left). error message: " + errorMessage);
        retry--;
        if (retry <= 0) {
            log.error("fail to get status from scan. error message: " + errorMessage);
            throw new CxRestOSAClientException("fail to get status from scan. error message: " + errorMessage);
        }

        return retry;
    }
}