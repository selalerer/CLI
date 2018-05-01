package com.checkmarx.cxconsole.clients.sast.constants;

/**
 * Created by nirli on 11/03/2018.
 */
public enum ReportType {

    XML("xml"),
    PDF("pdf"),
    CSV("csv"),
    RTF("rtf");

    private String value;

    ReportType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}