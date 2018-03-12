package com.checkmarx.cxconsole.clients.sast.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nirli on 11/03/2018.
 */
public enum ReportStatusValue {

    DELETED("Deleted"),
    IN_PROCESS("InProcess"),
    CREATED("Created"),
    FAILED("Failed");

    private String serverValue;
    private static Map<String, ReportStatusValue> map = new HashMap<>();

    static {
        for (ReportStatusValue reportStatusValue : ReportStatusValue.values()) {
            map.put(reportStatusValue.serverValue, reportStatusValue);
        }
    }


    private ReportStatusValue(final String value) {
        serverValue = value;
    }


    public static ReportStatusValue getServerValue(String reportValueStatus) {
        return map.get(reportValueStatus);
    }
}
