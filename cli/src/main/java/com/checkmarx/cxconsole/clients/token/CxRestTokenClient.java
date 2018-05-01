package com.checkmarx.cxconsole.clients.token;

import com.checkmarx.cxconsole.clients.exception.CxRestClientException;

import java.net.URL;

/**
 * Created by nirli on 14/03/2018.
 */
public interface CxRestTokenClient {

    String generateToken(URL serverUrl, String userName, String password) throws CxRestClientException;

    void revokeToken(URL serverUrl, String token) throws CxRestClientException;
}
