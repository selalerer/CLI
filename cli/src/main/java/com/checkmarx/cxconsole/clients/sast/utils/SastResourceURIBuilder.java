package com.checkmarx.cxconsole.clients.sast.utils;

import com.checkmarx.cxconsole.clients.sast.constants.RemoteSourceType;

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

    private static final String PROJECTS_RESOURCE = "/projects";
    private static final String SOURCECODE_RESOURCE = "/sourceCode";
    private static final String ZIP_FILE_ATTACHMENT_RESOURCE = "/attachments";

    private static final String PRESET_RESOURCE = "/sast/presets";
    private static final String ENGINE_CONFIGURATION_RESOURCE = "/sast/engineConfigurations";
    private static final String SCAN_SETTING_RESOURCE = "/sast/scanSettings";
    private static final String SAST_SCAN_RESOURCE = "/sast/scans";
    private static final String SAST_ADD_COMMENT_RESOURCE = "/comment";
    private static final String SAST_SCAN_QUEUE_RESOURCE = "/sast/scansQueue";
    private static final String SAST_SOURCE_CODE_RESOURCE = "/sourceCode";
    private static final String SAST_REMOTE_SOURCE_CODE_RESOURCE = "/sourceCode/remoteSettings";
    private static final String EXCLUSION_SETTING_RESOURCE = "excludeSettings";
    private static final String REPORTS_RESOURCE = "/reports/sastScan";

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
            return new URL(buildSASTScanSettingURL(serverUrl) + "/" + projectId);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL buildSASTScanSettingURL(URL serverUrl) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + SCAN_SETTING_RESOURCE);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }


    public static URL buildCreateNewSastScanURL(URL serverUrl) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + SAST_SCAN_RESOURCE);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL buildAddSastCommentURL(URL serverUrl, long scanId) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + SAST_SCAN_RESOURCE + "/" + scanId);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL buildUploadZipFileURL(URL serverUrl, int projectId) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + PROJECTS_RESOURCE + "/" + projectId + SOURCECODE_RESOURCE + ZIP_FILE_ATTACHMENT_RESOURCE);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL buildGetSASTScanQueueResponseURL(URL serverUrl, long scanId) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + SAST_SCAN_QUEUE_RESOURCE + "/" + scanId);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL buildGetSASTScanStatusURL(URL serverUrl, long scanId) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + SAST_SCAN_RESOURCE + "/" + scanId);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL buildCreateRemoteSourceScanURL(URL serverUrl, int projectId, RemoteSourceType remoteSourceType, boolean isSSH) {
        try {
            if (!isSSH) {
                return new URL(serverUrl, APPLICATION_NAME + PROJECTS_RESOURCE + "/" + projectId + SAST_REMOTE_SOURCE_CODE_RESOURCE + "/" + remoteSourceType.getUrlValue());
            } else {
                return new URL(serverUrl, APPLICATION_NAME + PROJECTS_RESOURCE + "/" + projectId + SAST_REMOTE_SOURCE_CODE_RESOURCE + "/" + remoteSourceType.getUrlValue() + "/ssh");
            }
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL buildSASTScanExclusionSettingURL(URL serverUrl, int projectId) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + PROJECTS_RESOURCE + "/" + projectId + SAST_SOURCE_CODE_RESOURCE + "/" + EXCLUSION_SETTING_RESOURCE);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL buildCreateReportURL(URL serverUrl) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + REPORTS_RESOURCE);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL buildGetReportStatusURL(URL serverUrl, int reportId) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + REPORTS_RESOURCE + "/" + reportId + "/status");
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL buildGetReportFileURL(URL serverUrl, int reportId) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + REPORTS_RESOURCE + "/" + reportId);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }
}
