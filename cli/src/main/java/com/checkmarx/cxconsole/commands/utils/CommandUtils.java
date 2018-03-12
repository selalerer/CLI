package com.checkmarx.cxconsole.commands.utils;

import com.checkmarx.cxconsole.clients.exception.CxRestClientException;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by nirli on 01/03/2018.
 */
public class CommandUtils {

    private static final int TIMEOUT_FOR_CX_SERVER_AVAILABILITY = 250;
    private static final String CX_CLI_WEB_SERVICE_URL = "/cxwebinterface/CLI/CxCLIWebServiceV1.asmx";

    public static String resolveServerProtocol(String originalHost) throws CxRestClientException {
        if (!originalHost.startsWith("http") && !originalHost.startsWith("https")) {
            String httpsProtocol = "https://" + originalHost;
            if (isCxWebServiceAvailable(httpsProtocol)) {
                return httpsProtocol;
            }

            String httpProtocol = "http://" + originalHost;
            if (isCxWebServiceAvailable(httpProtocol)) {
                return httpProtocol;
            }

            throw new CxRestClientException("Cx web service is not available in server: " + originalHost);
        } else {
            return originalHost;
        }
    }

    private static boolean isCxWebServiceAvailable(String host) {
        int responseCode;
        try {
            URL urlAddress = new URL(buildHostWithWSDL(host));
            HttpURLConnection httpConnection = (HttpURLConnection) urlAddress.openConnection();
            httpConnection.setRequestMethod("GET");
            httpConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
            httpConnection.setConnectTimeout(TIMEOUT_FOR_CX_SERVER_AVAILABILITY);
            responseCode = httpConnection.getResponseCode();
        } catch (Exception e) {
            return false;
        }

        return (responseCode == 404);
    }

    public static void validateResponse() throws CxRestClientException {
    }

    public static String buildHostWithWSDL(String host) {
        return host + CX_CLI_WEB_SERVICE_URL;
    }
}
