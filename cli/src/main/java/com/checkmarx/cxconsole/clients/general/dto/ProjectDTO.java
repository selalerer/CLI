package com.checkmarx.cxconsole.clients.general.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by nirli on 26/02/2018.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDTO {

    private int id;

    private String name;

    private String teamId;

    private boolean isPublic;

    public ProjectDTO() {
    }

    public ProjectDTO(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }
}
