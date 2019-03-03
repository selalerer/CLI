package com.checkmarx.cxconsole.commands.utils;

import com.checkmarx.cxconsole.clients.exception.CxRestClientException;
import com.checkmarx.cxconsole.clients.utils.RestClientUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * Created by nirli on 01/03/2018.
 */
public class CommandUtils {

    private CommandUtils() {
    }

    private static final String CX_SWAGGER = "/cxrestapi/help/swagger";

    private static final boolean IS_PROXY = Boolean.parseBoolean(System.getProperty("proxySet"));
    private static final String PROXY_HOST;
    private static final String PROXY_PORT;

    static {
        PROXY_PORT = System.getProperty("http.proxyPort") == null
                ? System.getProperty("https.proxyPort")
                : System.getProperty("http.proxyPort");

        PROXY_HOST = System.getProperty("http.proxyHost") == null
                ? System.getProperty("https.proxyHost")
                : System.getProperty("http.proxyHost");
    }

    public static String resolveServerProtocol(String originalHost) throws CxRestClientException {
        String host;
        if ((originalHost.startsWith("http://") || originalHost.startsWith("https://"))) {
            if (isCxWebServiceAvailable(originalHost + CX_SWAGGER)) {
                return originalHost;
            }
        }
        host = "http://" + originalHost;
        if (isCxWebServiceAvailable(host + CX_SWAGGER)) {
            return host;
        }

        host = "https://" + originalHost;
        if (isCxWebServiceAvailable(host + CX_SWAGGER)) {
            return host;
        }

        throw new CxRestClientException("Cx web service is not available at: " + originalHost);
    }

    private static boolean isCxWebServiceAvailable(String url) {
        int responseCode;
        HttpClient client = null;
        try {
            final HttpClientBuilder clientBuilder = HttpClientBuilder.create();
            if (IS_PROXY) {
                RestClientUtils.setClientProxy(clientBuilder, PROXY_HOST, Integer.parseInt(PROXY_PORT));
            }
            client = clientBuilder.build();
            HttpGet getMethod = new HttpGet(url);
            HttpResponse response = client.execute(getMethod);
            responseCode = response.getStatusLine().getStatusCode();
        } catch (Exception e) {
            return false;
        } finally {
            HttpClientUtils.closeQuietly(client);
        }

        return responseCode == 200;
    }

}
