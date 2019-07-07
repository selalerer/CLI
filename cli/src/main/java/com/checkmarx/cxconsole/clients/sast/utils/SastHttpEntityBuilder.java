package com.checkmarx.cxconsole.clients.sast.utils;

import com.checkmarx.cxconsole.clients.exception.CxRestClientException;
import com.checkmarx.cxconsole.clients.login.exceptions.CxRestLoginClientException;
import com.checkmarx.cxconsole.clients.sast.constants.ReportType;
import com.checkmarx.cxconsole.clients.sast.dto.RemoteSourceScanSettingDTO;
import com.checkmarx.cxconsole.clients.sast.dto.ScanSettingDTO;
import com.checkmarx.cxconsole.clients.sast.exceptions.CxRestSASTClientException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nirli on 25/02/2018.
 */
public class SastHttpEntityBuilder {

    private static final String CLIENT_ID_KEY = "client_id";
    private static final String CLI_CLIENT = "cli_client";
    private static final String CLIENT_SECRET_KEY = "client_secret";
    private static final String CLIENT_SECRET_VALUE = "B9D84EA8-E476-4E83-A628-8A342D74D3BD";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String PASS_KEY = "password";
    private static final String USERNAME_KEY = "username";
    private static final String ERROR_MESSAGE_PREFIX = "Failed to create body entity, due to: ";
    private static final String GRANT_TYPE_KEY = "grant_type";

    private SastHttpEntityBuilder() {
        throw new IllegalStateException("Utility class");
    }

    public static StringEntity createScanSettingEntity(ScanSettingDTO scanSetting) throws CxRestSASTClientException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString;
        try {
            jsonInString = mapper.writeValueAsString(scanSetting);
        } catch (JsonProcessingException e) {
            throw new CxRestSASTClientException("Error creating JSON string from scan setting" + e.getMessage());
        }

        if (jsonInString.contains("\"postScanActionId\":0")) {
            jsonInString = jsonInString.replace("\"postScanActionId\":0", "\"postScanActionId\":null");
        }

