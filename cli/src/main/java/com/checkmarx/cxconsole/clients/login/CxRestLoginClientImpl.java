package com.checkmarx.cxconsole.clients.login;

import com.checkmarx.cxconsole.clients.exception.CxValidateResponseException;
import com.checkmarx.cxconsole.clients.login.dto.RestGetAccessTokenDTO;
import com.checkmarx.cxconsole.clients.login.exceptions.CxRestLoginClientException;
import com.checkmarx.cxconsole.clients.login.utils.LoginHttpEntityBuilder;
import com.checkmarx.cxconsole.clients.login.utils.LoginResourceURIBuilder;
import com.checkmarx.cxconsole.clients.token.utils.TokenHttpEntityBuilder;
import com.checkmarx.cxconsole.clients.utils.RestClientUtils;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
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
    private static CookieStore cookieStore = new BasicCookieStore();
    private static List<Header> headers = new ArrayList<>();
    private Header cxCsrfTokenHeader;
    private String cookies;
    private String csrfToken;

    private static final Header CLI_ORIGIN_HEADER = new BasicHeader("cxOrigin", "cx-CLI");
    private static final Header CLI_CONTENT_TYPE_AND_VERSION_HEADER = new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON + ";v=1.0");
    private static final String CSRF_TOKEN_HEADER_KEY = "CXCSRFToken";

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
        headers.add(cxCsrfTokenHeader);
        headers.add(CLI_ORIGIN_HEADER);
        client = HttpClientBuilder.create().addInterceptorLast(responseFilter).setDefaultHeaders(headers).setDefaultCookieStore(cookieStore).build();
    }

    private final HttpResponseInterceptor responseFilter = new HttpResponseInterceptor() {
        public void process(HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
            for (org.apache.http.cookie.Cookie c : cookieStore.getCookies()) {
                if (CSRF_TOKEN_HEADER_KEY.equals(c.getName())) {
                    csrfToken = c.getValue();
                }
            }
            if (csrfToken != null) {
                cxCsrfTokenHeader = new BasicHeader(CSRF_TOKEN_HEADER_KEY, csrfToken);
                headers.add(cxCsrfTokenHeader);
            }

            Header[] setCookies = httpResponse.getHeaders("Set-Cookie");

            StringBuilder sb = new StringBuilder();
            for (Header h : setCookies) {
                sb.append(h.getValue()).append(";");
            }

            cookies = (cookies == null ? "" : cookies) + sb.toString();
        }
    };

    @Override
    public void credentialsLogin() throws CxRestLoginClientException {
        cookies = null;
        csrfToken = null;
        HttpUriRequest request;
        HttpResponse loginResponse = null;
        try {
            request = RequestBuilder.post()
                    .setUri(String.valueOf(LoginResourceURIBuilder.buildCredentialsLoginURL(new URL(hostName))))
                    .setEntity(LoginHttpEntityBuilder.createLoginParamsEntity(username, password))
                    .setHeader(CLI_CONTENT_TYPE_AND_VERSION_HEADER)
                    .build();
            loginResponse = client.execute(request);

            RestClientUtils.validateClientResponse(loginResponse, 200, "Fail to authenticate");
        } catch (IOException | CxValidateResponseException e) {
            log.error("Fail to login with credentials: " + e.getMessage());
            throw new CxRestLoginClientException("Fail to login with credentials: " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(loginResponse);
        }

        client = HttpClientBuilder.create().setDefaultHeaders(headers).setDefaultCookieStore(cookieStore).build();
        isLoggedIn = true;
    }

    @Override
    public void tokenLogin() throws CxRestLoginClientException {
        getAccessTokenFromRefreshToken(token);
        client = HttpClientBuilder.create().setDefaultHeaders(headers).build();
        isLoggedIn = true;
    }

    private void getAccessTokenFromRefreshToken(String refreshToken) throws CxRestLoginClientException {
        HttpResponse getAccessTokenResponse = null;
        String accessToken;
        HttpUriRequest postRequest = null;

        try {
            postRequest = RequestBuilder.post()
                    .setUri(String.valueOf(LoginResourceURIBuilder.getAccessTokenURL(new URL(hostName))))
                    .setHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString())
                    .setEntity(TokenHttpEntityBuilder.createGetAccessTokenParamsEntity(refreshToken))
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