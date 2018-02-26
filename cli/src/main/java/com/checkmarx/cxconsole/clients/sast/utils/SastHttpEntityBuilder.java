package com.checkmarx.cxconsole.clients.sast.utils;

import com.checkmarx.cxconsole.clientsold.rest.exceptions.CxRestClientException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

/**
 * Created by nirli on 25/02/2018.
 */
public class SastHttpEntityBuilder {

    private SastHttpEntityBuilder() {
        throw new IllegalStateException("Utility class");
    }

    private static final String DEFAULT_API_VERSION = ";v=1.0";

    private static final String ERROR_MESSAGE_PREFIX = "Failed to create body entity, due to: ";

    public static StringEntity createGetTeamsEntity() throws CxRestClientException {
        String sastGetTeamsEntity = null;
        return new StringEntity(sastGetTeamsEntity, ContentType.APPLICATION_JSON + DEFAULT_API_VERSION);
    }

}
