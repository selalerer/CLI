package com.checkmarx.cxconsole.clients.general.dto;

/**
 * Created by nirli on 25/02/2018.
 */
public class TeamDTO {

    private String id;

    private String fullName;

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
}
