package com.checkmarx.cxconsole.clients.arm.utils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by eyala on 7/9/2018.
 */
public class ArmResourceUriBuilder {

    private ArmResourceUriBuilder() {
    }

    private static final String APPLICATION_NAME = "cxarm/policymanager";

    private static final String ARM_GET_VIOLATIONS_RESOURCE = "projects/{projectId}/violations?provider={provider}";


    public static URL buildGetViolationsURL(URL serverUrl, int projectId, String provider) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + "/" + ARM_GET_VIOLATIONS_RESOURCE.replace("{projectId}", String.valueOf(projectId)).replace("{provider}", provider));
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }
}
