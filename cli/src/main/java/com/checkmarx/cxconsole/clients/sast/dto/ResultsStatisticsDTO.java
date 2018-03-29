package com.checkmarx.cxconsole.clients.sast.dto;

/**
 * Created by nirli on 29/03/2018.
 */
public class ResultsStatisticsDTO {
    private int highSeverity;

    private int mediumSeverity;

    private int lowSeverity;

    private int infoSeverity;

    private String statisticsCalculationDate;

    public ResultsStatisticsDTO() {
    }

    public int getHighSeverity() {
        return highSeverity;
    }

    public void setHighSeverity(int highSeverity) {
        this.highSeverity = highSeverity;
    }

    public int getMediumSeverity() {
        return mediumSeverity;
    }

    public void setMediumSeverity(int mediumSeverity) {
        this.mediumSeverity = mediumSeverity;
    }

    public int getLowSeverity() {
        return lowSeverity;
    }

    public void setLowSeverity(int lowSeverity) {
        this.lowSeverity = lowSeverity;
    }

    public int getInfoSeverity() {
        return infoSeverity;
    }

    public void setInfoSeverity(int infoSeverity) {
        this.infoSeverity = infoSeverity;
    }

    public String getStatisticsCalculationDate() {
        return statisticsCalculationDate;
    }

    public void setStatisticsCalculationDate(String statisticsCalculationDate) {
        this.statisticsCalculationDate = statisticsCalculationDate;
    }
}
