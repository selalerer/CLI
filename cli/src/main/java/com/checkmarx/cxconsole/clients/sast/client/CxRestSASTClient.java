package com.checkmarx.cxconsole.clients.sast.client;

import com.checkmarx.cxconsole.clients.login.CxRestLoginClient;
import com.checkmarx.cxconsole.clients.sast.dto.EngineConfigurationDTO;
import com.checkmarx.cxconsole.clients.sast.dto.PresetDTO;
import com.checkmarx.cxconsole.clients.sast.dto.TeamDTO;
import com.checkmarx.cxconsole.clients.sast.exceptions.CxRestSASTClientException;
import com.checkmarx.cxconsole.clients.sast.utils.SastResourceURIBuilder;
import com.checkmarx.cxconsole.clients.utils.RestClientUtils;
import com.checkmarx.cxconsole.clientsold.rest.exceptions.CxRestClientValidatorException;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static com.checkmarx.cxconsole.clients.utils.RestClientUtils.parseJsonListFromResponse;

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

    public List<TeamDTO> getTeams() throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpGet getRequest = null;

        try {
            getRequest = new HttpGet(String.valueOf(SastResourceURIBuilder.buildGetTeamsURL(new URL(hostName))));
            response = apacheClient.execute(getRequest);
            RestClientUtils.validateTokenResponse(response, 200, "Failed to get teams");

            return parseJsonListFromResponse(response, TypeFactory.defaultInstance().constructCollectionType(List.class, TeamDTO.class));
        } catch (IOException | CxRestClientValidatorException e) {
            throw new CxRestSASTClientException("Failed to get teams: " + e.getMessage());
        } finally {
            if (getRequest != null) {
                getRequest.releaseConnection();
            }
            HttpClientUtils.closeQuietly(response);
        }
    }

    public List<PresetDTO> getSastPresets() throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpGet getRequest = null;

        try {
            getRequest = new HttpGet(String.valueOf(SastResourceURIBuilder.buildGetSastPresetsURL(new URL(hostName))));
            response = apacheClient.execute(getRequest);
            RestClientUtils.validateTokenResponse(response, 200, "Failed to get presets");

            return parseJsonListFromResponse(response, TypeFactory.defaultInstance().constructCollectionType(List.class, PresetDTO.class));
        } catch (IOException | CxRestClientValidatorException e) {
            throw new CxRestSASTClientException("Failed to get presets: " + e.getMessage());
        } finally {
            if (getRequest != null) {
                getRequest.releaseConnection();
            }
            HttpClientUtils.closeQuietly(response);
        }
    }

    public List<EngineConfigurationDTO> getEngineConfiguration() throws CxRestSASTClientException {
        HttpResponse response = null;
        HttpGet getRequest = null;

        try {
            getRequest = new HttpGet(String.valueOf(SastResourceURIBuilder.buildGetEngineConfigurationURL(new URL(hostName))));
            response = apacheClient.execute(getRequest);
            RestClientUtils.validateTokenResponse(response, 200, "Failed to get engine configuration");

            return parseJsonListFromResponse(response, TypeFactory.defaultInstance().constructCollectionType(List.class, PresetDTO.class));
        } catch (IOException | CxRestClientValidatorException e) {
            throw new CxRestSASTClientException("Failed to get engine configuration: " + e.getMessage());
        } finally {
            if (getRequest != null) {
                getRequest.releaseConnection();
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
