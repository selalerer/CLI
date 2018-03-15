package com.checkmarx.cxconsole.clients.sast.constants;

/**
 * Created by nirli on 08/03/2018.
 */
public enum RemoteSourceType {

    SHARED("shared"),
    SVN("svn"),
    GIT("git"),
    TFS("tfs"),
    PERFORCE("perforce");

    private String urlValue;

    RemoteSourceType(String urlValue) {
        this.urlValue = urlValue;
    }

    public String getUrlValue() {
        return urlValue;
    }

}