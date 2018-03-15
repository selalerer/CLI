package com.checkmarx.cxconsole.commands.utils;

import com.checkmarx.cxconsole.clients.exception.CxRestClientException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Created by nirli on 01/03/2018.
 */
public class CommandUtils {

    private static final String CX_SWAGGER = "/cxrestapi/help/swagger";

    public static String resolveServerProtocol(String originalHost) throws CxRestClientException {
        if (!originalHost.startsWith("http") && !originalHost.startsWith("https")) {
            String httpsUrl = "https://" + originalHost + CX_SWAGGER;
            if (isCxWebServiceAvailable(httpsUrl)) {
                return "https://" + originalHost;
            }

            String httpUrl = "http://" + originalHost + CX_SWAGGER;
            if (isCxWebServiceAvailable(httpUrl)) {
                return  "http://" + originalHost;
            }

            throw new CxRestClientException("Cx web service is not available in server: " + originalHost);
        } else {
            return originalHost;
        }
    }

    private static boolean isCxWebServiceAvailable(String url) {
        int responseCode;
        HttpClient client = null;
        try {
            client = new DefaultHttpClient();
            HttpGet getMethod = new HttpGet(url);
            HttpResponse response = client.execute(getMethod);
            responseCode = response.getStatusLine().getStatusCode();
        } catch (Exception e) {
            return false;
        } finally {
            HttpClientUtils.closeQuietly(client);
        }

        return (responseCode == 200);
    }

}
