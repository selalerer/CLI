package com.checkmarx.cxconsole.clients.sast.dto;

/**
 * Created by nirli on 08/03/2018.
 */
public class PerforceScanSettingDTO extends SVNAndTFSScanSettingDTO {

    private String browseMode;

    public PerforceScanSettingDTO(String userName, String password, String[] paths, String absoluteUrl, int port,
                                  String privateKey, String browseMode) {
        super(userName, password, paths, absoluteUrl, port, privateKey);
        this.browseMode = browseMode;
    }

    public String getBrowseMode() {
        return browseMode;
    }

    public void setBrowseMode(String browseMode) {
        this.browseMode = browseMode;
    }
}
