package com.checkmarx.cxconsole.clients.general.dto;

/**
 * Created by eyala on 7/12/2018.
 */
public enum CxProviders {
    OPEN_SOURCE("openSource"),
    SAST("sast"); //todo
    private String value;

    public String value() {
        return value;
    }

    CxProviders(String value) {
        this.value = value;
    }
}
