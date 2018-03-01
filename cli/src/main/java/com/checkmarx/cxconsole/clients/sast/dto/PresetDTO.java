package com.checkmarx.cxconsole.clients.sast.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by nirli on 25/02/2018.
 */
public class PresetDTO {

    class presetLink {

        private String rel;

        private String uri;

        public presetLink() {
        }

        public String getRel() {
            return rel;
        }

        public void setRel(String rel) {
            this.rel = rel;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }
    }

    private int id;

    private String name;

    private String ownerName;

    @JsonProperty("link")
    private presetLink link;

    public PresetDTO() {
    }

    public PresetDTO(String name) {
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

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public presetLink getLink() {
        return link;
    }

    public void setLink(presetLink link) {
        this.link = link;
    }
}
