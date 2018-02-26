package com.checkmarx.cxconsole.clients.sast.dto;

/**
 * Created by nirli on 25/02/2018.
 */
public class EngineConfigurationDTO {

    private String id;

    private String name;

    public EngineConfigurationDTO() {
    }

    public EngineConfigurationDTO(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
