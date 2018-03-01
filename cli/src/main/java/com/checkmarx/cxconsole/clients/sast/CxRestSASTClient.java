package com.checkmarx.cxconsole.clients.sast;

import com.checkmarx.cxconsole.clients.sast.dto.EngineConfigurationDTO;
import com.checkmarx.cxconsole.clients.sast.dto.PresetDTO;
import com.checkmarx.cxconsole.clients.sast.dto.ScanSettingDTO;
import com.checkmarx.cxconsole.clients.sast.exceptions.CxRestSASTClientException;

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
}
