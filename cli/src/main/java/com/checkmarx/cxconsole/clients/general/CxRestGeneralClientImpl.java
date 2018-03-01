package com.checkmarx.cxconsole.clients.general;

import com.checkmarx.cxconsole.clients.exception.CxValidateResponseException;
import com.checkmarx.cxconsole.clients.general.dto.ProjectDTO;
import com.checkmarx.cxconsole.clients.general.dto.TeamDTO;
import com.checkmarx.cxconsole.clients.general.exception.CxRestGeneralClientException;
import com.checkmarx.cxconsole.clients.general.utils.GeneralHttpEntityBuilder;
import com.checkmarx.cxconsole.clients.general.utils.GeneralResourceURIBuilder;
import com.checkmarx.cxconsole.clients.login.CxRestLoginClient;
import com.checkmarx.cxconsole.clients.utils.RestClientUtils;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static com.checkmarx.cxconsole.clients.utils.RestClientUtils.parseJsonListFromResponse;

/**
 * Created by nirli on 27/02/2018.
 */
public class CxRestGeneralClientImpl implements CxRestGeneralClient {

    private static final int UNASSIGNED_VALUE = 0;

    private HttpClient apacheClient;
    private String hostName;

    public CxRestGeneralClientImpl(CxRestLoginClient restClient) {
        this.apacheClient = restClient.getApacheClient();
        this.hostName = restClient.getHostName();
    }

    @Override
    public List<TeamDTO> getTeams() throws CxRestGeneralClientException {
        HttpResponse response = null;
        HttpGet getRequest = null;

        try {
            getRequest = new HttpGet(String.valueOf(GeneralResourceURIBuilder.buildGetTeamsURL(new URL(hostName))));
            response = apacheClient.execute(getRequest);
            RestClientUtils.validateClientResponse(response, 200, "Failed to get teams");

            return parseJsonListFromResponse(response, TypeFactory.defaultInstance().constructCollectionType(List.class, TeamDTO.class));
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestGeneralClientException("Failed to get teams: " + e.getMessage());
        } finally {
            if (getRequest != null) {
                getRequest.releaseConnection();
            }
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public List<ProjectDTO> getProjects() throws CxRestGeneralClientException {
        HttpResponse response = null;
        HttpGet getRequest = null;

        try {
            getRequest = new HttpGet(String.valueOf(GeneralResourceURIBuilder.buildProjectsURL(new URL(hostName))));
            response = apacheClient.execute(getRequest);
            RestClientUtils.validateClientResponse(response, 200, "Failed to get projects");

            return parseJsonListFromResponse(response, TypeFactory.defaultInstance().constructCollectionType(List.class, ProjectDTO.class));
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestGeneralClientException("Failed to get projects: " + e.getMessage());
        } finally {
            if (getRequest != null) {
                getRequest.releaseConnection();
            }
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public void createNewProject(ProjectDTO projectToCreate) throws CxRestGeneralClientException {
        HttpResponse response = null;
        HttpPost postRequest = null;

        try {
            postRequest = new HttpPost(String.valueOf(GeneralResourceURIBuilder.buildProjectsURL(new URL(hostName))));
            postRequest.setEntity(GeneralHttpEntityBuilder.createProjectEntity(projectToCreate));

            response = apacheClient.execute(postRequest);
            RestClientUtils.validateClientResponse(response, 201, "Failed to create new project");

            JSONObject jsonResponse = RestClientUtils.parseJsonObjectFromResponse(response);
            projectToCreate.setId(jsonResponse.getInt("id"));
            if (projectToCreate.getId() == UNASSIGNED_VALUE) {
                throw new CxRestGeneralClientException("Failed to get new project id");
            }
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestGeneralClientException("Failed to create project: " + e.getMessage());
        } finally {
            if (postRequest != null) {
                postRequest.releaseConnection();
            }
            HttpClientUtils.closeQuietly(response);
        }
    }
}
