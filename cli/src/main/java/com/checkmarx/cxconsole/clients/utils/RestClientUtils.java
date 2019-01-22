package com.checkmarx.cxconsole.clients.utils;

import com.checkmarx.cxconsole.clients.arm.CxRestArmClient;
import com.checkmarx.cxconsole.clients.arm.dto.Policy;
import com.checkmarx.cxconsole.clients.arm.exceptions.CxRestARMClientException;
import com.checkmarx.cxconsole.clients.exception.CxValidateResponseException;
import com.checkmarx.cxconsole.clients.general.dto.CxProviders;
import com.checkmarx.cxconsole.clients.sast.dto.ScanSettingDTO;
import com.checkmarx.cxconsole.clients.sast.dto.ScanSettingDTODeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.checkmarx.cxconsole.exitcodes.Constants.ExitCodes.POLICY_VIOLATION_ERROR_EXIT_CODE;
import static com.checkmarx.cxconsole.exitcodes.Constants.ExitCodes.SCAN_SUCCEEDED_EXIT_CODE;

/**
 * Created by nirli on 20/02/2018.
 */
public interface RestClientUtils {

    String SEPARATOR = ",";

    static JSONObject parseJsonObjectFromResponse(HttpResponse response) throws IOException {
        String responseInString = createStringFromResponse(response).toString();
        return new JSONObject(responseInString);
    }

    static <ResponseObj> ResponseObj parseJsonFromResponse(HttpResponse response, Class<ResponseObj> dtoClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(createStringFromResponse(response).toString(), dtoClass);
    }

    static <ResponseObj> ResponseObj parseFromURL(String url, Class<ResponseObj> dtoClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(fromUrlToJson(url), dtoClass);
    }

    static <ResponseObj> List<ResponseObj> parseJsonListFromResponse(HttpResponse response, CollectionType dtoClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(createStringFromResponse(response).toString(), dtoClass);
    }

    static StringBuilder createStringFromResponse(HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        return result;
    }

    static ScanSettingDTO parseScanSettingResponse(HttpResponse response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ScanSettingDTO.class, new ScanSettingDTODeserializer());
        mapper.registerModule(module);

        return mapper.readValue(createStringFromResponse(response).toString(), ScanSettingDTO.class);
    }

    static void validateClientResponse(HttpResponse response, int status, String message) throws CxValidateResponseException {
        try {
            if (response.getStatusLine().getStatusCode() != status) {
                String responseBody = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
                if (responseBody.contains("<!DOCTYPE html PUBLIC \"")) {
                    responseBody = "No error message";
                }
                throw new CxValidateResponseException(message + ": " + "status code: " + response.getStatusLine().getStatusCode() + ". Error message:" + responseBody);
            }
        } catch (IOException e) {
            throw new CxValidateResponseException("Error parse REST response body: " + e.getMessage());
        }
    }

    static void validateTokenResponse(HttpResponse response, int status, String message) throws CxValidateResponseException {
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

    //Common method to be called by SAST or OSA commands.
    static int getArmViolationExitCode(CxRestArmClient armClient, CxProviders provider, int projectId, Logger log) throws CxRestARMClientException {
        List<String> violatedPolicies = new ArrayList<>();
        int exitCode = SCAN_SUCCEEDED_EXIT_CODE;
//        String status = armClient.getPolicyStatus(projectId);
        List<Policy> policiesViolations = armClient.getProjectViolations(projectId, provider.name());
        violatedPolicies.addAll(policiesViolations.stream().map(Policy::getPolicyName).collect(Collectors.toList()));
        if (violatedPolicies.size() > 0) {
            exitCode = POLICY_VIOLATION_ERROR_EXIT_CODE;
            StringBuilder builder = new StringBuilder();
            for (String policy : violatedPolicies) {
                builder.append(policy);
                builder.append(SEPARATOR);
            }
            String commaSeparatedPolicies = builder.toString();
            //Remove last comma
            commaSeparatedPolicies = commaSeparatedPolicies.substring(0, commaSeparatedPolicies.length() - SEPARATOR.length());
            log.info("Policy status: Violated");
            log.info("Policy violations: " + violatedPolicies.size() + " - " + commaSeparatedPolicies);
        } else {
            log.info("Policy Status: Compliant");
        }
        return exitCode;
    }

    static String fromUrlToJson(String url) {
        url = url.replaceAll("=", "\":\"");
        url = url.replaceAll("&", "\",\"");
        return "{\"" + url + "\"}";
    }
}