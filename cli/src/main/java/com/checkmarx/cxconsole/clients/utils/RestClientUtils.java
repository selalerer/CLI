package com.checkmarx.cxconsole.clients.utils;

import com.checkmarx.cxconsole.clients.login.exceptions.CxRestLoginClientException;
import com.checkmarx.cxconsole.clients.login.jwt.dto.JwtAccessTokenDto;
import com.checkmarx.cxconsole.clients.login.jwt.exceptions.JWTException;
import com.checkmarx.cxconsole.clients.login.jwt.utils.JwtUtils;
import com.checkmarx.cxconsole.clientsold.rest.exceptions.CxRestClientValidatorException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by nirli on 20/02/2018.
 */
public class RestClientUtils {

    private static Logger log = Logger.getLogger(RestClientUtils.class);


    public static <ResponseObj> ResponseObj parseJsonFromString(String jsonInString, Class<ResponseObj> dtoClass) throws JWTException {
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readValue(jsonInString, dtoClass);
        } catch (IOException e) {
            throw new JWTException("Can't convert string into JSON");
        }
    }

    public static <ResponseObj> ResponseObj parseJsonFromResponse(HttpResponse generateTokenResponse, Class<ResponseObj> dtoClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        BufferedReader rd = new BufferedReader(new InputStreamReader(generateTokenResponse.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        return mapper.readValue(result.toString(), dtoClass);
    }

    public static <ResponseObj> List<ResponseObj> parseJsonListFromResponse(HttpResponse generateTokenResponse, CollectionType dtoClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        BufferedReader rd = new BufferedReader(new InputStreamReader(generateTokenResponse.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        return mapper.readValue(result.toString(), dtoClass);
    }

    public static void validateLoginResponse(HttpResponse response, int status, String message) throws CxRestClientValidatorException {
        try {
            if (response.getStatusLine().getStatusCode() != status) {
                String responseBody = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
                responseBody = responseBody.replace("{", "").replace("}", "").replace(System.getProperty("line.separator"), " ").replace("  ", "");
                if (responseBody.contains("<!DOCTYPE html>")) {
                    throw new CxRestClientValidatorException(message + ": " + "status code: 500. Error message: Internal Server Error");
                } else if (responseBody.contains("\"error\":\"invalid_grant\"")) {
                    throw new CxRestClientValidatorException(message);
                } else {
                    throw new CxRestClientValidatorException(message + ": " + "status code: " + response.getStatusLine() + ". Error message:" + responseBody);
                }
            }
        } catch (IOException e) {
            throw new CxRestClientValidatorException("Error parse REST response body: " + e.getMessage());
        }
    }

    private static String getPayloadSectionFromAccessJWT(String accessJWT) throws JWTException {
        String[] accessJWTDividedToSection = accessJWT.split("\\.");
        if (accessJWTDividedToSection.length != 3) {
            throw new JWTException("Access token is incomplete");
        }

        return accessJWTDividedToSection[1];
    }

    public static String getSessionIdFromAccessToken(String accessToken) throws CxRestLoginClientException {
        JwtAccessTokenDto jwtAccessTokenDto;
        try {
            String payload = RestClientUtils.getPayloadSectionFromAccessJWT(accessToken);
            String decodedPayload = JwtUtils.convertBase64ToString(payload);
            jwtAccessTokenDto = RestClientUtils.parseJsonFromString(decodedPayload, JwtAccessTokenDto.class);
        } catch (JWTException e) {
            log.error("Failed to get session ID from token: " + e.getMessage());
            throw new CxRestLoginClientException("Failed to get session ID from token: " + e.getMessage());
        }

        return jwtAccessTokenDto.getSessionId();
    }
}
