package com.checkmarx.cxconsole.clients.login.utils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by nirli on 27/02/2018.
 */
public class LoginResourceURIBuilder {

    private static final String APPLICATION_NAME = "cxrestapi";

    private static final String CREDENTIALS_LOGIN_RESOURCE = "auth/login";
    private static final String WINDOWS_AUTHENTICATION_LOGIN_RESOURCE = "auth/identity/externalLogin";
    private static final String WINDOWS_AUTHENTICATION_LOGIN_RESOURCE_LEGACY = "auth/ssologin";
    private static final String TOKEN_LOGIN_RESOURCE = "token";
    private static final String IDENTITY_CONNECT_RESOURCE = "auth/identity/connect";

    private LoginResourceURIBuilder() {
        throw new IllegalStateException("Utility class");
    }

    public static URL getAccessTokenURL(URL serverUrl) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + "/" + IDENTITY_CONNECT_RESOURCE + "/" + TOKEN_LOGIN_RESOURCE);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL buildCredentialsLoginURL(URL serverUrl) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + "/" + CREDENTIALS_LOGIN_RESOURCE);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL buildWindowsAuthenticationLoginURL(URL serverUrl) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + "/" + WINDOWS_AUTHENTICATION_LOGIN_RESOURCE);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL buildLegactWindowsAuthenticationLoginURL(URL serverUrl) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + "/" + WINDOWS_AUTHENTICATION_LOGIN_RESOURCE_LEGACY);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }
}
