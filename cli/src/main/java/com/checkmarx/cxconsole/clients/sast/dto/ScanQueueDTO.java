package com.checkmarx.cxconsole.clients.sast.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by nirli on 05/03/2018.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScanQueueDTO {

    private long id;

    @JsonProperty("stage")
    private StageDTO stageDTO;

    private String stageDetails;

    private String stepDetails;

    private String teamId;

    private String dateCreated;

    private String queuedOn;

    private String engineStartedOn;

    private String completedOn;

    private int loc;

    private boolean isIncremental;

    private boolean isPublic;

    private int totalPercent;

    private int stagePercent;

    private String initiator;

    public ScanQueueDTO() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public StageDTO getStageDTO() {
        return stageDTO;
    }

    public void setStageDTO(StageDTO stageDTO) {
        this.stageDTO = stageDTO;
    }

    public String getStageDetails() {
        return stageDetails;
    }

    public void setStageDetails(String stageDetails) {
        this.stageDetails = stageDetails;
    }

    public String getStepDetails() {
        return stepDetails;
    }

    public void setStepDetails(String stepDetails) {
        this.stepDetails = stepDetails;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getQueuedOn() {
        return queuedOn;
    }

    public void setQueuedOn(String queuedOn) {
        this.queuedOn = queuedOn;
    }

    public String getEngineStartedOn() {
        return engineStartedOn;
    }

    public void setEngineStartedOn(String engineStartedOn) {
        this.engineStartedOn = engineStartedOn;
    }

    public String getCompletedOn() {
        return completedOn;
    }

    public void setCompletedOn(String completedOn) {
        this.completedOn = completedOn;
    }

    public int getLoc() {
        return loc;
    }

    public void setLoc(int loc) {
        this.loc = loc;
    }

    public boolean isIncremental() {
        return isIncremental;
    }

    public void setIncremental(boolean incremental) {
        isIncremental = incremental;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public int getTotalPercent() {
        return totalPercent;
    }

    public void setTotalPercent(int totalPercent) {
        this.totalPercent = totalPercent;
    }

    public int getStagePercent() {
        return stagePercent;
    }

    public void setStagePercent(int stagePercent) {
        this.stagePercent = stagePercent;
    }

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    @Override
    public String toString() {
        return "ScanQueueDTO{" +
                "id=" + id +
                ", stageDTO=" + stageDTO +
                ", stageDetails='" + stageDetails + '\'' +
                ", stepDetails='" + stepDetails + '\'' +
                ", teamId='" + teamId + '\'' +
                ", dateCreated='" + dateCreated + '\'' +
                ", queuedOn='" + queuedOn + '\'' +
                ", engineStartedOn='" + engineStartedOn + '\'' +
                ", completedOn='" + completedOn + '\'' +
                ", loc=" + loc +
                ", isIncremental=" + isIncremental +
                ", isPublic=" + isPublic +
                ", totalPercent=" + totalPercent +
                ", stagePercent=" + stagePercent +
                ", initiator='" + initiator + '\'' +
                '}';
    }
}
