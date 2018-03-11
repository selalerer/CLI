package com.checkmarx.cxconsole.clients.sast.constants;

/**
 * Created by nirli on 11/03/2018.
 */
public enum ReportStatusValue {

    DELETED("Deleted"),
    IN_PROCESS("InProcess"),
    CREATED("Created"),
    FAILED("Failed");

    private String value;

    ReportStatusValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
