package com.checkmarx.cxconsole.clients.login;

import com.checkmarx.cxconsole.clients.exception.CxValidateResponseException;
import com.checkmarx.cxconsole.clients.login.dto.RestGetAccessTokenDTO;
import com.checkmarx.cxconsole.clients.login.exceptions.CxRestLoginClientException;
import com.checkmarx.cxconsole.clients.login.utils.LoginResourceURIBuilder;
import com.checkmarx.cxconsole.clients.token.utils.TokenHttpEntityBuilder;
import com.checkmarx.cxconsole.clients.utils.RestClientUtils;
import com.google.common.base.Strings;
import org.apache.http.*;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.auth.DigestSchemeFactory;
import org.apache.http.impl.auth.win.WindowsCredentialsProvider;
import org.apache.http.impl.auth.win.WindowsNTLMSchemeFactory;
import org.apache.http.impl.auth.win.WindowsNegotiateSchemeFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.SystemDefaultCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nirli on 24/10/2017.
 */
public class CxRestLoginClientImpl implements CxRestLoginClient {

    private static final String CX_COOKIE = "cxCookie";
    private static final String CSRF_TOKEN_HEADER = "CXCSRFToken";
    private static final String TLS_PROTOCOL = "TLSv1.2";
    private static Logger log = Logger.getLogger(CxRestLoginClientImpl.class);

    private final String username;
    private final String password;
    private final String hostName;
    private final String token;
    private boolean isLoggedIn = false;

    private HttpClient client;
    private static List<Header> headers = new ArrayList<>();

    private static final Header CLI_ORIGIN_HEADER = new BasicHeader("cxOrigin", "cx-CLI");

    private static final String SERVER_STACK_TRACE_ERROR_MESSAGE = "Failed to get access token: Fail to authenticate: status code: HTTP/1.1 400 Bad Request. error:\"error\":\"invalid_grant\"";
    private static final String FAIL_TO_VALIDATE_TOKEN_RESPONSE_ERROR = " User authentication failed";

    private static CookieStore cookieStore = new BasicCookieStore();
    private String cxCookie = null;
    private String csrfToken = null;

    public CxRestLoginClientImpl(String hostname, String token) {
        this.hostName = hostname;
        this.token = token;
        this.username = null;
        this.password = null;

        SSLContext sslContext = generateSSLContext(TLS_PROTOCOL, log);
        client = HttpClientBuilder.create().build();
        try {
            getAccessTokenFromRefreshToken(token);
            headers.add(CLI_ORIGIN_HEADER);
            client = HttpClientBuilder
                    .create()
                    .setDefaultHeaders(headers)
                    .setSSLContext(sslContext)
//                    .setRedirectStrategy(new LaxRedirectStrategy())
                    .build();
        } catch (CxRestLoginClientException e) {
            if (e.getMessage().contains(SERVER_STACK_TRACE_ERROR_MESSAGE)) {
                log.trace("Failed to login, due to: " + e.getMessage());
                log.error("Failed to login: User authentication failed");
            } else {
                log.error("Failed to login with token: " + e.getMessage());
            }
        }
    }

    public CxRestLoginClientImpl(String hostname, String username, String password) {
        this.hostName = hostname;
        this.username = username;
        this.password = password;
        this.token = null;

        //create http client
        headers.add(CLI_ORIGIN_HEADER);
        SSLContext sslContext = generateSSLContext(TLS_PROTOCOL, log);
        client = HttpClientBuilder.create().setDefaultHeaders(headers).setSSLContext(sslContext).build();
    }

    public CxRestLoginClientImpl(String hostname) {
        this.hostName = hostname;
        this.username = null;
        this.password = null;
        this.token = null;

        headers.add(CLI_ORIGIN_HEADER);
        SSLContext sslContext = generateSSLContext(TLS_PROTOCOL, log);
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
                .setDefaultCookieStore(cookieStore)
                .setDefaultHeaders(headers)
                .setSSLContext(sslContext)
                .build();
    }

    @Override
    public void credentialsLogin() throws CxRestLoginClientException {
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
            SSLContext sslContext = generateSSLContext(TLS_PROTOCOL, log);
            client = HttpClientBuilder.create().setDefaultHeaders(headers).setSSLContext(sslContext).build();
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
        if (headers.size() == 2) {
            SSLContext sslContext = generateSSLContext(TLS_PROTOCOL, log);
            client = HttpClientBuilder.create().setDefaultHeaders(headers).setSSLContext(sslContext).build();
            isLoggedIn = true;
        } else {
            throw new CxRestLoginClientException("Login failed");
        }
    }

