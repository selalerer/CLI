package com.checkmarx.cxconsole.clients.general.utils;

import com.checkmarx.cxconsole.clients.general.dto.ProjectDTO;
import com.checkmarx.cxconsole.clients.general.exception.CxRestGeneralClientException;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nirli on 25/02/2018.
 */
public class GeneralHttpEntityBuilder {

    private GeneralHttpEntityBuilder() {
        throw new IllegalStateException("Utility class");
    }

    private static final String ERROR_MESSAGE_PREFIX = "Failed to create body entity, due to: ";

    public static StringEntity createProjectEntity(ProjectDTO projectToCreate) throws CxRestGeneralClientException {
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("name", projectToCreate.getName()));
        urlParameters.add(new BasicNameValuePair("owningTeam", projectToCreate.getTeamId()));
        urlParameters.add(new BasicNameValuePair("isPublic", String.valueOf(projectToCreate.isPublic())));

        try {
            return new UrlEncodedFormEntity(urlParameters, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new CxRestGeneralClientException(ERROR_MESSAGE_PREFIX + e.getMessage());
        }
    }

}
