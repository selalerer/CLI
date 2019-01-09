package com.checkmarx.cxconsole.clients.general.utils;

import com.checkmarx.cxconsole.clients.general.CxRestGeneralClient;
import com.checkmarx.cxconsole.clients.general.dto.ProjectDTO;
import com.checkmarx.cxconsole.clients.general.dto.TeamDTO;
import com.checkmarx.cxconsole.clients.general.exception.CxRestGeneralClientException;
import com.checkmarx.cxconsole.clients.general.exception.CxScanPrerequisitesValidatorException;
import com.checkmarx.cxconsole.clients.sast.CxRestSASTClient;
import com.checkmarx.cxconsole.clients.sast.dto.EngineConfigurationDTO;
import com.checkmarx.cxconsole.clients.sast.dto.PresetDTO;
import com.checkmarx.cxconsole.clients.sast.exceptions.CxRestSASTClientException;
import com.google.common.base.Strings;
import org.apache.log4j.Logger;

import java.util.List;

import static com.checkmarx.cxconsole.exitcodes.Constants.ErrorMassages.NO_PROJECT_PRIOR_TO_OSA_SCAN_ERROR_MSG;

/**
 * Created by nirli on 26/02/2018.
 */
public class ScanPrerequisitesValidator {

    private static Logger log = Logger.getLogger(ScanPrerequisitesValidator.class);
    private static final int UNASSIGNED_VALUE = 0;

    private final TeamDTO teamInput;
    private final EngineConfigurationDTO engineConfigurationInput;
    private final PresetDTO presetInput;
    private final ProjectDTO projectInput;
    private final CxRestGeneralClient cxRestGeneralClient;
    private final CxRestSASTClient cxRestSASTClient;
    private boolean isProjectExists = false;

    public ScanPrerequisitesValidator(CxRestGeneralClient cxRestGeneralClient, CxRestSASTClient cxRestSASTClient, TeamDTO teamInput, EngineConfigurationDTO engineConfigurationInput,
                                      PresetDTO presetInput, ProjectDTO project) throws CxScanPrerequisitesValidatorException {
        this.cxRestGeneralClient = cxRestGeneralClient;
        this.cxRestSASTClient = cxRestSASTClient;
        this.teamInput = teamInput;
        this.engineConfigurationInput = engineConfigurationInput;
        this.presetInput = presetInput;
        this.projectInput = project;

        validateSASTScanPrerequisites();
    }

    public ScanPrerequisitesValidator(CxRestGeneralClient cxRestGeneralClient, TeamDTO teamInput, ProjectDTO project) throws CxScanPrerequisitesValidatorException {
        this.teamInput = teamInput;
        this.projectInput = project;
        this.engineConfigurationInput = null;
        this.presetInput = null;
        this.cxRestGeneralClient = cxRestGeneralClient;
        this.cxRestSASTClient = null;

        validateOSAScanPrerequisites();
    }

    private void validateSASTScanPrerequisites() throws CxScanPrerequisitesValidatorException {
        try {
            validateScanTeam();
            validateScanPreset();
            validateScanEngineConfiguration();
            isProjectExists = findProjectOnServer();
        } catch (CxRestGeneralClientException | CxRestSASTClientException e) {
            throw new CxScanPrerequisitesValidatorException(e);
        }
        log.info("SAST scan prerequisites were validated successfully");
    }

    private void validateOSAScanPrerequisites() throws CxScanPrerequisitesValidatorException {
        try {
            validateScanTeam();
            isProjectExists = findProjectOnServer();
        } catch (CxRestGeneralClientException e) {
            throw new CxScanPrerequisitesValidatorException(e);
        }
        if (!isProjectExists) {
            int projectId;
            ProjectDTO project = new ProjectDTO();
            project.setName(projectInput.getName());
            project.setTeamId(projectInput.getTeamId());
            project.setPublic(true);
            try {
                projectId = cxRestGeneralClient.createNewProject(project);
                projectInput.setId(projectId);
            } catch (CxRestGeneralClientException e) {
                throw new CxScanPrerequisitesValidatorException(e.getMessage(), e);
            }
        }
        log.info("OSA scan prerequisites were validated successfully");
    }

    private boolean findProjectOnServer() throws CxRestGeneralClientException {
        final List<ProjectDTO> projects = cxRestGeneralClient.getProjects();
        for (ProjectDTO project : projects) {
            if (project.getTeamId().equals(projectInput.getTeamId()) &&
                    (project.getName().equals(projectInput.getName()))) {
                projectInput.setId(project.getId());
                log.info("Project id (" + projectInput.getId() + ") found in server");
                return true;
            }
        }
        return false;
    }

    private void validateScanEngineConfiguration() throws CxScanPrerequisitesValidatorException, CxRestSASTClientException {
        final List<EngineConfigurationDTO> engineConfigurations = cxRestSASTClient.getEngineConfiguration();
        for (EngineConfigurationDTO engineConfiguration : engineConfigurations) {
            if (engineConfiguration.getName().equalsIgnoreCase(engineConfigurationInput.getName())) {
                engineConfigurationInput.setId(engineConfiguration.getId());
                log.info("Engine configuration: \"" + engineConfigurationInput.getName() + "\" was validated in server");
            }
        }
        if (engineConfigurationInput.getId() == UNASSIGNED_VALUE) {
            throw new CxScanPrerequisitesValidatorException("Engine configuration: \"" + engineConfigurationInput.getName() + "\" was not found in server");
        }
    }

    private void validateScanPreset() throws CxScanPrerequisitesValidatorException, CxRestSASTClientException {
        final List<PresetDTO> presets = cxRestSASTClient.getSastPresets();
        for (PresetDTO preset : presets) {
            if (preset.getName().equalsIgnoreCase(presetInput.getName())) {
                presetInput.setId(preset.getId());
                log.info("Preset: \"" + presetInput.getName() + "\" was validated in server");
                log.trace("Preset id: " + presetInput.getId());
            }
        }
        if (presetInput.getId() == UNASSIGNED_VALUE) {
            throw new CxScanPrerequisitesValidatorException("Preset: \"" + presetInput.getName() + "\" was not found in server");
        }
    }

    private void validateScanTeam() throws CxScanPrerequisitesValidatorException, CxRestGeneralClientException {
        final List<TeamDTO> teams = cxRestGeneralClient.getTeams();
        for (TeamDTO team : teams) {
            if (team.getFullName().equalsIgnoreCase(teamInput.getFullName())) {
                teamInput.setId(team.getId());
                projectInput.setTeamId(team.getId());
                log.info("Team: \"" + teamInput.getFullName().replaceFirst("\\\\", "") + "\" was validated in server");
                log.trace("Team id: " + teamInput.getId());
            }
        }
        if (Strings.isNullOrEmpty(projectInput.getTeamId())) {
            log.info("Team: \"" + teamInput.getFullName() + "\" was not found in server");
            throw new CxScanPrerequisitesValidatorException("Team: \"" + teamInput.getFullName() + "\" was not found in server");
        }
    }

    public boolean isProjectExists() {
        return isProjectExists;
    }
}