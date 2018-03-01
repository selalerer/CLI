package com.checkmarx.cxconsole.clients.token.utils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by nirli on 27/02/2018.
 */
public class TokenResourceURIBuilder {

    private static final String APPLICATION_NAME = "cxrestapi";
    private static final String REVOCATION_RESOURCE = "revocation";
    private static final String TOKEN_LOGIN_RESOURCE = "token";
    private static final String IDENTITY_CONNECT_RESOURCE = "auth/identity/connect";

    public static URL buildGenerateTokenURL(URL serverUrl) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + "/" + IDENTITY_CONNECT_RESOURCE + "/" + TOKEN_LOGIN_RESOURCE);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL buildRevokeURL(URL serverUrl) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + "/" + IDENTITY_CONNECT_RESOURCE + "/" + REVOCATION_RESOURCE);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

}
