package com.checkmarx.cxconsole.clients.osa.dto;

/**
 * Created by idanA on 3/24/2019.
 */
public class ScanState {
    private int id;
    private String value;
    private String failureReason;

    public ScanState() {
    }

    public ScanState(int id, String value) {
        this.id = id;
        this.value = value;
    }

    public ScanState(int id, String value, String failureReason) {
        this.id = id;
        this.value = value;
        this.failureReason = failureReason;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
