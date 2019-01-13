package com.checkmarx.cxconsole.clients.token;

import com.checkmarx.cxconsole.clients.exception.CxRestClientException;
import com.checkmarx.cxconsole.clients.login.dto.RestGenerateTokenDTO;
import com.checkmarx.cxconsole.clients.login.exceptions.CxRestLoginClientException;
import com.checkmarx.cxconsole.clients.token.utils.TokenHttpEntityBuilder;
import com.checkmarx.cxconsole.clients.token.utils.TokenResourceURIBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.net.URL;

import static com.checkmarx.cxconsole.clients.utils.RestClientUtils.parseJsonFromResponse;
import static com.checkmarx.cxconsole.clients.utils.RestClientUtils.validateTokenResponse;

public class CxRestTokenClientImpl implements CxRestTokenClient {

    private HttpClient client = HttpClientBuilder.create().build();

    private static final String PARSING_ERROR = "Failed due to parsing error: ";
    private static final String FAIL_TO_VALIDATE_TOKEN_RESPONSE_ERROR = " User authentication failed";

    @Override
    public String generateToken(URL serverUrl, String userName, String password, boolean checkPolicyViolations) throws CxRestClientException {
        HttpResponse generateTokenResponse = null;
        HttpUriRequest postRequest;

        try {
            postRequest = RequestBuilder.post()
                    .setUri(String.valueOf(TokenResourceURIBuilder.buildGenerateTokenURL(serverUrl)))
                    .setHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString())
                    .setEntity(TokenHttpEntityBuilder.createGenerateTokenParamsEntity(userName, password, checkPolicyViolations))
                    .build();
            generateTokenResponse = client.execute(postRequest);

            validateTokenResponse(generateTokenResponse, 200, FAIL_TO_VALIDATE_TOKEN_RESPONSE_ERROR);
            RestGenerateTokenDTO jsonResponse = parseJsonFromResponse(generateTokenResponse, RestGenerateTokenDTO.class);
            return jsonResponse.getRefreshToken();
        } catch (IOException e) {
            throw new CxRestLoginClientException(PARSING_ERROR + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(generateTokenResponse);
        }
    }

    @Override
    public void revokeToken(URL serverUrl, String token) throws CxRestClientException {
        HttpResponse generateTokenResponse = null;
        HttpUriRequest postRequest;

        try {
            postRequest = RequestBuilder.post()
                    .setUri(String.valueOf(TokenResourceURIBuilder.buildRevokeURL(serverUrl)))
                    .setHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString())
                    .setEntity(TokenHttpEntityBuilder.createRevokeTokenParamsEntity(token))
                    .build();
            generateTokenResponse = client.execute(postRequest);

            validateTokenResponse(generateTokenResponse, 200, FAIL_TO_VALIDATE_TOKEN_RESPONSE_ERROR);
        } catch (IOException e) {
            throw new CxRestLoginClientException(PARSING_ERROR + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(generateTokenResponse);
        }
    }
}