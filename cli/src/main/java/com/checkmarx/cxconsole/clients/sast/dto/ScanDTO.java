package com.checkmarx.cxconsole.clients.sast.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by nirli on 05/03/2018.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScanDTO {

    private class Details {

        private String stage;

        private String step;

        public Details() {
        }

        public String getStage() {
            return stage;
        }

        public void setStage(String stage) {
            this.stage = stage;
        }

        public String getStep() {
            return step;
        }

        public void setStep(String step) {
            this.step = step;
        }
    }

    public class ResultsStatistics {
        private int highSeverity;

        private int mediumSeverity;

        private int lowSeverity;

        private int infoSeverity;

        private String statisticsCalculationDate;

        public ResultsStatistics() {
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

    private long id;

    private Details details;

    private ResultsStatistics resultsStatistics;

    public ScanDTO() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Details getDetails() {
        return details;
    }

    public void setDetails(Details details) {
        this.details = details;
    }

    public ResultsStatistics getResultsStatistics() {
        return resultsStatistics;
    }

    public void setResultsStatistics(ResultsStatistics resultsStatistics) {
        this.resultsStatistics = resultsStatistics;
    }
}
