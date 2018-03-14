package com.checkmarx.cxconsole.clients.osa;

import com.checkmarx.cxconsole.clients.exception.CxRestClientException;
import com.checkmarx.cxconsole.clients.exception.CxValidateResponseException;
import com.checkmarx.cxconsole.clients.login.CxRestLoginClient;
import com.checkmarx.cxconsole.clients.osa.dto.*;
import com.checkmarx.cxconsole.clients.osa.exceptions.CxRestOSAClientException;
import com.checkmarx.cxconsole.clients.osa.utils.OsaHttpEntityBuilder;
import com.checkmarx.cxconsole.clients.osa.utils.OsaResourcesURIBuilder;
import com.checkmarx.cxconsole.clients.utils.RestClientUtils;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;

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
public class CxRestOSAClientImpl implements CxRestOSAClient {

    private static Logger log = Logger.getLogger(CxRestOSAClientImpl.class);

    private HttpClient apacheClient;
    private String hostName;
    private static final Header CLI_CONTENT_TYPE_AND_VERSION_HEADER = new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON + ";v=1.0");
    private static final Header CLI_ACCEPT_HEADER = new BasicHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON + ";v=1.0");

    private static int waitForScanToFinishRetry = ConfigMgr.getCfgMgr().getIntProperty(ConfigMgr.KEY_OSA_PROGRESS_INTERVAL);

    private static final String OSA_SUMMARY_NAME = "CxOSASummary";
    private static final String OSA_LIBRARIES_NAME = "CxOSALibraries";
    private static final String OSA_VULNERABILITIES_NAME = "CxOSAVulnerabilities";
    private static final String JSON_FILE = ".json";
    private ObjectMapper objectMapper = new ObjectMapper();

    public CxRestOSAClientImpl(CxRestLoginClient restClient) {
        this.apacheClient = restClient.getClient();
        this.hostName = restClient.getHostName();
    }

    @Override
    public CreateOSAScanResponse createOSAScan(CreateOSAScanRequest osaScanRequest) throws CxRestOSAClientException {
        HttpUriRequest post;
        HttpResponse response = null;

        try {
            post = RequestBuilder.post()
                    .setUri(String.valueOf(OsaResourcesURIBuilder.buildCreateOSAFSScanURL(new URL(hostName))))
                    .setEntity(OsaHttpEntityBuilder.createOsaFSAEntity(osaScanRequest))
                    .setHeader(CLI_CONTENT_TYPE_AND_VERSION_HEADER)
                    .build();

            response = apacheClient.execute(post);

            RestClientUtils.validateClientResponse(response, 201, "Fail to create OSA scan");
            return parseJsonFromResponse(response, CreateOSAScanResponse.class);
        } catch (IOException | CxRestClientException e) {
            log.error(e.getMessage());
            throw new CxRestOSAClientException(e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public OSASummaryResults getOSAScanSummaryResults(String scanId) throws CxRestOSAClientException {
        HttpUriRequest getRequest = null;
        HttpResponse response = null;

        try {
            getRequest = RequestBuilder.get()
                    .setUri(String.valueOf(OsaResourcesURIBuilder.buildGetOSAScanSummaryResultsURL(new URL(hostName), scanId)))
                    .setHeader(CLI_CONTENT_TYPE_AND_VERSION_HEADER)
                    .build();

            response = apacheClient.execute(getRequest);
            RestClientUtils.validateClientResponse(response, 200, "fail get OSA scan summary results");

            return parseJsonFromResponse(response, OSASummaryResults.class);
        } catch (IOException | CxValidateResponseException e) {
            log.error("Failed to get OSA scan summary results: " + e.getMessage());
            throw new CxRestOSAClientException("Failed to get OSA scan summary results: " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
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
        } catch (IOException e) {
            log.error("Failed to create OSA JSON report: " + e.getMessage());
            throw new CxRestOSAClientException("Failed to create OSA JSON report: " + e.getMessage());
        }
    }

    @Override
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

    private List<Library> getOSALibraries(String scanId) throws CxRestOSAClientException {
        HttpResponse response = null;
        HttpUriRequest getRequest;

        try {
            getRequest = RequestBuilder.get()
                    .setUri((String.valueOf(OsaResourcesURIBuilder.buildGetOSAScanLibrariesResultsURL(new URL(hostName), scanId))))
                    .setHeader(CLI_CONTENT_TYPE_AND_VERSION_HEADER)
                    .build();
            response = apacheClient.execute(getRequest);

            RestClientUtils.validateClientResponse(response, 200, "Failed to get OSA libraries");
            return parseJsonListFromResponse(response, TypeFactory.defaultInstance().constructCollectionType(List.class, Library.class));
        } catch (IOException | CxValidateResponseException e) {
            log.error("Failed to get OSA libraries: " + e.getMessage());
            throw new CxRestOSAClientException("Failed to get OSA libraries: " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    private List<CVE> getOSAVulnerabilities(String scanId) throws CxRestOSAClientException {
        HttpResponse response = null;
        HttpUriRequest getRequest = null;

        try {
            getRequest = RequestBuilder.get()
                    .setUri(String.valueOf(OsaResourcesURIBuilder.buildGetOSAScanVulnerabilitiesResultsURL(new URL(hostName), scanId)))
                    .setHeader(CLI_CONTENT_TYPE_AND_VERSION_HEADER)
                    .build();
            response = apacheClient.execute(getRequest);

            RestClientUtils.validateClientResponse(response, 200, "Failed to get OSA vulnerabilities");
            return parseJsonListFromResponse(response, TypeFactory.defaultInstance().constructCollectionType(List.class, CVE.class));
        } catch (IOException | CxValidateResponseException e) {
            log.error("Failed to get OSA vulnerabilities: " + e.getMessage());
            throw new CxRestOSAClientException("Failed to get OSA vulnerabilities: " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    private OSAScanStatus getOSAScanStatus(String scanId) throws CxRestOSAClientException {
        HttpResponse response = null;
        HttpUriRequest getRequest = null;

        try {
            getRequest = RequestBuilder.get()
                    .setUri(String.valueOf(OsaResourcesURIBuilder.buildGetOSAScanStatusURL(new URL(hostName), scanId)))
                    .setHeader(CLI_CONTENT_TYPE_AND_VERSION_HEADER)
                    .build();
            response = apacheClient.execute(getRequest);

            RestClientUtils.validateClientResponse(response, 200, "Failed to get OSA scan status");
            return parseJsonFromResponse(response, OSAScanStatus.class);
        } catch (IOException | CxValidateResponseException e) {
            log.error("Failed to get OSA scan status: " + e.getMessage());
            throw new CxRestOSAClientException("Failed to get OSA scan status: " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
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