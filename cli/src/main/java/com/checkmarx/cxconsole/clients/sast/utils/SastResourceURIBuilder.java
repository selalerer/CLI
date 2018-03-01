package com.checkmarx.cxconsole.clients.sast.utils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by nirli on 25/02/2018.
 */
public class SastResourceURIBuilder {

    private SastResourceURIBuilder() {
        throw new IllegalStateException("Utility class");
    }

    private static final String APPLICATION_NAME = "cxrestapi";

    private static final String PRESET_RESOURCE = "/sast/presets";
    private static final String ENGINE_CONFIGURATION_RESOURCE = "/sast/engineConfigurations";
    private static final String SCAN_SETTING_RESOURCE = "/sast/scanSettings";
    private static final String SYSTEM_CONFIGURATION_RESOURCE = "/Configurations/systemSettings";

    public static URL buildGetSastPresetsURL(URL serverUrl) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + PRESET_RESOURCE);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL buildGetEngineConfigurationURL(URL serverUrl) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + ENGINE_CONFIGURATION_RESOURCE);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL buildGetSASTScanSettingURL(URL serverUrl, int projectId) {
        try {
            return new URL(buildSASTScanSettingURL(serverUrl) +  "/" + projectId);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL buildSASTScanSettingURL(URL serverUrl) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + SCAN_SETTING_RESOURCE );
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }


}
