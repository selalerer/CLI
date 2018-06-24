package com.checkmarx.cxconsole.clients.sast;

import com.checkmarx.cxconsole.clients.exception.CxValidateResponseException;
import com.checkmarx.cxconsole.clients.login.CxRestLoginClient;
import com.checkmarx.cxconsole.clients.sast.constants.RemoteSourceType;
import com.checkmarx.cxconsole.clients.sast.constants.ReportStatusValue;
import com.checkmarx.cxconsole.clients.sast.constants.ReportType;
import com.checkmarx.cxconsole.clients.sast.dto.*;
import com.checkmarx.cxconsole.clients.sast.exceptions.CxRestSASTClientException;
import com.checkmarx.cxconsole.clients.sast.utils.SastHttpEntityBuilder;
import com.checkmarx.cxconsole.clients.sast.utils.SastResourceURIBuilder;
import com.checkmarx.cxconsole.clients.utils.RestClientUtils;
import com.checkmarx.cxconsole.commands.utils.FilesUtils;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static com.checkmarx.cxconsole.clients.utils.RestClientUtils.parseJsonListFromResponse;

/**
 * Created by nirli on 21/02/2018.
 */
public class CxRestSASTClientImpl<T extends RemoteSourceScanSettingDTO> implements CxRestSASTClient<T> {

    private static Logger log = Logger.getLogger(CxRestSASTClientImpl.class);

