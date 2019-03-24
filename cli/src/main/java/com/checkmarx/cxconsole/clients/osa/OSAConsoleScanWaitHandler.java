package com.checkmarx.cxconsole.clients.osa;


import com.checkmarx.cxconsole.clients.osa.dto.OSAScanStatus;
import com.checkmarx.cxconsole.clients.osa.dto.OSAScanStatusEnum;
import com.checkmarx.cxconsole.clients.osa.dto.ScanState;
import com.checkmarx.cxconsole.clients.osa.exceptions.CxRestOSAClientException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import static com.checkmarx.cxconsole.clients.osa.dto.OSAScanStatusEnum.QUEUED;


/**
 * Created by: Dorg.
 * Date: 28/09/2016.
 */
public class OSAConsoleScanWaitHandler implements ScanWaitHandler<OSAScanStatus> {

    private static Logger log = Logger.getLogger(OSAConsoleScanWaitHandler.class);
    private long startTime;
    private long scanTimeoutInMin;

    public void onTimeout(OSAScanStatus scanStatus) throws CxRestOSAClientException {

        String status = scanStatus.getStatus() == null ? OSAScanStatusEnum.NONE.uiValue() : scanStatus.getStatus().getValue();
        throw new CxRestOSAClientException("OSA scan has reached the time limit (" + scanTimeoutInMin + " minutes). status: [" + status + "]");

    }

    public void onFail(OSAScanStatus scanStatus) throws CxRestOSAClientException {
        throw new CxRestOSAClientException("OSA scan cannot be completed. status [" + scanStatus.getStatus().getValue() + "]. message: [" + StringUtils.defaultString(scanStatus.getMessage()) + "]");

    }

    public void onIdle(OSAScanStatus scanStatus) {
        long hours = (System.currentTimeMillis() - startTime) / 3600000;
        long minutes = ((System.currentTimeMillis() - startTime) % 3600000) / 60000;
        long seconds = ((System.currentTimeMillis() - startTime) % 60000) / 1000;

        String hoursStr = (hours < 10) ? ("0" + Long.toString(hours)) : (Long.toString(hours));
        String minutesStr = (minutes < 10) ? ("0" + Long.toString(minutes)) : (Long.toString(minutes));
        String secondsStr = (seconds < 10) ? ("0" + Long.toString(seconds)) : (Long.toString(seconds));

        log.info("Waiting for OSA Scan Results. " +
                "Time Elapsed: " + hoursStr + ":" + minutesStr + ":" + secondsStr + ". " +
                "Status: " + scanStatus.getStatus().getValue());

    }

    public void onSuccess(OSAScanStatus scanStatus) {
        log.debug("OSA Scan Finished.");
    }

    public void onQueued(OSAScanStatus scanStatus) {
        log.debug("OSA Scan Queued.");
        scanStatus.setStatus(new ScanState(QUEUED.getNum(), QUEUED.uiValue()));
        scanStatus.setLink(null);
        scanStatus.setMessage("Osa scan queued");
    }

    public void onStart(long startTime, long scanTimeoutInMin) {
        this.startTime = startTime;
        this.scanTimeoutInMin = scanTimeoutInMin;
    }

}
