package com.checkmarx.cxconsole.clients.login;

import com.checkmarx.cxconsole.clients.login.dto.RestGetAccessTokenDTO;
import com.checkmarx.cxconsole.clients.utils.RestClientUtils;
import com.checkmarx.cxconsole.clientsold.rest.exceptions.CxRestClientException;
import com.checkmarx.cxconsole.clients.login.dto.RestGenerateTokenDTO;
import com.checkmarx.cxconsole.clients.login.exceptions.CxRestLoginClientException;
import com.checkmarx.cxconsole.clientsold.rest.exceptions.CxRestClientValidatorException;
import com.checkmarx.cxconsole.clientsold.rest.utils.RestHttpEntityBuilder;
import com.checkmarx.cxconsole.clientsold.rest.utils.RestResourcesURIBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;

import static com.checkmarx.cxconsole.clients.utils.RestClientUtils.validateLoginResponse;
import static com.checkmarx.cxconsole.clients.utils.RestClientUtils.parseJsonFromResponse;

public class CxRestTokenClient {

    private static Logger log = Logger.getLogger(CxRestTokenClient.class);

    private HttpClient client = HttpClientBuilder.create().build();

    private static final String PARSING_ERROR = "Failed due to parsing error: ";
    static final String FAIL_TO_AUTHENTICATE_ERROR = " User authentication failed";

    public String generateToken(URL serverUrl, String userName, String password) throws CxRestClientException {
        HttpPost postRequest = new HttpPost(String.valueOf(RestResourcesURIBuilder.buildLoginURL(serverUrl)));
        postRequest.setHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString());

        HttpResponse generateTokenResponse = null;
        String token;
        try {
            postRequest.setEntity(RestHttpEntityBuilder.createGenerateTokenParamsEntity(userName, password));
            generateTokenResponse = client.execute(postRequest);

            validateLoginResponse(generateTokenResponse, 200, FAIL_TO_AUTHENTICATE_ERROR);

            RestGenerateTokenDTO jsonResponse = parseJsonFromResponse(generateTokenResponse, RestGenerateTokenDTO.class);
            token = jsonResponse.getRefreshToken();
        } catch (IOException e) {
            throw new CxRestLoginClientException(PARSING_ERROR + e.getMessage());
        } finally {
            postRequest.releaseConnection();
            HttpClientUtils.closeQuietly(generateTokenResponse);
        }

        return token;
    }

    public void revokeToken(URL serverUrl, String token) throws CxRestClientException {
        HttpPost postRequest = new HttpPost(String.valueOf(RestResourcesURIBuilder.buildRevokeURL(serverUrl)));
        postRequest.setHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString());

        HttpResponse generateTokenResponse = null;
        try {
            postRequest.setEntity(RestHttpEntityBuilder.createRevokeTokenParamsEntity(token));
            generateTokenResponse = client.execute(postRequest);

            validateLoginResponse(generateTokenResponse, 200, FAIL_TO_AUTHENTICATE_ERROR);
        } catch (IOException e) {
            throw new CxRestLoginClientException(PARSING_ERROR + e.getMessage());
        } finally {
            postRequest.releaseConnection();
            HttpClientUtils.closeQuietly(generateTokenResponse);
        }
    }

}