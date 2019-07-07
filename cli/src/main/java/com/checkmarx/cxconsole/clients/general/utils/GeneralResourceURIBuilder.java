package com.checkmarx.cxconsole.clients.general.utils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by nirli on 25/02/2018.
 */
public class GeneralResourceURIBuilder {

    private GeneralResourceURIBuilder() {
        throw new IllegalStateException("Utility class");
    }

    private static final String APPLICATION_NAME = "cxrestapi";

    private static final String TEAMS_RESOURCE = "/auth/teams";
    private static final String PROJECTS_RESOURCE = "/projects";
    private static final String CX_VERSION = "/system/version";

    public static URL buildGetTeamsURL(URL serverUrl) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + TEAMS_RESOURCE);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL buildProjectsURL(URL serverUrl) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + PROJECTS_RESOURCE);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL buildGetCxVersion(URL serverUrl){
        try {
            return new URL(serverUrl, APPLICATION_NAME + CX_VERSION);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }


}
