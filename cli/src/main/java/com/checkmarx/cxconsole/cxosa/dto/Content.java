package com.checkmarx.cxconsole.cxosa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;


/**
 * Created by nirli on 06/02/2018.
 */
class Content {

    @JsonRawValue
    @JsonProperty("projects")
    private String projects;

    Content(String projects) {
        this.projects = projects;
    }

    public String getProjects() {
        return projects;
    }

    public void setProjects(String projects) {
        this.projects = projects;
    }
}