        return new StringEntity(jsonInString, ContentType.APPLICATION_JSON);
    }


    public static HttpEntity createNewSastScanEntity(int projectId, boolean forceScan, boolean incrementalScan, boolean visibleOthers) {
        Map<String, String> content = new HashMap<>();
        content.put("projectId", String.valueOf(projectId));
        content.put("isIncremental", String.valueOf(incrementalScan));
        content.put("isPublic", String.valueOf(visibleOthers));
        content.put("forceScan", String.valueOf(forceScan));

        JSONObject jsonObject = new JSONObject(content);
        return new StringEntity(jsonObject.toString(), ContentType.APPLICATION_JSON);
    }

    public static HttpEntity patchSastCommentEntity(String comment) {
        Map<String, String> content = new HashMap<>();
        content.put("comment", comment);
        JSONObject jsonObject = new JSONObject(content);

        return new StringEntity(jsonObject.toString(), ContentType.APPLICATION_JSON);
    }

    public static <T extends RemoteSourceScanSettingDTO> HttpEntity createRemoteSourceEntity(T remoteSourceScanSettingDTO) throws CxRestSASTClientException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString;
        try {
            jsonInString = mapper.writeValueAsString(remoteSourceScanSettingDTO);
        } catch (JsonProcessingException e) {
            throw new CxRestSASTClientException("Error creating JSON string from remote source scan settings" + e.getMessage());
        }

        return new StringEntity(jsonInString, ContentType.APPLICATION_JSON);
    }

    public static HttpEntity createGITSourceEntity(String locationURL, String locationBranch) {
        Map<String, String> content = new HashMap<>();
        content.put("url", locationURL);
        content.put("branch", locationBranch);
        JSONObject jsonObject = new JSONObject(content);

        return new StringEntity(jsonObject.toString(), ContentType.APPLICATION_JSON);
    }

    public static HttpEntity createScanExclusionSettingEntity(String excludeFoldersPattern, String excludeFilesPattern) {
        Map<String, String> content = new HashMap<>();
        content.put("excludeFoldersPattern", excludeFoldersPattern);
        content.put("excludeFilesPattern", excludeFilesPattern);
        JSONObject jsonObject = new JSONObject(content);

        return new StringEntity(jsonObject.toString(), ContentType.APPLICATION_JSON);
    }

    public static HttpEntity createReportEntity(long scanId, ReportType reportType) {
        Map<String, String> content = new HashMap<>();
        content.put("reportType", reportType.getValue());
        content.put("scanId", String.valueOf(scanId));
        JSONObject jsonObject = new JSONObject(content);

        return new StringEntity(jsonObject.toString(), ContentType.APPLICATION_JSON);
    }

    //TODO: remove these methods by 9.30

    public static StringEntity createGenerateTokenParamsEntity(String userName, String password, boolean isLegacy) throws CxRestClientException {
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(USERNAME_KEY, userName));
        urlParameters.add(new BasicNameValuePair(PASS_KEY, password));
        urlParameters.add(new BasicNameValuePair(GRANT_TYPE_KEY, PASS_KEY));
        urlParameters.add(new BasicNameValuePair("scope", "sast_rest_api offline_access"));
        urlParameters.add(new BasicNameValuePair(CLIENT_ID_KEY, CLI_CLIENT));
        urlParameters.add(new BasicNameValuePair(CLIENT_SECRET_KEY, CLIENT_SECRET_VALUE));

        try {
            return new UrlEncodedFormEntity(urlParameters, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new CxRestClientException(ERROR_MESSAGE_PREFIX + e.getMessage());
        }
    }

    public static StringEntity createRevokeTokenParamsEntity(String token) throws CxRestClientException {
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("token_type_hint", REFRESH_TOKEN));
        urlParameters.add(new BasicNameValuePair("token", token));
        urlParameters.add(new BasicNameValuePair(CLIENT_ID_KEY, CLI_CLIENT));
        urlParameters.add(new BasicNameValuePair(CLIENT_SECRET_KEY, CLIENT_SECRET_VALUE));

        try {
            return new UrlEncodedFormEntity(urlParameters, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new CxRestClientException(ERROR_MESSAGE_PREFIX + e.getMessage());
        }
    }

    public static StringEntity createGetAccessTokenFromRefreshTokenParamsEntity(String token) throws CxRestLoginClientException {
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(GRANT_TYPE_KEY, REFRESH_TOKEN));
        urlParameters.add(new BasicNameValuePair(CLIENT_ID_KEY, CLI_CLIENT));
        urlParameters.add(new BasicNameValuePair(CLIENT_SECRET_KEY, CLIENT_SECRET_VALUE));
        urlParameters.add(new BasicNameValuePair(REFRESH_TOKEN, token));

        try {
            return new UrlEncodedFormEntity(urlParameters, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new CxRestLoginClientException(ERROR_MESSAGE_PREFIX + e.getMessage());
        }
    }

    public static StringEntity createGetAccessTokenFromCredentialsParamsEntity(String userName, String password) throws CxRestLoginClientException {
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(USERNAME_KEY, userName));
        urlParameters.add(new BasicNameValuePair(PASS_KEY, password));
        urlParameters.add(new BasicNameValuePair(GRANT_TYPE_KEY, PASS_KEY));
        urlParameters.add(new BasicNameValuePair("scope", "sast_rest_api offline_access"));
        urlParameters.add(new BasicNameValuePair(CLIENT_ID_KEY, CLI_CLIENT));
        urlParameters.add(new BasicNameValuePair(CLIENT_SECRET_KEY, CLIENT_SECRET_VALUE));

        try {
            return new UrlEncodedFormEntity(urlParameters, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new CxRestLoginClientException(ERROR_MESSAGE_PREFIX + e.getMessage());
        }
    }


}
