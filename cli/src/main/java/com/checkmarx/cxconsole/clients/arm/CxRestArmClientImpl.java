package com.checkmarx.cxconsole.clients.arm;

import com.checkmarx.cxconsole.clients.arm.dto.Policy;
import com.checkmarx.cxconsole.clients.arm.exceptions.CxRestARMClientException;
import com.checkmarx.cxconsole.clients.exception.CxValidateResponseException;
import com.checkmarx.cxconsole.clients.login.CxRestLoginClient;
import com.checkmarx.cxconsole.clients.utils.RestClientUtils;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static com.checkmarx.cxconsole.clients.utils.RestClientUtils.parseJsonListFromResponse;

/**
 * Created by eyala on 7/9/2018.
 */
public class CxRestArmClientImpl implements CxRestArmClient {

    private static Logger log = Logger.getLogger(CxRestArmClientImpl.class);

    private HttpClient apacheClient;
    private String hostName;

    private static final Header CLI_CONTENT_TYPE_AND_VERSION_HEADER = new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType() + ";v=1.0");
    private static final Header CLI_ACCEPT_HEADER_AND_VERSION_HEADER = new BasicHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType() + ";v=1.0");

    private static final String APPLICATION_NAME = "cxarm/policymanager";
    private static final String SAST_GET_CXARM_STATUS = "sast/projects/{projectId}/publisher/policyFindings/status";
    private static final String ARM_GET_VIOLATIONS_RESOURCE = "projects/{projectId}/violations?provider={provider}";

    public CxRestArmClientImpl(CxRestLoginClient restClient, String hostName) {
        this.apacheClient = restClient.getClient();
        this.hostName = hostName;
    }

    @Override
    public List<Policy> getProjectViolations(int projectId, String provider) throws CxRestARMClientException {
        HttpUriRequest getRequest;
        HttpResponse response = null;

        try {
            getRequest = RequestBuilder.get()
                    .setUri(String.valueOf(generateGetViolationsURI(projectId, provider)))
                    .setHeader(CLI_ACCEPT_HEADER_AND_VERSION_HEADER)
                    .setHeader(CLI_CONTENT_TYPE_AND_VERSION_HEADER)
                    .build();
            response = apacheClient.execute(getRequest);
            RestClientUtils.validateClientResponse(response, 200, "fail to get CXArm violations");
            return parseJsonListFromResponse(response, TypeFactory.defaultInstance().constructCollectionType(List.class, Policy.class));
        } catch (IOException | CxValidateResponseException e) {
            log.error("Failed to get CXArm violations: " + e.getMessage());
            throw new CxRestARMClientException("Failed to get CXArm violations:  " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public String getPolicyStatus(int projectId) {

        HttpUriRequest request;
        HttpResponse response = null;
        String ret = "";
        try {
            request = RequestBuilder.get()
                    .setUri(String.valueOf(generateGetPolicyStatusURI(projectId)))
                    .setHeader(CLI_ACCEPT_HEADER_AND_VERSION_HEADER)
                    .setHeader(CLI_CONTENT_TYPE_AND_VERSION_HEADER)
                    .build();
            response = apacheClient.execute(request);
            RestClientUtils.validateClientResponse(response, 200, "Failed getting policy status response");
            ret =  "";
        } catch (IOException | CxValidateResponseException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public void close() {
        HttpClientUtils.closeQuietly(apacheClient);
    }

    private URL generateGetViolationsURI(int projectId, String provider) throws MalformedURLException {
        return new URL(new URL(hostName), APPLICATION_NAME + "/" + ARM_GET_VIOLATIONS_RESOURCE.replace("{projectId}", String.valueOf(projectId)).replace("{provider}", provider));
    }

    private URL generateGetPolicyStatusURI(int projectId) throws MalformedURLException {
        return new URL(new URL(hostName), APPLICATION_NAME + "/" + SAST_GET_CXARM_STATUS.replace("{projectId}", String.valueOf(projectId)));
    }

}
