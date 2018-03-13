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
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static com.checkmarx.cxconsole.clients.utils.RestClientUtils.parseJsonListFromResponse;

/**
 * Created by nirli on 21/02/2018.
 */
public class CxRestSASTClientImpl<T extends RemoteSourceScanSettingDTO> implements CxRestSASTClient<T> {

    private static Logger log = Logger.getLogger(CxRestSASTClientImpl.class);

    private HttpClient apacheClient;
    private String hostName;

    public CxRestSASTClientImpl(CxRestLoginClient restClient) {
        this.apacheClient = restClient.getApacheClient();
        this.hostName = restClient.getHostName();
    }

    @Override
    public List<PresetDTO> getSastPresets() throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpGet getRequest = null;

        try {
            getRequest = new HttpGet(String.valueOf(SastResourceURIBuilder.buildGetSastPresetsURL(new URL(hostName))));
            response = apacheClient.execute(getRequest);
            RestClientUtils.validateClientResponse(response, 200, "Failed to get presets");

            return parseJsonListFromResponse(response, TypeFactory.defaultInstance().constructCollectionType(List.class, PresetDTO.class));
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to get presets: " + e.getMessage());
        } finally {
            if (getRequest != null) {
                getRequest.releaseConnection();
            }
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public List<EngineConfigurationDTO> getEngineConfiguration() throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpGet getRequest = null;

        try {
            getRequest = new HttpGet(String.valueOf(SastResourceURIBuilder.buildGetEngineConfigurationURL(new URL(hostName))));
            response = apacheClient.execute(getRequest);
            RestClientUtils.validateClientResponse(response, 200, "Failed to get engine configuration");

            return parseJsonListFromResponse(response, TypeFactory.defaultInstance().constructCollectionType(List.class, EngineConfigurationDTO.class));
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to get engine configuration: " + e.getMessage());
        } finally {
            if (getRequest != null) {
                getRequest.releaseConnection();
            }
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public ScanSettingDTO getProjectScanSetting(int id) throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpGet getRequest = null;

        try {
            getRequest = new HttpGet(String.valueOf(SastResourceURIBuilder.buildGetSASTScanSettingURL(new URL(hostName), id)));
            response = apacheClient.execute(getRequest);
            RestClientUtils.validateClientResponse(response, 200, "Failed to get SAST scan setting");

            return RestClientUtils.parseScanSettingResponse(response);
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to get SAST scan setting: " + e.getMessage());
        } finally {
            if (getRequest != null) {
                getRequest.releaseConnection();
            }
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public void createProjectScanSetting(ScanSettingDTO scanSetting) throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpPost postRequest = null;

        try {
            postRequest = new HttpPost(String.valueOf(SastResourceURIBuilder.buildSASTScanSettingURL(new URL(hostName))));
            postRequest.setEntity(SastHttpEntityBuilder.createScanSettingEntity(scanSetting));

            response = apacheClient.execute(postRequest);
            RestClientUtils.validateClientResponse(response, 200, "Failed to create scan settings");
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to create scan settings: " + e.getMessage());
        } finally {
            if (postRequest != null) {
                postRequest.releaseConnection();
            }
            HttpClientUtils.closeQuietly(response);
        }

    }

    @Override
    public void updateProjectScanSetting(ScanSettingDTO scanSetting) throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpPut putRequest = null;

        try {
            putRequest = new HttpPut(String.valueOf(SastResourceURIBuilder.buildSASTScanSettingURL(new URL(hostName))));
            putRequest.setEntity(SastHttpEntityBuilder.createScanSettingEntity(scanSetting));

            response = apacheClient.execute(putRequest);
            RestClientUtils.validateClientResponse(response, 200, "Failed to update scan settings");
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to update scan settings: " + e.getMessage());
        } finally {
            if (putRequest != null) {
                putRequest.releaseConnection();
            }
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public int createNewSastScan(int projectId, boolean forceScan, boolean incrementalScan, boolean visibleOthers) throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpPost postRequest = null;

        try {
            postRequest = new HttpPost(String.valueOf(SastResourceURIBuilder.buildCreateNewSastScanURL(new URL(hostName))));
            postRequest.setEntity(SastHttpEntityBuilder.createNewSastScanEntity(projectId, forceScan, incrementalScan, visibleOthers));

            response = apacheClient.execute(postRequest);
            RestClientUtils.validateClientResponse(response, 201, "Failed to create new SAST scan");

            JSONObject jsonResponse = RestClientUtils.parseJsonObjectFromResponse(response);
            return jsonResponse.getInt("id");
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to create new SAST scan: " + e.getMessage());
        } finally {
            if (postRequest != null) {
                postRequest.releaseConnection();
            }
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public void updateScanExclusions(int projectId, String[] excludeFoldersPattern, String[] excludeFilesPattern) throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpPut putRequest = null;

        try {
            putRequest = new HttpPut(String.valueOf(SastResourceURIBuilder.buildSASTScanExclusionSettingURL(new URL(hostName), projectId)));
            putRequest.setEntity(SastHttpEntityBuilder.createScanExclusionSettingEntity(StringUtils.join(excludeFoldersPattern, ","),
                    StringUtils.join(excludeFilesPattern, ",")));

            response = apacheClient.execute(putRequest);
            RestClientUtils.validateClientResponse(response, 200, "Failed to update scan exclusions settings");
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to update scan exclusions settings: " + e.getMessage());
        } finally {
            if (putRequest != null) {
                putRequest.releaseConnection();
            }
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public void updateScanComment(long scanId, String comment) throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpPatch patchRequest = null;

        try {
            patchRequest = new HttpPatch(String.valueOf(SastResourceURIBuilder.buildAddSastCommentURL(new URL(hostName), scanId)));
            patchRequest.setEntity(SastHttpEntityBuilder.patchSastCommentEntity(comment));

            response = apacheClient.execute(patchRequest);
            RestClientUtils.validateClientResponse(response, 204, "Failed to add comment to SAST scan (id " + scanId + ")");
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to add comment to SAST scan (id " + scanId + "): " + e.getMessage());
        } finally {
            if (patchRequest != null) {
                patchRequest.releaseConnection();
            }
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public void uploadZipFileForSASTScan(int projectId, byte[] zipFile) throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpPost postRequest = null;

        try {
            postRequest = new HttpPost(String.valueOf(SastResourceURIBuilder.buildUploadZipFileURL(new URL(hostName), projectId)));
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("zippedSource", zipFile, ContentType.APPLICATION_OCTET_STREAM, null);
            HttpEntity multipart = builder.build();
            postRequest.setEntity(multipart);

            log.info("Uploading zipped source files to server, please wait.");
            response = apacheClient.execute(postRequest);
            RestClientUtils.validateClientResponse(response, 204, "Failed to upload zip file for SAST scan");
            log.info("Zipped source files were uploaded successfully");
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to upload zip file for SAST scan: " + e.getMessage());
        } finally {
            if (postRequest != null) {
                postRequest.releaseConnection();
            }
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public ScanQueueDTO getScanQueueResponse(long scanId) throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpGet getRequest = null;

        try {
            getRequest = new HttpGet(String.valueOf(SastResourceURIBuilder.buildGetSASTScanQueueResponseURL(new URL(hostName), scanId)));
            response = apacheClient.execute(getRequest);
            RestClientUtils.validateClientResponse(response, 200, "Failed to get SAST scan queue response");

            return RestClientUtils.parseJsonFromResponse(response, ScanQueueDTO.class);
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to get SAST scan queue response: " + e.getMessage());
        } finally {
            if (getRequest != null) {
                getRequest.releaseConnection();
            }
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public ScanStatusDTO getScanStatus(long scanId) throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpGet getRequest = null;

        try {
            getRequest = new HttpGet(String.valueOf(SastResourceURIBuilder.buildGetSASTScanStatusURL(new URL(hostName), scanId)));
            response = apacheClient.execute(getRequest);
            RestClientUtils.validateClientResponse(response, 200, "Failed to get SAST scan status");

            return RestClientUtils.parseJsonFromResponse(response, ScanStatusDTO.class);
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to get SAST scan status: " + e.getMessage());
        } finally {
            if (getRequest != null) {
                getRequest.releaseConnection();
            }
            HttpClientUtils.closeQuietly(response);
        }
    }

    public void createRemoteSourceScan(int projectId, T remoteSourceScanSettingDTO, RemoteSourceType remoteSourceType) throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpPost postRequest = null;

        try {
            if (remoteSourceScanSettingDTO instanceof SVNAndTFSScanSettingDTO && ((SVNAndTFSScanSettingDTO) remoteSourceScanSettingDTO).getPrivateKey().length > 1) {
                postRequest = new HttpPost(String.valueOf(SastResourceURIBuilder.buildCreateRemoteSourceScanURL(new URL(hostName), projectId, remoteSourceType, true)));
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.addBinaryBody("privateKey", ((SVNAndTFSScanSettingDTO) remoteSourceScanSettingDTO).getPrivateKey());
                builder.addTextBody("absoluteUrl", ((SVNAndTFSScanSettingDTO) remoteSourceScanSettingDTO).getUri().getAbsoluteUrl());
                builder.addTextBody("port", String.valueOf(((SVNAndTFSScanSettingDTO) remoteSourceScanSettingDTO).getUri().getPort()));
                builder.addTextBody("path", (Arrays.toString(remoteSourceScanSettingDTO.getPaths())));
                HttpEntity multipart = builder.build();
                postRequest.setEntity(multipart);
            } else {
                postRequest = new HttpPost(String.valueOf(SastResourceURIBuilder.buildCreateRemoteSourceScanURL(new URL(hostName), projectId, remoteSourceType, false)));
                postRequest.setEntity(SastHttpEntityBuilder.createRemoteSourceEntity(remoteSourceScanSettingDTO));
            }

            response = apacheClient.execute(postRequest);
            RestClientUtils.validateClientResponse(response, 204, "Failed to create " + remoteSourceType.getUrlValue() + " remote source scan setting");
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to create " + remoteSourceType.getUrlValue() + " remote source scan setting: " + e.getMessage());
        } finally {
            if (postRequest != null) {
                postRequest.releaseConnection();
            }
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public void createGITScan(int projectId, String locationURL, String locationBranch, byte[] privateKey) throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpPost postRequest = null;

        try {
            if (privateKey.length < 1) {
                postRequest = new HttpPost(String.valueOf(SastResourceURIBuilder.buildCreateRemoteSourceScanURL(new URL(hostName), projectId, RemoteSourceType.GIT, false)));
                postRequest.setEntity(SastHttpEntityBuilder.createGITSourceEntity(locationURL, locationBranch));
            } else {
                postRequest = new HttpPost(String.valueOf(SastResourceURIBuilder.buildCreateRemoteSourceScanURL(new URL(hostName), projectId, RemoteSourceType.GIT, true)));
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.addBinaryBody("privateKey", privateKey);
                builder.addTextBody("url", locationURL);
                builder.addTextBody("branch", locationBranch);
                HttpEntity multipart = builder.build();
                postRequest.setEntity(multipart);
            }

            response = apacheClient.execute(postRequest);
            RestClientUtils.validateClientResponse(response, 204, "Failed to create " + RemoteSourceType.GIT.getUrlValue() + " remote source scan setting");
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to create " + RemoteSourceType.GIT.getUrlValue() + " remote source scan setting: " + e.getMessage());
        } finally {
            if (postRequest != null) {
                postRequest.releaseConnection();
            }
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public int createReport(long scanId, ReportType reportType) throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpPost postRequest = null;

        try {
            postRequest = new HttpPost(String.valueOf(SastResourceURIBuilder.buildCreateReportURL(new URL(hostName))));
            postRequest.setEntity(SastHttpEntityBuilder.createReportEntity(scanId, reportType));

            response = apacheClient.execute(postRequest);
            RestClientUtils.validateClientResponse(response, 202, "Failed to create " + reportType.getValue() + " report");

            JSONObject jsonResponse = RestClientUtils.parseJsonObjectFromResponse(response);
            return jsonResponse.getInt("reportId");
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to create " + reportType.getValue() + " report: " + e.getMessage());
        } finally {
            if (postRequest != null) {
                postRequest.releaseConnection();
            }
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public ReportStatusValue getReportStatus(int reportId) throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpGet getRequest = null;

        try {
            getRequest = new HttpGet(String.valueOf(SastResourceURIBuilder.buildGetReportStatusURL(new URL(hostName), reportId)));
            response = apacheClient.execute(getRequest);
            RestClientUtils.validateClientResponse(response, 200, "Failed to get report status");

            JSONObject jsonResponse = RestClientUtils.parseJsonObjectFromResponse(response);
            return ReportStatusValue.getServerValue(jsonResponse.getJSONObject("status").getString("value"));
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to get report status: " + e.getMessage());
        } finally {
            if (getRequest != null) {
                getRequest.releaseConnection();
            }
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public void createReportFile(int reportId, String reportFilePath) throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpGet getRequest = null;

        try {
            getRequest = new HttpGet(String.valueOf(SastResourceURIBuilder.buildGetReportFileURL(new URL(hostName), reportId)));
            response = apacheClient.execute(getRequest);
            RestClientUtils.validateClientResponse(response, 200, "Failed to get report file");

            FilesUtils.createReportFile(response, reportFilePath);
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestSASTClientException("Failed to get report file: " + e.getMessage());
        } finally {
            if (getRequest != null) {
                getRequest.releaseConnection();
            }
            HttpClientUtils.closeQuietly(response);
        }
    }
}
