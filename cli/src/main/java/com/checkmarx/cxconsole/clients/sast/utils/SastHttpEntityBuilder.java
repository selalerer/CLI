package com.checkmarx.cxconsole.clients.sast.utils;

import com.checkmarx.cxconsole.clients.sast.constants.ReportType;
import com.checkmarx.cxconsole.clients.sast.dto.RemoteSourceScanSettingDTO;
import com.checkmarx.cxconsole.clients.sast.dto.ScanSettingDTO;
import com.checkmarx.cxconsole.clients.sast.exceptions.CxRestSASTClientException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nirli on 25/02/2018.
 */
public class SastHttpEntityBuilder {

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
}