    private HttpClient apacheClient;
    private String hostName;
    private static final Header CLI_CONTENT_TYPE_AND_VERSION_HEADER = new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType() + ";v=1.0");
    private static final Header CLI_ACCEPT_HEADER_AND_VERSION_HEADER = new BasicHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType() + ";v=1.0");

    public CxRestSASTClientImpl(CxRestLoginClient restClient) {
        this.apacheClient = restClient.getClient();
        this.hostName = restClient.getHostName();
    }

    @Override
    public List<PresetDTO> getSastPresets() throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpUriRequest getRequest;

        try {
            getRequest = RequestBuilder.get()
                    .setUri(String.valueOf(SastResourceURIBuilder.buildGetSastPresetsURL(new URL(hostName))))
                    .setHeader(CLI_ACCEPT_HEADER_AND_VERSION_HEADER)
                    .build();
            response = apacheClient.execute(getRequest);

            RestClientUtils.validateClientResponse(response, 200, "Failed to get presets");
            return parseJsonListFromResponse(response, TypeFactory.defaultInstance().constructCollectionType(List.class, PresetDTO.class));
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to get presets: " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public List<EngineConfigurationDTO> getEngineConfiguration() throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpUriRequest getRequest;

        try {
            getRequest = RequestBuilder.get()
                    .setUri(String.valueOf(SastResourceURIBuilder.buildGetEngineConfigurationURL(new URL(hostName))))
                    .setHeader(CLI_ACCEPT_HEADER_AND_VERSION_HEADER)
                    .build();
            response = apacheClient.execute(getRequest);

            RestClientUtils.validateClientResponse(response, 200, "Failed to get engine configuration");
            return parseJsonListFromResponse(response, TypeFactory.defaultInstance().constructCollectionType(List.class, EngineConfigurationDTO.class));
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to get engine configuration: " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public ScanSettingDTO getProjectScanSetting(int id) throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpUriRequest getRequest;

        try {
            getRequest = RequestBuilder.get()
                    .setUri(String.valueOf(SastResourceURIBuilder.buildGetSASTScanSettingURL(new URL(hostName), id)))
                    .setHeader(CLI_ACCEPT_HEADER_AND_VERSION_HEADER)
                    .build();
            response = apacheClient.execute(getRequest);

            RestClientUtils.validateClientResponse(response, 200, "Failed to get SAST scan setting");
            return RestClientUtils.parseScanSettingResponse(response);
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to get SAST scan setting: " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public void createProjectScanSetting(ScanSettingDTO scanSetting) throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpUriRequest postRequest;

        try {
            postRequest = RequestBuilder.post()
                    .setUri(String.valueOf(SastResourceURIBuilder.buildSASTScanSettingURL(new URL(hostName))))
                    .setHeader(CLI_CONTENT_TYPE_AND_VERSION_HEADER)
                    .setEntity(SastHttpEntityBuilder.createScanSettingEntity(scanSetting))
                    .build();
            response = apacheClient.execute(postRequest);

            RestClientUtils.validateClientResponse(response, 200, "Failed to create scan settings");
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to create scan settings: " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public void updateProjectScanSetting(ScanSettingDTO scanSetting) throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpUriRequest putRequest;

        try {
            putRequest = RequestBuilder.put()
                    .setUri(String.valueOf(SastResourceURIBuilder.buildSASTScanSettingURL(new URL(hostName))))
                    .setHeader(CLI_CONTENT_TYPE_AND_VERSION_HEADER)
                    .setEntity(SastHttpEntityBuilder.createScanSettingEntity(scanSetting))
                    .build();

            response = apacheClient.execute(putRequest);
            RestClientUtils.validateClientResponse(response, 200, "Failed to update scan settings");
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to update scan settings: " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public int createNewSastScan(int projectId, boolean forceScan, boolean incrementalScan, boolean visibleOthers) throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpUriRequest postRequest;

        try {
            postRequest = RequestBuilder.post()
                    .setUri(String.valueOf(SastResourceURIBuilder.buildCreateNewSastScanURL(new URL(hostName))))
                    .setHeader(CLI_CONTENT_TYPE_AND_VERSION_HEADER)
                    .setEntity(SastHttpEntityBuilder.createNewSastScanEntity(projectId, forceScan, incrementalScan, visibleOthers))
                    .build();
            response = apacheClient.execute(postRequest);

            RestClientUtils.validateClientResponse(response, 201, "Failed to create new SAST scan");
            JSONObject jsonResponse = RestClientUtils.parseJsonObjectFromResponse(response);
            return jsonResponse.getInt("id");
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to create new SAST scan: " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public void updateScanExclusions(int projectId, String[] excludeFoldersPattern, String[] excludeFilesPattern) throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpUriRequest putRequest;

        try {
            putRequest = RequestBuilder.put()
                    .setUri(String.valueOf(SastResourceURIBuilder.buildSASTScanExclusionSettingURL(new URL(hostName), projectId)))
                    .setHeader(CLI_CONTENT_TYPE_AND_VERSION_HEADER)
                    .setEntity(SastHttpEntityBuilder.createScanExclusionSettingEntity(StringUtils.join(excludeFoldersPattern, ","),
                            StringUtils.join(excludeFilesPattern, ",")))
                    .build();
            response = apacheClient.execute(putRequest);

            RestClientUtils.validateClientResponse(response, 200, "Failed to update scan exclusions settings");
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to update scan exclusions settings: " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public void updateScanComment(long scanId, String comment) throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpUriRequest patchRequest;

        try {
            patchRequest = RequestBuilder.patch()
                    .setUri(String.valueOf(SastResourceURIBuilder.buildGetSASTScanResourceURL(new URL(hostName), scanId)))
                    .setHeader(CLI_CONTENT_TYPE_AND_VERSION_HEADER)
                    .setEntity(SastHttpEntityBuilder.patchSastCommentEntity(comment))
                    .build();
            response = apacheClient.execute(patchRequest);

            RestClientUtils.validateClientResponse(response, 204, "Failed to add comment to SAST scan (id " + scanId + ")");
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to add comment to SAST scan (id " + scanId + "): " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public void uploadZipFileForSASTScan(int projectId, byte[] zipFile) throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpUriRequest postRequest;

        try {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("zippedSource", zipFile, ContentType.APPLICATION_OCTET_STREAM, null);
            HttpEntity multipart = builder.build();
            postRequest = RequestBuilder.post()
                    .setUri(String.valueOf(SastResourceURIBuilder.buildUploadZipFileURL(new URL(hostName), projectId)))
                    .setEntity(multipart)
                    .build();
            log.info("Uploading zipped source files to server, please wait.");
            response = apacheClient.execute(postRequest);

            RestClientUtils.validateClientResponse(response, 204, "Failed to upload zip file for SAST scan");
            log.info("Zipped source files were uploaded successfully");
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to upload zip file for SAST scan: " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public ScanQueueDTO getScanQueueResponse(long scanId) throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpUriRequest getRequest;

        try {
            getRequest = RequestBuilder.get()
                    .setUri(String.valueOf(SastResourceURIBuilder.buildGetSASTScanQueueResponseURL(new URL(hostName), scanId)))
                    .setHeader(CLI_ACCEPT_HEADER_AND_VERSION_HEADER)
                    .build();
            response = apacheClient.execute(getRequest);

            RestClientUtils.validateClientResponse(response, 200, "Failed to get SAST scan queue response");
            return RestClientUtils.parseJsonFromResponse(response, ScanQueueDTO.class);
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to get SAST scan queue response: " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public ResultsStatisticsDTO getScanResults(long scanId) throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpUriRequest getRequest;

        try {
            getRequest = RequestBuilder.get()
                    .setUri(String.valueOf(SastResourceURIBuilder.buildGetSASTScanResultsURL(new URL(hostName), scanId)))
                    .setHeader(CLI_ACCEPT_HEADER_AND_VERSION_HEADER)
                    .build();
            response = apacheClient.execute(getRequest);

            RestClientUtils.validateClientResponse(response, 200, "Failed to get SAST scan results");
            return RestClientUtils.parseJsonFromResponse(response, ResultsStatisticsDTO.class);
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to get SAST scan results: " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    public void createRemoteSourceScan(int projectId, T remoteSourceScanSettingDTO, RemoteSourceType remoteSourceType) throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpUriRequest postRequest;

        try {
            if (remoteSourceScanSettingDTO instanceof SVNAndTFSScanSettingDTO && ((SVNAndTFSScanSettingDTO) remoteSourceScanSettingDTO).getPrivateKey() != null && ((SVNAndTFSScanSettingDTO) remoteSourceScanSettingDTO).getPrivateKey().length > 1) {
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.addBinaryBody("privateKey", ((SVNAndTFSScanSettingDTO) remoteSourceScanSettingDTO).getPrivateKey(), ContentType.APPLICATION_JSON, null);
                builder.addTextBody("absoluteUrl", ((SVNAndTFSScanSettingDTO) remoteSourceScanSettingDTO).getUri().getAbsoluteUrl(), ContentType.APPLICATION_JSON);
                builder.addTextBody("port", String.valueOf(((SVNAndTFSScanSettingDTO) remoteSourceScanSettingDTO).getUri().getPort()), ContentType.APPLICATION_JSON);
                builder.addTextBody("paths", StringUtils.join(remoteSourceScanSettingDTO.getPaths(), ";"), ContentType.APPLICATION_JSON);
                HttpEntity multipart = builder.build();
                postRequest = RequestBuilder.post()
                        .setUri(String.valueOf(SastResourceURIBuilder.buildCreateRemoteSourceScanURL(new URL(hostName), projectId, remoteSourceType, true)))
                        .setHeader(CLI_CONTENT_TYPE_AND_VERSION_HEADER)
                        .setEntity(multipart)
                        .build();
            } else {
                postRequest = RequestBuilder.post()
                        .setUri(String.valueOf(SastResourceURIBuilder.buildCreateRemoteSourceScanURL(new URL(hostName), projectId, remoteSourceType, false)))
                        .setHeader(CLI_CONTENT_TYPE_AND_VERSION_HEADER)
                        .setEntity(SastHttpEntityBuilder.createRemoteSourceEntity(remoteSourceScanSettingDTO))
                        .build();
            }
            response = apacheClient.execute(postRequest);

            RestClientUtils.validateClientResponse(response, 204, "Failed to create " + remoteSourceType.getUrlValue() + " remote source scan setting");
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to create " + remoteSourceType.getUrlValue() + " remote source scan setting: " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public void createGITScan(int projectId, String locationURL, String locationBranch, byte[] privateKey) throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpUriRequest postRequest;

        try {
            if (privateKey.length < 1) {
                postRequest = RequestBuilder.post()
                        .setUri(String.valueOf(SastResourceURIBuilder.buildCreateRemoteSourceScanURL(new URL(hostName), projectId, RemoteSourceType.GIT, false)))
                        .setHeader(CLI_CONTENT_TYPE_AND_VERSION_HEADER)
                        .setEntity(SastHttpEntityBuilder.createGITSourceEntity(locationURL, locationBranch))
                        .build();
            } else {
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.addTextBody("url", locationURL, ContentType.APPLICATION_JSON);
                builder.addTextBody("branch", locationBranch, ContentType.APPLICATION_JSON);
                builder.addBinaryBody("privateKey", privateKey, ContentType.APPLICATION_JSON, null);
                postRequest = RequestBuilder.post()
                        .setUri(String.valueOf(SastResourceURIBuilder.buildCreateRemoteSourceScanURL(new URL(hostName), projectId, RemoteSourceType.GIT, true)))
                        .setHeader(CLI_ACCEPT_HEADER_AND_VERSION_HEADER)
                        .setEntity(builder.build())
                        .build();
            }
            response = apacheClient.execute(postRequest);

            RestClientUtils.validateClientResponse(response, 204, "Failed to create " + RemoteSourceType.GIT.getUrlValue() + " remote source scan setting");
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to create " + RemoteSourceType.GIT.getUrlValue() + " remote source scan setting: " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public int createReport(long scanId, ReportType reportType) throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpUriRequest postRequest;

        try {
            postRequest = RequestBuilder.post()
                    .setUri(String.valueOf(SastResourceURIBuilder.buildCreateReportURL(new URL(hostName))))
                    .setHeader(CLI_CONTENT_TYPE_AND_VERSION_HEADER)
                    .setEntity(SastHttpEntityBuilder.createReportEntity(scanId, reportType))
                    .build();
            response = apacheClient.execute(postRequest);

            RestClientUtils.validateClientResponse(response, 202, "Failed to create " + reportType.getValue() + " report");
            JSONObject jsonResponse = RestClientUtils.parseJsonObjectFromResponse(response);
            return jsonResponse.getInt("reportId");
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to create " + reportType.getValue() + " report: " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public ReportStatusValue getReportStatus(int reportId) throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpUriRequest getRequest;

        try {
            getRequest = RequestBuilder.get()
                    .setUri(String.valueOf(SastResourceURIBuilder.buildGetReportStatusURL(new URL(hostName), reportId)))
                    .setHeader(CLI_ACCEPT_HEADER_AND_VERSION_HEADER)
                    .build();
            response = apacheClient.execute(getRequest);

            RestClientUtils.validateClientResponse(response, 200, "Failed to get report status");
            JSONObject jsonResponse = RestClientUtils.parseJsonObjectFromResponse(response);
            return ReportStatusValue.getServerValue(jsonResponse.getJSONObject("status").getString("value"));
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to get report status: " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public void createReportFile(int reportId, File reportFile) throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpUriRequest getRequest;

        try {
            getRequest = RequestBuilder.get()
                    .setUri(String.valueOf(SastResourceURIBuilder.buildGetReportFileURL(new URL(hostName), reportId)))
                    .setHeader(CLI_CONTENT_TYPE_AND_VERSION_HEADER)
                    .build();
            response = apacheClient.execute(getRequest);

            RestClientUtils.validateClientResponse(response, 200, "Failed to get report file");
            FilesUtils.createReportFile(response, reportFile);
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to get report file: " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }
}
