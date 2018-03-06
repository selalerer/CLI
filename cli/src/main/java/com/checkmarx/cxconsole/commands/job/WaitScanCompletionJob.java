package com.checkmarx.cxconsole.commands.job;

import com.checkmarx.cxconsole.clients.sast.CxRestSASTClient;
import com.checkmarx.cxconsole.clients.sast.dto.StageDTO;
import com.checkmarx.cxconsole.clients.sast.constants.StageValues;
import com.checkmarx.cxconsole.clients.sast.dto.ScanQueueDTO;
import com.checkmarx.cxconsole.clients.sast.exceptions.CxRestSASTClientException;
import com.checkmarx.cxconsole.commands.job.exceptions.CLIJobException;
import com.checkmarx.cxconsole.commands.job.exceptions.CLIJobUtilException;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.google.common.base.Strings;
import org.apache.log4j.Logger;

import java.util.concurrent.Callable;

class WaitScanCompletionJob implements Callable<Boolean> {

    private static Logger log = Logger.getLogger(WaitScanCompletionJob.class);

    private CxRestSASTClient cxRestSASTClient;
    private long scanId;
    private boolean isAsyncScan = false;

    WaitScanCompletionJob(CxRestSASTClient cxRestSASTClient, long scanId, boolean isAsyncScan) {
        super();
        this.cxRestSASTClient = cxRestSASTClient;
        this.scanId = scanId;
        this.isAsyncScan = isAsyncScan;
    }

    @Override
    public Boolean call() throws CLIJobException {
        int retriesNum = ConfigMgr.getCfgMgr().getIntProperty(ConfigMgr.KEY_RETIRES);
        int getStatusInterval = ConfigMgr.getCfgMgr().getIntProperty(ConfigMgr.KEY_PROGRESS_INTERVAL);

        long currTime;
        long prevTime;
        long exceededTime;
        int progressRequestAttempt = 0;
        boolean jobCompleted;

        try {
            log.trace(cxRestSASTClient.getScanQueueResponse(scanId).toString());
            do {
                currTime = System.currentTimeMillis();
                ScanQueueDTO scanQueueResponse = cxRestSASTClient.getScanQueueResponse(scanId);
                StageDTO currentStageDTO = scanQueueResponse.getStageDTO();

                if (currentStageDTO.getValue().equals(StageValues.CANCELED)) {
                    log.error("Project scan was cancelled on server side.");
                    throw new CLIJobUtilException("Project scan was cancelled on server side.");
                }

                if (currentStageDTO.getValue().equals(StageValues.FAILED)) {
                    log.error("Scan failed: " + scanQueueResponse.getStageDetails());
                    if (progressRequestAttempt > retriesNum) {
                        log.error("Performing another request. Attempt#" + progressRequestAttempt);
                        progressRequestAttempt++;
                    } else {
                        throw new CLIJobException("Scan progress request failure.");
                    }
                }

                if (isAsyncScan && currentStageDTO.getValue().equals(StageValues.QUEUED)) {
                    log.info("Current Stage: " + currentStageDTO.getValue().getServerValue());
                    return true;
                }

                log.info("Total scan worked: " + scanQueueResponse.getTotalPercent() + "%");
                String currentStageString = "Current Stage: " + currentStageDTO.getValue().getServerValue();
                if (!Strings.isNullOrEmpty(scanQueueResponse.getStageDetails())) {
                    log.info(currentStageString + " - " + scanQueueResponse.getStageDetails());
                } else {
                    log.info(currentStageString);
                }

                log.trace(scanQueueResponse.toString());
                jobCompleted = currentStageDTO.getValue().equals(StageValues.FINISHED);
                if (jobCompleted && !Strings.isNullOrEmpty(scanQueueResponse.getStageDetails())) {
                    log.info(scanQueueResponse.getStageDetails());
                    continue;
                }

                prevTime = currTime;
                currTime = System.currentTimeMillis();
                exceededTime = (currTime - prevTime) / 1000;
                //Check, maybe no need to wait, and another request should be sent
                while (exceededTime < getStatusInterval && !jobCompleted) {
                    currTime = System.currentTimeMillis();
                    exceededTime = (currTime - prevTime) / 1000;
                }
            } while (!jobCompleted);
        } catch (CxRestSASTClientException e) {
            log.error("Error occurred during retrieving scan status: " + e.getMessage());
            throw new CLIJobException("Error occurred during retrieving scan status: " + e.getMessage());
        }

        return false;
    }
}