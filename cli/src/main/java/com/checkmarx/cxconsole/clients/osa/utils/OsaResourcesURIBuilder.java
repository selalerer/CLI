package com.checkmarx.cxconsole.clients.osa.utils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by nirli on 22/02/2018.
 */
public class OsaResourcesURIBuilder {

    private OsaResourcesURIBuilder() {
        throw new IllegalStateException("Utility class");
    }

    private static final String APPLICATION_NAME = "cxrestapi";

    private static final String OSA_SCAN_STATUS_RESOURCE = "scans/{scanId}";
    private static final String OSA_SCAN_SUMMARY_RESOURCE = "osa/reports";
    private static final String OSA_CREATE_SCAN_WITH_FS = "osa/inventory";
    private static final String SCAN_ID_QUERY_PARAM = "?scanId=";

    private static final String ITEM_PER_PAGE_QUERY_PARAM = "&itemsPerPage=";

    private static final long MAX_ITEMS = 1000000;


    public static URL buildCreateOSAFSScanURL(URL serverUrl) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + "/" + OSA_CREATE_SCAN_WITH_FS);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL buildGetOSAScanStatusURL(URL serverUrl, String scanId) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + "/" + OSA_SCAN_STATUS_RESOURCE.replace("{scanId}", String.valueOf(scanId)));
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL buildGetOSAScanSummaryResultsURL(URL serverUrl, String scanId) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + "/" + OSA_SCAN_SUMMARY_RESOURCE + SCAN_ID_QUERY_PARAM + scanId);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }

    public static URL buildGetOSAScanSpecificDetailsResultsURL(URL serverUrl, String scanId, String detailsType) {
        try {
            return new URL(serverUrl, APPLICATION_NAME + "/" + "osa/" + detailsType + SCAN_ID_QUERY_PARAM + scanId + ITEM_PER_PAGE_QUERY_PARAM + MAX_ITEMS);
        } catch (MalformedURLException e) {
            return serverUrl;
        }
    }
}
