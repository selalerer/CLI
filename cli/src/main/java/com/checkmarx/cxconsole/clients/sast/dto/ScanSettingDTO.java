package com.checkmarx.cxconsole.clients.sast.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Created by nirli on 28/02/2018.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScanSettingDTO {

    static class EmailNotificationsDTO {

        private List<String> failedScan;

        private List<String> beforeScan;

        private List<String> afterScan;

        public EmailNotificationsDTO() {
        }

        public List<String> getFailedScan() {
            return failedScan;
        }

        public void setFailedScan(List<String> failedScan) {
            this.failedScan = failedScan;
        }

        public List<String> getBeforeScan() {
            return beforeScan;
        }

        public void setBeforeScan(List<String> beforeScan) {
            this.beforeScan = beforeScan;
        }

        public List<String> getAfterScan() {
            return afterScan;
        }

        public void setAfterScan(List<String> afterScan) {
            this.afterScan = afterScan;
        }
    }

    private int projectId;

    private int presetId;

    private int engineConfigurationId;

    private int postScanActionId;

    private EmailNotificationsDTO emailNotifications;

    public ScanSettingDTO() {
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getPresetId() {
        return presetId;
    }

    public void setPresetId(int presetId) {
        this.presetId = presetId;
    }

    public int getEngineConfigurationId() {
        return engineConfigurationId;
    }

    public void setEngineConfigurationId(int engineConfigurationId) {
        this.engineConfigurationId = engineConfigurationId;
    }

    public int getPostScanActionId() {
        return postScanActionId;
    }

    public void setPostScanActionId(Integer postScanActionId) {
        this.postScanActionId = postScanActionId;
    }

    public EmailNotificationsDTO getEmailNotifications() {
        return emailNotifications;
    }

    public void setEmailNotifications(EmailNotificationsDTO emailNotifications) {
        this.emailNotifications = emailNotifications;
    }
}
