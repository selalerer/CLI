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
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
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
    private static final Header CLI_CONTENT_TYPE_AND_VERSION_HEADER = new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType() + ";v=1.0");
    private static final Header CLI_ACCEPT_AND_VERSION_HEADER = new BasicHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType() + ";v=1.0");


    public CxRestGeneralClientImpl(CxRestLoginClient restClient) {
        this.apacheClient = restClient.getClient();
        this.hostName = restClient.getHostName();
    }

    @Override
    public List<TeamDTO> getTeams() throws CxRestGeneralClientException {
        HttpResponse response = null;
        HttpUriRequest getRequest;

        try {
            getRequest = RequestBuilder.get()
                    .setUri(String.valueOf(GeneralResourceURIBuilder.buildGetTeamsURL(new URL(hostName))))
                    .setHeader(CLI_ACCEPT_AND_VERSION_HEADER)
                    .build();
            response = apacheClient.execute(getRequest);

            RestClientUtils.validateClientResponse(response, 200, "Failed to get teams");
            return parseJsonListFromResponse(response, TypeFactory.defaultInstance().constructCollectionType(List.class, TeamDTO.class));
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestGeneralClientException("Failed to get teams: " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public List<ProjectDTO> getProjects() throws CxRestGeneralClientException {
        HttpResponse response = null;
        HttpUriRequest getRequest;

        try {
            getRequest = RequestBuilder.get()
                    .setUri(String.valueOf(GeneralResourceURIBuilder.buildProjectsURL(new URL(hostName))))
                    .setHeader(CLI_ACCEPT_AND_VERSION_HEADER)
                    .build();
            response = apacheClient.execute(getRequest);

            RestClientUtils.validateClientResponse(response, 200, "Failed to get projects");
            return parseJsonListFromResponse(response, TypeFactory.defaultInstance().constructCollectionType(List.class, ProjectDTO.class));
        } catch (IOException | CxValidateResponseException e) {
            throw new CxRestGeneralClientException("Failed to get projects: " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public void createNewProject(ProjectDTO projectToCreate) throws CxRestGeneralClientException {
        HttpResponse response = null;
        HttpUriRequest postRequest;

        try {
            postRequest = RequestBuilder.post()
                    .setUri(String.valueOf(GeneralResourceURIBuilder.buildProjectsURL(new URL(hostName))))
                    .setEntity(GeneralHttpEntityBuilder.createProjectEntity(projectToCreate))
                    .setHeader(CLI_CONTENT_TYPE_AND_VERSION_HEADER)
                    .build();

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
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public boolean isLoggedIn() {
        return (apacheClient != null);
    }
}
