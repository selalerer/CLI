package com.checkmarx.cxconsole.clients.login.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

/**
 * Created by idanA on 1/17/2019.
 */


@JsonIgnoreProperties(ignoreUnknown = true)
public class Provider {
    private int id;
    private String name;
    private int providerId;
    private String providerType;

    private boolean isExternal;
    private boolean isActive;

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

    public int getProviderId() {
        return providerId;
    }

    public void setProviderId(int providerId) {
        this.providerId = providerId;
    }

    public String getProviderType() {
        return providerType;
    }

    public void setProviderType(String providerType) {
        this.providerType = providerType;
    }

    public boolean isExternal() {
        return isExternal;
    }

    public void setExternal(boolean external) {
        isExternal = external;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Provider provider = (Provider) o;

        if (id != provider.id) return false;
        if (providerId != provider.providerId) return false;
        if (isExternal != provider.isExternal) return false;
        if (isActive != provider.isActive) return false;
        if (name != null ? !name.equals(provider.name) : provider.name != null) return false;
        return providerType != null ? providerType.equals(provider.providerType) : provider.providerType == null;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + providerId;
        result = 31 * result + (providerType != null ? providerType.hashCode() : 0);
        result = 31 * result + (isExternal ? 1 : 0);
        result = 31 * result + (isActive ? 1 : 0);
        return result;
    }
}
