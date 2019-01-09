package com.checkmarx.cxconsole.clients.general.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by nirli on 25/02/2018.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamDTO {

    private String id;

    private String fullName;

    private String name;

    private String parentId;

    public TeamDTO() {
    }

    public TeamDTO(String fullName) {
        this.fullName = fullName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
