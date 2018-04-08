package com.checkmarx.cxconsole.clients.login;

import com.checkmarx.cxconsole.clients.exception.CxValidateResponseException;
import com.checkmarx.cxconsole.clients.login.dto.RestGetAccessTokenDTO;
import com.checkmarx.cxconsole.clients.login.exceptions.CxRestLoginClientException;
import com.checkmarx.cxconsole.clients.login.utils.LoginResourceURIBuilder;
import com.checkmarx.cxconsole.clients.token.utils.TokenHttpEntityBuilder;
import com.checkmarx.cxconsole.clients.utils.RestClientUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.auth.DigestSchemeFactory;
import org.apache.http.impl.auth.win.WindowsCredentialsProvider;
import org.apache.http.impl.auth.win.WindowsNTLMSchemeFactory;
import org.apache.http.impl.auth.win.WindowsNegotiateSchemeFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.SystemDefaultCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nirli on 24/10/2017.
 */
public class CxRestLoginClientImpl implements CxRestLoginClient {

    private static Logger log = Logger.getLogger(CxRestLoginClientImpl.class);

    private final String username;
    private final String password;
    private final String hostName;
    private final String token;
    private boolean isLoggedIn = false;

    private HttpClient client;
    private static List<Header> headers = new ArrayList<>();

