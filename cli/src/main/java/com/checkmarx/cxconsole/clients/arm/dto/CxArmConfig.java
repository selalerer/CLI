package com.checkmarx.cxconsole.clients.arm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by eyala on 7/25/2018.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CxArmConfig {

    private boolean isOsaEulaAccepted;
    private String cxARMPolicyURL;

    public boolean getIsOsaEulaAccepted() {
        return isOsaEulaAccepted;
    }

    public void setIsOsaEulaAccepted(boolean osaEulaAccepted) {
        isOsaEulaAccepted = osaEulaAccepted;
    }

    public String getCxARMPolicyURL() {
        return cxARMPolicyURL;
    }

    public void setCxARMPolicyURL(String cxARMPolicyURL) {
        this.cxARMPolicyURL = cxARMPolicyURL;
    }
}
