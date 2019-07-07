package com.checkmarx.cxconsole.clients.osa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by: Dorg.
 * Date: 06/10/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)

public class OSAScanStatus {

    private ScanState status;
    private String message;
    private String link;

    public ScanState getStatus() {
        return status;
    }

    public OSAScanStatusEnum getStatusAsEnum() {
        for (OSAScanStatusEnum state : OSAScanStatusEnum.values()) {
            if (state.getNum() == (this.status.getId())) {
                status.setValue(state.uiValue()); //TODO: for backward compatibility, can be removed in 9.3
                return state;
            }
        }
        return OSAScanStatusEnum.NONE;
    }

    public void setStatus(ScanState status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }


}
