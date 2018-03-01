package com.checkmarx.cxconsole.clients.utils;

import com.checkmarx.cxconsole.clients.exception.CxValidateResponseException;
import com.checkmarx.cxconsole.clients.sast.dto.ScanSettingDTO;
import com.checkmarx.cxconsole.clients.sast.dto.ScanSettingDTODeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by nirli on 20/02/2018.
 */
public class RestClientUtils {

    public static JSONObject parseJsonObjectFromResponse(HttpResponse response) throws IOException {
        String responseInString = createStringFromResponse(response).toString();
        return new JSONObject(responseInString);
    }

    public static <ResponseObj> ResponseObj parseJsonFromResponse(HttpResponse response, Class<ResponseObj> dtoClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(createStringFromResponse(response).toString(), dtoClass);
    }

    public static <ResponseObj> List<ResponseObj> parseJsonListFromResponse(HttpResponse response, CollectionType dtoClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(createStringFromResponse(response).toString(), dtoClass);
    }

    private static StringBuilder createStringFromResponse(HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        return result;
    }

    public static ScanSettingDTO parseScanSettingResponse(HttpResponse response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ScanSettingDTO.class, new ScanSettingDTODeserializer());
        mapper.registerModule(module);

        return mapper.readValue(createStringFromResponse(response).toString(), ScanSettingDTO.class);
    }


    public static void validateClientResponse(HttpResponse response, int status, String message) throws CxValidateResponseException {
        try {
            if (response.getStatusLine().getStatusCode() != status) {
                String responseBody = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
                if (responseBody.contains("<!DOCTYPE html PUBLIC \"")) {
                    responseBody = "No body";
                }
                throw new CxValidateResponseException(message + ": " + "status code: " + response.getStatusLine().getStatusCode() + ". Error message:" + responseBody);
            }
        } catch (IOException e) {
            throw new CxValidateResponseException("Error parse REST response body: " + e.getMessage());
        }
    }

    public static void validateTokenResponse(HttpResponse response, int status, String message) throws CxValidateResponseException {
        try {
            if (response.getStatusLine().getStatusCode() != status) {
                String responseBody = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
                responseBody = responseBody.replace("{", "").replace("}", "").replace(System.getProperty("line.separator"), " ").replace("  ", "");
                if (responseBody.contains("<!DOCTYPE html>")) {
                    throw new CxValidateResponseException(message + ": " + "status code: 500. Error message: Internal Server Error");
                } else if (responseBody.contains("\"error\":\"invalid_grant\"")) {
                    throw new CxValidateResponseException(message);
                } else {
                    throw new CxValidateResponseException(message + ": " + "status code: " + response.getStatusLine() + ". Error message:" + responseBody);
                }
            }
        } catch (IOException e) {
            throw new CxValidateResponseException("Error parse REST response body: " + e.getMessage());
        }
    }
}