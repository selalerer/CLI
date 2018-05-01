package com.checkmarx.cxconsole.clients.login.utils;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nirli on 21/02/2018.
 */
public class LoginHttpEntityBuilder {

    private LoginHttpEntityBuilder() {
        throw new IllegalStateException("Utility class");
    }

    private static final String PASS_KEY = "password";
    private static final String USERNAME_KEY = "username";

    public static HttpEntity createLoginParamsEntity(String userName, String password) {
        Map<String, String> content = new HashMap<>();
        content.put(USERNAME_KEY, userName);
        content.put(PASS_KEY, password);
        JSONObject jsonObject = new JSONObject(content);

        return new StringEntity(jsonObject.toString(), ContentType.APPLICATION_JSON);
    }
}
