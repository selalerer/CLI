package com.checkmarx.cxconsole.clients.sast.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by nirli on 05/03/2018.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScanStatusDTO {

    private class Details {

        private String stage;

        private String step;

        public Details() {
        }

        public String getStage() {
            return stage;
        }

        public void setStage(String stage) {
            this.stage = stage;
        }

        public String getStep() {
            return step;
        }

        public void setStep(String step) {
            this.step = step;
        }
    }

    private long id;

    private Details details;

    public ScanStatusDTO() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Details getDetails() {
        return details;
    }

    public void setDetails(Details details) {
        this.details = details;
    }
}