    @Override
    public void ssoLogin() throws CxRestLoginClientException {
        HttpUriRequest request;
        HttpResponse loginResponse = null;
        final String BASE_URL = "/CxRestAPI/auth/identity/";
        RequestConfig requestConfig = RequestConfig.custom()
                .setRedirectsEnabled(false)
                .setAuthenticationEnabled(true)
                .build();

        try {
            //Request1
            request = RequestBuilder.post()
                    .setUri(String.valueOf(LoginResourceURIBuilder.buildWindowsAuthenticationLoginURL(new URL(hostName))))
                    .setConfig(requestConfig)
                    .setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString())
                    .setEntity(generateEntity())
                    .build();
            loginResponse = client.execute(request);

            //Request2
            String cookies = retrieveCookies();
            String redirectURL = loginResponse.getHeaders("Location")[0].getValue();
            request = RequestBuilder.get()
                    .setUri(hostName + BASE_URL + redirectURL)
                    .setConfig(requestConfig)
                    .setHeader("Cookie", cookies)
                    .setHeader("Upgrade-Insecure-Requests", "1")
                    .build();
            loginResponse = client.execute(request);

            //Request3
            cookies = retrieveCookies();
            redirectURL = loginResponse.getHeaders("Location")[0].getValue();
            request = RequestBuilder.get()
                    .setUri(hostName + redirectURL)
                    .setConfig(requestConfig)
                    .setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString())
                    .setHeader("Cookie", cookies)
                    .build();
            loginResponse = client.execute(request);

            // RestClientUtils.validateClientResponse(loginResponse, 200, "Fail to authenticate");
            RestGetAccessTokenDTO jsonResponse = RestClientUtils.parseJsonFromResponse(loginResponse, RestGetAccessTokenDTO.class);
            CxRestLoginClientImpl.headers.add(new BasicHeader("Authorization", "Bearer " + jsonResponse.getAccessToken()));
        } catch (IOException e) {
            log.error("Fail to login with windows authentication: " + e.getMessage());
            throw new CxRestLoginClientException("Fail to login with windows authentication: " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(loginResponse);
        }
        isLoggedIn = true;
    }

    private String retrieveCookies() {
        List<Cookie> cookieList = cookieStore.getCookies();
        String cookies = "";
        for (Cookie cookie : cookieList) {
            cookies += cookie.getName() + "=" + cookie.getValue() + ";";
        }

        return cookies;
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

    @Override
    public boolean isCredentialsLogin() {
        return !Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(password);
    }

    @Override
    public boolean isTokenLogin() {
        return !Strings.isNullOrEmpty(token);
    }

    private SSLContext generateSSLContext(String protocol, Logger log) {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContextBuilder.create().setProtocol(protocol).build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.warn("Failed to build SSL context error was: " + e.getMessage());
        }
        return sslContext;
    }

    private StringEntity generateEntity() throws CxRestLoginClientException {
//        final String BASE_URL = "/CxRestAPI/auth/identity/";

        //String redirectURL = hostName + BASE_URL + String.format("connect/authorize/callback?client_id=%s&redirect_uri=%s&response_type=%s&scope=%s", "cli_client", hostName + "/CxWebClient/authCallback.html?", "token", "sast_api%20openid%20offline_access%20management_and_orchestration_api");
//        final String redirectURL = "/CxRestAPI/auth/identity/connect/authorize/callback?client_id=cli_client&redirect_uri=http%3A%2F%2F10.31.2.9%2Fcxwebclient%2FauthCallback.html%3F&response_type=id_token%20token&scope=scope=openid%20offline_access%20management_and_orchestration_api";
//                "connect/authorize/callback" +
//                "?client_id=cli_client" +
//                "&redirect_uri=http%3A%2F%2F10.31.2.9%2Fcxwebclient%2FauthCallback.html%3F&response_type=id_token%20token" +
//                "&scope=sast_api%20openid%20offline_access%20management_and_orchestration_api";
//        urlParameters.add(new BasicNameValuePair("redirectUrl", String.format("%s/CxRestAPI/auth/", hostName)));
//        urlParameters.add(new BasicNameValuePair("redirectUrl", String.format("%s/CxWebClient/authCallback.html?", hostName)));

        String redirectUrl = "%2FCxRestAPI%2Fauth%2Fidentity%2Fconnect%2Fauthorize%2Fcallback%3Fclient_id%3Dcli_client%26redirect_uri%3Dhttp%253A%252F%252F10.31.2.9%252Fcxwebclient%252FauthCallback.html%253F%26response_type%3Did_token%2520token%26scope%3Dsast_api%2520openid%2520sast-permissions%2520access-control-permissions%2520access_control_api%2520management_and_orchestration_api&providerid=2";

        List<NameValuePair> urlParameters = new ArrayList<>();
//        urlParameters.add(new BasicNameValuePair("redirectUrl", "/CxRestAPI/auth/identity/connect/authorize/callback?" +
//                "client_id=cli_client" +
//                "&redirect_uri=" + hostName + "/cxwebclient/authCallback.html?" +
//                "&scope=sast_api%20openid%20offline_access%20management_and_orchestration_api" +
//                "&esponse_type=id_token token"));
//                "&providerid=2"));

        try {
            urlParameters.add(new BasicNameValuePair("redirectUrl", redirectUrl));
            urlParameters.add(new BasicNameValuePair("providerid", "2"));
            return new UrlEncodedFormEntity(urlParameters, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new CxRestLoginClientException(e.getMessage());
        }
    }
}
