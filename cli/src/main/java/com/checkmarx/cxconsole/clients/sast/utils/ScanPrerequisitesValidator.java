package com.checkmarx.cxconsole.clients.sast.utils;

import com.checkmarx.cxconsole.clients.sast.client.CxRestSASTClient;
import com.checkmarx.cxconsole.clients.sast.dto.EngineConfigurationDTO;
import com.checkmarx.cxconsole.clients.sast.dto.PresetDTO;
import com.checkmarx.cxconsole.clients.sast.dto.TeamDTO;
import com.checkmarx.cxconsole.clients.sast.exceptions.CxRestSASTClientException;
import com.checkmarx.cxconsole.clientsold.rest.exceptions.CxRestClientValidatorException;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by nirli on 26/02/2018.
 */
public class ScanPrerequisitesValidator {

    private static Logger log = Logger.getLogger(ScanPrerequisitesValidator.class);

    private final List<TeamDTO> teams;
    private final TeamDTO teamInput;
    private final List<EngineConfigurationDTO> engineConfigurations;
    private final EngineConfigurationDTO engineConfigurationInput;
    private final List<PresetDTO> presets;
    private final PresetDTO presetInput;

    public ScanPrerequisitesValidator(CxRestSASTClient cxRestSASTClient, TeamDTO teamInput, EngineConfigurationDTO engineConfigurationInput,
                                      PresetDTO presetInput) throws CxRestSASTClientException, CxRestClientValidatorException {
        teams = cxRestSASTClient.getTeams();
        engineConfigurations = cxRestSASTClient.getEngineConfiguration();
        presets = cxRestSASTClient.getSastPresets();
        this.teamInput = teamInput;
        this.engineConfigurationInput = engineConfigurationInput;
        this.presetInput = presetInput;

        validateScanPrerequisites();
    }

    private void validateScanPrerequisites() {
        validateScanTeam();
        validateScanPreset();
        validateScanEngineConfiguration();
    }

    private void validateScanEngineConfiguration() {
        for (EngineConfigurationDTO engineConfiguration : engineConfigurations) {
            if (engineConfiguration.getName().equalsIgnoreCase(engineConfigurationInput.getName())) {
                engineConfigurationInput.setId(engineConfiguration.getId());
            }
        }

    }

    private void validateScanPreset() {

    }

    private void validateScanTeam() {

    }


}
