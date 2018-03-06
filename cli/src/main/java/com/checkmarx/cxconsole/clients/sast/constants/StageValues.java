package com.checkmarx.cxconsole.clients.sast.constants;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created by nirli on 05/03/2018.
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StageValues {

    NEW("New"),
    PRE_SCAN("PreScan"),
    QUEUED("Queued"),
    SCANNING("Scanning"),
    POST_SCAN("PostScan"),
    FINISHED("Finished"),
    CANCELED("Canceled"),
    FAILED("Failed"),
    SOURCE_PULLING_AND_DEPLOYMENT("SourcePullingAndDeployment"),
    NONE("None");

    private String value;

    StageValues(String value) {
        this.value = value;
    }

    @JsonValue
    public String getServerValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
