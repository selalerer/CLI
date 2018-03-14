package com.checkmarx.cxconsole.clients.general.utils;

import com.checkmarx.cxconsole.clients.general.dto.ProjectDTO;
import com.checkmarx.cxconsole.clients.general.exception.CxRestGeneralClientException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nirli on 25/02/2018.
 */
public class GeneralHttpEntityBuilder {

    private GeneralHttpEntityBuilder() {
        throw new IllegalStateException("Utility class");
    }

    public static StringEntity createProjectEntity(ProjectDTO projectToCreate) throws CxRestGeneralClientException {
        Map<String, String> content = new HashMap<>();
        content.put("name", projectToCreate.getName());
        content.put("owningTeam", projectToCreate.getTeamId());
        content.put("isPublic", "true");

        JSONObject jsonObject = new JSONObject(content);
        return new StringEntity(jsonObject.toString(), ContentType.APPLICATION_JSON);
    }
}