    private static final Header CLI_ORIGIN_HEADER = new BasicHeader("cxOrigin", "cx-CLI");
    private static final Header CLI_CONTENT_TYPE_WITH_VERSION_HEADER = new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType() + ";v=1.0");

    private static final String SERVER_STACK_TRACE_ERROR_MESSAGE = "Failed to get access token: Fail to authenticate: status code: HTTP/1.1 400 Bad Request. error:\"error\":\"invalid_grant\"";
    private static final String FAIL_TO_VALIDATE_TOKEN_RESPONSE_ERROR = " User authentication failed";

    public CxRestLoginClientImpl(String hostname, String token) {
        this.hostName = hostname;
        this.token = token;
        this.username = null;
        this.password = null;

        //create http client
        client = HttpClientBuilder.create().build();
        try {
            getAccessTokenFromRefreshToken(token);
        } catch (CxRestLoginClientException e) {
            if (e.getMessage().contains(SERVER_STACK_TRACE_ERROR_MESSAGE)) {
                log.trace("Failed to login, due to: " + e.getMessage());
                log.error("Failed to login: User authentication failed");
            } else {
                log.error("Failed to login with token: " + e.getMessage());
            }
        }
        headers.add(CLI_ORIGIN_HEADER);
        client = HttpClientBuilder.create().setDefaultHeaders(headers).build();
    }

    public CxRestLoginClientImpl(String hostname, String username, String password) {
        this.hostName = hostname;
        this.username = username;
        this.password = password;
        this.token = null;

        //create http client

        headers.add(CLI_ORIGIN_HEADER);
        client = HttpClientBuilder.create().setDefaultHeaders(headers).build();
    }

    public CxRestLoginClientImpl(String hostname) {
        this.hostName = hostname;
        this.username = null;
        this.password = null;
        this.token = null;

        //create http client
        headers.add(CLI_ORIGIN_HEADER);
        final Registry<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create()
                .register(AuthSchemes.BASIC, new BasicSchemeFactory())
                .register(AuthSchemes.DIGEST, new DigestSchemeFactory())
                .register(AuthSchemes.NTLM, new WindowsNTLMSchemeFactory(null))
                .register(AuthSchemes.SPNEGO, new WindowsNegotiateSchemeFactory(null))
                .build();
        final CredentialsProvider credsProvider = new WindowsCredentialsProvider(new SystemDefaultCredentialsProvider());
        client = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(credsProvider)
                .setDefaultAuthSchemeRegistry(authSchemeRegistry)
                .setDefaultHeaders(headers)
                .build();
    }

    @Override
    public void credentialsLogin(String username, String password) throws CxRestLoginClientException {
        HttpUriRequest postRequest;
        HttpResponse loginResponse = null;
        try {
            postRequest = RequestBuilder.post()
                    .setUri(String.valueOf(LoginResourceURIBuilder.getAccessTokenURL(new URL(hostName))))
                    .setHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString())
                    .setEntity(TokenHttpEntityBuilder.createGetAccessTokenFromCredentialsParamsEntity(username, password))
                    .build();
            loginResponse = client.execute(postRequest);

            RestClientUtils.validateTokenResponse(loginResponse, 200, FAIL_TO_VALIDATE_TOKEN_RESPONSE_ERROR);
            RestGetAccessTokenDTO jsonResponse = RestClientUtils.parseJsonFromResponse(loginResponse, RestGetAccessTokenDTO.class);
            headers.add(new BasicHeader("Authorization", "Bearer " + jsonResponse.getAccessToken()));
            client = HttpClientBuilder.create().setDefaultHeaders(headers).build();
            isLoggedIn = true;
        } catch (IOException | CxValidateResponseException e) {
            log.error("Fail to login with credentials: " + e.getMessage());
            throw new CxRestLoginClientException("Fail to login with credentials: " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(loginResponse);
        }
    }

    @Override
    public void tokenLogin() throws CxRestLoginClientException {
        getAccessTokenFromRefreshToken(token);
        client = HttpClientBuilder.create().setDefaultHeaders(headers).build();
        isLoggedIn = true;
    }

    @Override
    public void ssoLogin() throws CxRestLoginClientException {
        HttpUriRequest request;
        HttpResponse loginResponse = null;
        try {
            request = RequestBuilder.post()
                    .setUri(String.valueOf(LoginResourceURIBuilder.buildWindowsAuthenticationLoginURL(new URL(hostName))))
                    .setConfig(RequestConfig.DEFAULT)
                    .setEntity(new StringEntity(""))
                    .build();
            loginResponse = client.execute(request);

            RestClientUtils.validateClientResponse(loginResponse, 200, "Fail to authenticate");
        } catch (IOException | CxValidateResponseException e) {
            log.error("Fail to login with windows authentication: " + e.getMessage());
            throw new CxRestLoginClientException("Fail to login with windows authentication: " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(loginResponse);
        }

        client = HttpClientBuilder.create().setDefaultHeaders(headers).build();
        isLoggedIn = true;
    }

    private void getAccessTokenFromRefreshToken(String refreshToken) throws CxRestLoginClientException {
        HttpResponse getAccessTokenResponse = null;
        String accessToken;
        HttpUriRequest postRequest;

        try {
            postRequest = RequestBuilder.post()
                    .setUri(String.valueOf(LoginResourceURIBuilder.getAccessTokenURL(new URL(hostName))))
                    .setHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString())
                    .setEntity(TokenHttpEntityBuilder.createGetAccessTokenFromRefreshTokenParamsEntity(refreshToken))
                    .build();
            getAccessTokenResponse = client.execute(postRequest);

            RestClientUtils.validateTokenResponse(getAccessTokenResponse, 200, FAIL_TO_VALIDATE_TOKEN_RESPONSE_ERROR);

            RestGetAccessTokenDTO jsonResponse = RestClientUtils.parseJsonFromResponse(getAccessTokenResponse, RestGetAccessTokenDTO.class);
            accessToken = jsonResponse.getAccessToken();
        } catch (IOException | CxValidateResponseException e) {
            log.trace("Failed to get access token: " + e.getMessage());
            throw new CxRestLoginClientException("User authentication failed");
        } finally {
            HttpClientUtils.closeQuietly(getAccessTokenResponse);
        }

        headers.add(new BasicHeader("Authorization", "Bearer " + accessToken));
    }

    @Override
    public HttpClient getClient() {
        return client;
    }

    @Override
    public String getHostName() {
        return hostName;
    }

    @Override
    public boolean isLoggedIn() {
        return isLoggedIn;
    }
}