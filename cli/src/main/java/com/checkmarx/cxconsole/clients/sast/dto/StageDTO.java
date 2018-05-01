package com.checkmarx.cxconsole.clients.sast.dto;

import com.checkmarx.cxconsole.clients.sast.constants.StageValues;

/**
 * Created by nirli on 05/03/2018.
 */
public class StageDTO {

    private int id;
    private StageValues value;

    public StageDTO() {
    }

    public StageValues getValue() {
        return value;
    }

    public void setValue(StageValues value) {
        this.value = value;
    }

    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "StageDTO{" +
                "id=" + id +
                ", value=" + value +
                '}';
    }
}