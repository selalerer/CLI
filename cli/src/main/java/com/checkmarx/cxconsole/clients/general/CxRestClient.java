package com.checkmarx.cxconsole.clients.general;

import com.checkmarx.cxconsole.clients.arm.dto.CxArmConfig;
import com.checkmarx.cxconsole.clients.exception.CxValidateResponseException;
import com.checkmarx.cxconsole.clients.osa.CxRestOSAClientImpl;
import com.checkmarx.cxconsole.clients.osa.exceptions.CxRestOSAClientException;
import com.checkmarx.cxconsole.clients.osa.utils.OsaResourcesURIBuilder;
import com.checkmarx.cxconsole.clients.utils.RestClientUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;

import static com.checkmarx.cxconsole.clients.utils.RestClientUtils.parseJsonFromResponse;

/**
 * Created by idanA on 12/19/2018.
 */

public interface CxRestClient {

    Header CLI_ACCEPT_HEADER_AND_VERSION_HEADER = new BasicHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType() + ";v=1.0");
    Logger log = Logger.getLogger(CxRestClient.class);

    default CxArmConfig getPolicyConfig(HttpClient client, String hostName) throws CxRestOSAClientException {
        HttpUriRequest getRequest;
        HttpResponse response = null;

        try {
            getRequest = RequestBuilder.get()
                    .setUri(String.valueOf(OsaResourcesURIBuilder.buildGetCxArmConfigurationURL(new URL(hostName))))
                    .setHeader(CLI_ACCEPT_HEADER_AND_VERSION_HEADER)
                    .build();
            response = client.execute(getRequest);

            RestClientUtils.validateClientResponse(response, 200, "fail get CXARM configuration");
            return parseJsonFromResponse(response, CxArmConfig.class);
        } catch (IOException | CxValidateResponseException e) {
            log.error("Failed to get CXARM configuration: " + e.getMessage());
            throw new CxRestOSAClientException("Failed to get CXARM configuration: " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }
}
