package com.checkmarx.cxconsole.clients.osa.utils;

import com.checkmarx.cxconsole.clients.osa.dto.CreateOSAScanRequest;
import com.checkmarx.cxconsole.clientsold.rest.exceptions.CxRestClientException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

/**
 * Created by nirli on 21/02/2018.
 */
public class OsaHttpEntityBuilder {

    private OsaHttpEntityBuilder() {
        throw new IllegalStateException("Utility class");
    }

    private static final String ERROR_MESSAGE_PREFIX = "Failed to create body entity, due to: ";

    public static StringEntity createOsaFSAEntity(CreateOSAScanRequest osaScanRequest) throws CxRestClientException {
        ObjectMapper mapper = new ObjectMapper();
        String osaScanRequestStr;
        try {
            osaScanRequestStr = mapper.writeValueAsString(osaScanRequest);
            return new StringEntity(osaScanRequestStr, ContentType.APPLICATION_JSON);
        } catch (JsonProcessingException e) {
            throw new CxRestClientException(ERROR_MESSAGE_PREFIX + e.getMessage());
        }

    }
}
