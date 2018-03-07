package com.checkmarx.cxconsole.clients.sast;

import com.checkmarx.cxconsole.clients.sast.dto.*;
import com.checkmarx.cxconsole.clients.sast.exceptions.CxRestSASTClientException;

import java.io.File;
import java.util.List;

/**
 * Created by nirli on 01/03/2018.
 */
public interface CxRestSASTClient {

    List<PresetDTO> getSastPresets() throws CxRestSASTClientException;

    List<EngineConfigurationDTO> getEngineConfiguration() throws CxRestSASTClientException;

    ScanSettingDTO getProjectScanSetting(int id) throws CxRestSASTClientException;

    void createProjectScanSetting(ScanSettingDTO scanSetting) throws CxRestSASTClientException;

    void updateProjectScanSetting(ScanSettingDTO scanSetting) throws CxRestSASTClientException;

    int createNewSastScan(int projectId, boolean forceScan, boolean incrementalScan, boolean visibleOthers) throws CxRestSASTClientException;

    void updateScanComment(long scanId, String comment) throws CxRestSASTClientException;

    void uploadZipFileForSASTScan(int projectId, byte[] zipFile) throws CxRestSASTClientException;

    ScanQueueDTO getScanQueueResponse(long scanId) throws CxRestSASTClientException;

    ScanStatusDTO getScanStatus(long scanId) throws CxRestSASTClientException;

    void createSharedSourceProject(int projectId, String[] paths, String locationUser, String locationPass) throws CxRestSASTClientException;
}
