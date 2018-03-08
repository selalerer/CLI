package com.checkmarx.cxconsole.clients.sast.utils;

import com.checkmarx.cxconsole.clients.sast.dto.RemoteSourceScanSettingDTO;
import com.checkmarx.cxconsole.clients.sast.dto.ScanSettingDTO;
import com.checkmarx.cxconsole.clients.sast.exceptions.CxRestSASTClientException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.bouncycastle.util.Arrays;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by nirli on 25/02/2018.
 */
public class SastHttpEntityBuilder<T extends RemoteSourceScanSettingDTO> {

    private SastHttpEntityBuilder() {
        throw new IllegalStateException("Utility class");
    }

    private static final String ERROR_MESSAGE_PREFIX = "Failed to create body entity, due to: ";

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


    public static HttpEntity createNewSastScanEntity(int projectId, boolean forceScan, boolean incrementalScan, boolean visibleOthers) throws CxRestSASTClientException {
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("projectId", String.valueOf(projectId)));
        urlParameters.add(new BasicNameValuePair("isIncremental", String.valueOf(incrementalScan)));
        urlParameters.add(new BasicNameValuePair("isPublic", String.valueOf(visibleOthers)));
        urlParameters.add(new BasicNameValuePair("forceScan", String.valueOf(forceScan)));

        try {
            return new UrlEncodedFormEntity(urlParameters, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new CxRestSASTClientException(ERROR_MESSAGE_PREFIX + e.getMessage());
        }
    }

    public static HttpEntity patchSastCommentEntity(String comment) throws CxRestSASTClientException {
        Map<String, String> content = new HashMap<>();
        content.put("content", comment);
        JSONObject jsonObject = new JSONObject(content);

        return new StringEntity(jsonObject.toString(), ContentType.APPLICATION_JSON);
    }

    public static<T extends RemoteSourceScanSettingDTO> HttpEntity createRemoteSourceEntity(T remoteSourceScanSettingDTO) throws CxRestSASTClientException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString;
        try {
            jsonInString = mapper.writeValueAsString(remoteSourceScanSettingDTO);
        } catch (JsonProcessingException e) {
            throw new CxRestSASTClientException("Error creating JSON string from remote source scan settings" + e.getMessage());
        }

        return new StringEntity(jsonInString, ContentType.APPLICATION_JSON);
    }
}
