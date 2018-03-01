package com.checkmarx.cxconsole.clients.sast;

import com.checkmarx.cxconsole.clients.exception.CxValidateResponseException;
import com.checkmarx.cxconsole.clients.login.CxRestLoginClient;
import com.checkmarx.cxconsole.clients.sast.dto.EngineConfigurationDTO;
import com.checkmarx.cxconsole.clients.sast.dto.PresetDTO;
import com.checkmarx.cxconsole.clients.sast.dto.ScanSettingDTO;
import com.checkmarx.cxconsole.clients.sast.exceptions.CxRestSASTClientException;
import com.checkmarx.cxconsole.clients.sast.utils.SastHttpEntityBuilder;
import com.checkmarx.cxconsole.clients.sast.utils.SastResourceURIBuilder;
import com.checkmarx.cxconsole.clients.utils.RestClientUtils;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static com.checkmarx.cxconsole.clients.utils.RestClientUtils.parseJsonListFromResponse;

/**
 * Created by nirli on 21/02/2018.
 */
public class CxRestSASTClientImpl implements CxRestSASTClient {

    private static Logger log = Logger.getLogger(CxRestSASTClientImpl.class);

    private HttpClient apacheClient;
    private String hostName;
    private static int waitForScanToFinishRetry = ConfigMgr.getCfgMgr().getIntProperty(ConfigMgr.KEY_PROGRESS_INTERVAL);

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

    public HttpClient getApacheClient() {
        return apacheClient;
    }

    public String getHostName() {
        return hostName;
    }
}
