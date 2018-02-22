package com.checkmarx.cxconsole.clients.login.utils;

import com.checkmarx.cxconsole.clients.login.exceptions.CxRestLoginClientException;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nirli on 21/02/2018.
 */
public class LoginHttpEntityBuilder {

    private LoginHttpEntityBuilder() {
        throw new IllegalStateException("Utility class");
    }

    private static final String PASS_KEY = "password";
    private static final String USERNAME_KEY = "username";
    private static final String ERROR_MESSAGE_PREFIX = "Failed to create body entity, due to: ";

    public static UrlEncodedFormEntity createLoginParamsEntity(String userName, String password) throws CxRestLoginClientException {
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(USERNAME_KEY, userName));
        urlParameters.add(new BasicNameValuePair(PASS_KEY, password));

        try {
            return new UrlEncodedFormEntity(urlParameters, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new CxRestLoginClientException(ERROR_MESSAGE_PREFIX + e.getMessage());
        }
    }
}
