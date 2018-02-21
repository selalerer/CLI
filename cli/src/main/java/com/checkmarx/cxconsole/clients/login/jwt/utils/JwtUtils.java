package com.checkmarx.cxconsole.clients.login.jwt.utils;

import com.checkmarx.cxconsole.clients.login.jwt.exceptions.JWTException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by nirli on 16/10/2017.
 */
public class JwtUtils {

    private JwtUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String convertBase64ToString(String base64String) throws JWTException {
        byte[] decoded = Base64.decodeBase64(base64String);

        String decodedString;
        try {
            decodedString = new String(decoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new JWTException("Can't decode base64 to String");
        }

        return decodedString;
    }


}