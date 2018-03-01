package com.checkmarx.cxconsole.clients.sast.utils;

import com.checkmarx.cxconsole.clients.sast.dto.ScanSettingDTO;
import com.checkmarx.cxconsole.clients.sast.exceptions.CxRestSASTClientException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

/**
 * Created by nirli on 25/02/2018.
 */
public class SastHttpEntityBuilder {

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


}
