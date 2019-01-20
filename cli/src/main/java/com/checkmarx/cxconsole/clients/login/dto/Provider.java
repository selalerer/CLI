package com.checkmarx.cxconsole.clients.login.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

/**
 * Created by idanA on 1/17/2019.
 */

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Provider {
    private int id;
    private String name;
    private int providerId;
    private String providerType;
    private boolean isExternal;
    private boolean isActive;
}
