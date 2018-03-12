package com.checkmarx.cxconsole.commands.job;

import com.checkmarx.cxconsole.clients.general.dto.ProjectDTO;
import com.checkmarx.cxconsole.clients.general.exception.CxRestGeneralClientException;
import com.checkmarx.cxconsole.clients.general.exception.CxScanPrerequisitesValidatorException;
import com.checkmarx.cxconsole.clients.general.utils.ScanPrerequisitesValidator;
import com.checkmarx.cxconsole.clients.sast.CxRestSASTClientImpl;
import com.checkmarx.cxconsole.clients.sast.constants.RemoteSourceType;
import com.checkmarx.cxconsole.clients.sast.constants.ReportStatusValue;
import com.checkmarx.cxconsole.clients.sast.dto.PerforceScanSettingDTO;
import com.checkmarx.cxconsole.clients.sast.dto.RemoteSourceScanSettingDTO;
import com.checkmarx.cxconsole.clients.sast.dto.SVNAndTFSScanSettingDTO;
import com.checkmarx.cxconsole.clients.sast.dto.ScanSettingDTO;
import com.checkmarx.cxconsole.clients.sast.exceptions.CxRestSASTClientException;
import com.checkmarx.cxconsole.commands.job.exceptions.CLIJobException;
import com.checkmarx.cxconsole.commands.job.utils.PathHandler;
import com.checkmarx.cxconsole.commands.utils.FilesUtils;
import com.checkmarx.cxconsole.parameters.CLIMandatoryParameters;
import com.checkmarx.cxconsole.parameters.CLIScanParametersSingleton;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.checkmarx.cxconsole.commands.constants.LocationType.*;
import static com.checkmarx.cxconsole.exitcodes.Constants.ExitCodes.SCAN_SUCCEEDED_EXIT_CODE;

/**
 * Created by nirli on 05/11/2017.
 */
public class CLISASTScanJob extends CLIScanJob {

    private CxRestSASTClientImpl cxRestSASTClient;

    public CLISASTScanJob(CLIScanParametersSingleton params, boolean isAsyncScan) {
        super(params, isAsyncScan);
    }

    @Override
    public Integer call() throws CLIJobException {
        CLIMandatoryParameters cliMandatoryParameters = params.getCliMandatoryParameters();
        log.info("Project name is \"" + cliMandatoryParameters.getProject().getName() + "\"");
        if (!cxRestLoginClient.isLoggedIn()) {
            super.login();
        }
        cxRestSASTClient = new CxRestSASTClientImpl(cxRestLoginClient);
        ScanPrerequisitesValidator scanPrerequisitesValidator;
        try {
            scanPrerequisitesValidator = new ScanPrerequisitesValidator(cxRestGeneralClient, cxRestSASTClient, cliMandatoryParameters.getTeam(),
                    params.getCliSastParameters().getConfiguration(), params.getCliSastParameters().getPreset(), cliMandatoryParameters.getProject());
        } catch (CxScanPrerequisitesValidatorException e) {
            throw new CLIJobException("Failed to initialize SAST scan prerequisites: " + e.getMessage());
        }

        try {
            if (!scanPrerequisitesValidator.isProjectExists()) {
                createNewSastProject(cliMandatoryParameters.getProject());
            } else {
                updateExistingSastProject(cliMandatoryParameters.getProject());
            }
            if (params.getCliSastParameters().isHasExcludedFilesParam() || params.getCliSastParameters().isHasExcludedFoldersParam()) {
                cxRestSASTClient.updateScanExclusions(cliMandatoryParameters.getProject().getId(),
                        params.getCliSastParameters().getExcludedFolders(), params.getCliSastParameters().getExcludedFiles());
            }
        } catch (CxRestGeneralClientException | CxRestSASTClientException e) {
            throw new CLIJobException(e);
        }

        if (params.getCliSharedParameters().getLocationType() == FOLDER) {
            handleLocalFolderSource(cliMandatoryParameters.getProject().getId());
        } else if (params.getCliSharedParameters().getLocationType() == SHARED) {
            handleSharedFolderSource(cliMandatoryParameters.getProject().getId());
        } else if (params.getCliSharedParameters().getLocationType() == SVN) {
            handleSVNSource(cliMandatoryParameters.getProject().getId());
        } else if (params.getCliSharedParameters().getLocationType() == TFS) {
            handleTFSSource(cliMandatoryParameters.getProject().getId());
        } else if (params.getCliSharedParameters().getLocationType() == PERFORCE) {
            handlePerforceSource(cliMandatoryParameters.getProject().getId());
        } else if (params.getCliSharedParameters().getLocationType() == GIT) {
            handleGITSource(cliMandatoryParameters.getProject().getId());
        }

        log.info("Request SAST scan");
        int scanId;
        try {
            scanId = cxRestSASTClient.createNewSastScan(cliMandatoryParameters.getProject().getId(), params.getCliSastParameters().isForceScan(),
                    params.getCliSastParameters().isIncrementalScan(), params.getCliSharedParameters().isVisibleOthers());
            log.info("SAST scan created successfully: Scan ID is " + scanId);
        } catch (CxRestSASTClientException e) {
            throw new CLIJobException(e);
        }

        // wait for scan completion
        if (isAsyncScan) {
            log.info("Asynchronous scan initiated, Waiting for SAST scan to enter the queue.");
        } else {
            log.info("Full scan initiated, Waiting for SAST scan to finish.");
        }
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        WaitScanCompletionJob waiterJob = new WaitScanCompletionJob(cxRestSASTClient, scanId, isAsyncScan);
        try {
            Future<Boolean> future = executor.schedule(waiterJob, 250, TimeUnit.MILLISECONDS);
            // wait for scan completion
            future.get();
            if (isAsyncScan) {
                log.info("SAST scan queued. Job finished");
            } else {
                log.info("SAST scan finished. Retrieving scan results");
                String comment = params.getCliSharedParameters().getScanComment();
                if (comment != null) {
                    cxRestSASTClient.updateScanComment(scanId, comment);
                }
            }
        } catch (Exception e) {
            log.trace("Error occurred during scan progress monitoring: " + e.getMessage());
            throw new CLIJobException("Error occurred during scan progress monitoring: " + e.getMessage());
        } finally {
            executor.shutdownNow();
        }


        if (!isAsyncScan) {
            //Get JSON scan summary
//            String scanSummary;
//                scanSummary = cxSoapSASTClient.getScanSummary(cliMandatoryParameters.getOriginalHost(), sessionId, scanId);

            //SAST print results
//            SASTResultsDTO scanResults = JobUtils.parseScanSummary(scanSummary);
//            PrintResultsUtils.printSASTResultsToConsole(scanResults);

            //SAST reports
            if (!params.getCliSastParameters().getReportType().isEmpty()) {
                for (int i = 0; i < params.getCliSastParameters().getReportType().size(); i++) {
                    String reportFilePath = params.getCliSastParameters().getReportFile().get(i);
                    log.info("Creating report file: " + reportFilePath);
                    try {
                        int reportId = cxRestSASTClient.createReport(scanId, params.getCliSastParameters().getReportType().get(i));
                        ReportStatusValue reportStatus;
                        do {
                            //TODO: change sleep to something smarter
                            reportStatus = cxRestSASTClient.getReportStatus(reportId);
                            Thread.sleep(350);
                        } while (reportStatus == ReportStatusValue.IN_PROCESS);
                        if (reportStatus == ReportStatusValue.CREATED) {
                            cxRestSASTClient.createReportFile(reportId, reportFilePath);
                        } else {
                            log.error("Error creating " + params.getCliSastParameters().getReportType().get(i) + " report file");
                        }
                    } catch (CxRestSASTClientException | InterruptedException e) {
                        log.error("Error creating report: " + reportFilePath + " :" + e.getMessage());
                    }
                }
            }

            //SAST threshold calculation
//            if (params.getCliSastParameters().isSastThresholdEnabled()) {
//                ThresholdDto thresholdDto = new ThresholdDto(params.getCliSastParameters().getSastHighThresholdValue(), params.getCliSastParameters().getSastMediumThresholdValue(),
//                        params.getCliSastParameters().getSastLowThresholdValue(), scanResults);
//                return resolveThresholdExitCode(thresholdDto);
//            }
        }

        return SCAN_SUCCEEDED_EXIT_CODE;
    }

    private void handleGITSource(int projectId) throws CLIJobException {
        try {
            cxRestSASTClient.createGITScan(projectId, params.getCliSastParameters().getLocationURL(),
                    params.getCliSastParameters().getLocationBranch(), params.getCliSastParameters().getPrivateKey());
        } catch (CxRestSASTClientException e) {
            throw new CLIJobException(e.getMessage());
        }
    }

    private void handlePerforceSource(int projectId) throws CLIJobException {
        String[] paths = params.getCliSharedParameters().getLocationPath().split(";");
        try {
            PerforceScanSettingDTO perforceScanSettingDTO = new PerforceScanSettingDTO(params.getCliSastParameters().getLocationUser(),
                    params.getCliSastParameters().getLocationPass(), paths, params.getCliSastParameters().getLocationURL(), params.getCliSastParameters().getLocationPort(),
                    params.getCliSastParameters().getPrivateKey(), null);
            if (params.getCliSastParameters().getPerforceWorkspaceMode() != null) {
                perforceScanSettingDTO.setBrowseMode("Workspace");
            } else {
                perforceScanSettingDTO.setBrowseMode("Depot");
            }
            cxRestSASTClient.createRemoteSourceScan(projectId, perforceScanSettingDTO, RemoteSourceType.PERFORCE);
        } catch (CxRestSASTClientException e) {
            throw new CLIJobException(e.getMessage());
        }
    }

    private void handleTFSSource(int projectId) throws CLIJobException {
        String[] paths = params.getCliSharedParameters().getLocationPath().split(";");
        try {
            SVNAndTFSScanSettingDTO svnScanSettingDTO = new SVNAndTFSScanSettingDTO(params.getCliSastParameters().getLocationUser(),
                    params.getCliSastParameters().getLocationPass(), paths, params.getCliSastParameters().getLocationURL(), params.getCliSastParameters().getLocationPort(),
                    params.getCliSastParameters().getPrivateKey());
            cxRestSASTClient.createRemoteSourceScan(projectId, svnScanSettingDTO, RemoteSourceType.TFS);
        } catch (CxRestSASTClientException e) {
            throw new CLIJobException(e.getMessage());
        }
    }

    private void handleSVNSource(int projectId) throws CLIJobException {
        String[] paths = params.getCliSharedParameters().getLocationPath().split(";");
        try {
            SVNAndTFSScanSettingDTO svnScanSettingDTO = new SVNAndTFSScanSettingDTO(params.getCliSastParameters().getLocationUser(),
                    params.getCliSastParameters().getLocationPass(), paths, params.getCliSastParameters().getLocationURL(), params.getCliSastParameters().getLocationPort(),
                    params.getCliSastParameters().getPrivateKey());
            cxRestSASTClient.createRemoteSourceScan(projectId, svnScanSettingDTO, RemoteSourceType.SVN);
        } catch (CxRestSASTClientException e) {
            throw new CLIJobException(e.getMessage());
        }
    }

    private void handleSharedFolderSource(int projectId) throws CLIJobException {
        String[] paths = params.getCliSharedParameters().getLocationPath().split(";");
        try {
            RemoteSourceScanSettingDTO remoteSourceScanSettingDTO = new RemoteSourceScanSettingDTO(params.getCliSastParameters().getLocationUser(),
                    params.getCliSastParameters().getLocationPass(), paths);
            cxRestSASTClient.createRemoteSourceScan(projectId, remoteSourceScanSettingDTO, RemoteSourceType.SHARED);
        } catch (CxRestSASTClientException e) {
            throw new CLIJobException(e.getMessage());
        }
    }

    private void handleLocalFolderSource(int projectId) throws CLIJobException {
        long maxZipSize = ConfigMgr.getCfgMgr().getLongProperty(ConfigMgr.KEY_MAX_ZIP_SIZE);
        maxZipSize *= (1024 * 1024);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        FilesUtils.zipFolder(params.getCliSharedParameters().getLocationPath(), params.getCliSastParameters(), maxZipSize, byteArrayOutputStream);
        log.info("Compressed file size is: " + FileUtils.byteCountToDisplaySize(byteArrayOutputStream.size()));
        FilesUtils.validateZippedSources(maxZipSize, byteArrayOutputStream);
        try {
            cxRestSASTClient.uploadZipFileForSASTScan(projectId, byteArrayOutputStream.toByteArray());
        } catch (CxRestSASTClientException e) {
            throw new CLIJobException(e.getMessage());
        }
    }

    private void updateExistingSastProject(ProjectDTO project) throws CxRestSASTClientException {
        ScanSettingDTO scanSetting = cxRestSASTClient.getProjectScanSetting(project.getId());
        scanSetting.setPresetId(params.getCliSastParameters().getPreset().getId());
        scanSetting.setEngineConfigurationId(params.getCliSastParameters().getConfiguration().getId());
        cxRestSASTClient.updateProjectScanSetting(scanSetting);
    }

    private void createNewSastProject(ProjectDTO project) throws CxRestSASTClientException, CxRestGeneralClientException {
        cxRestGeneralClient.createNewProject(project);
        log.info("New project was created successfully");
        log.trace("New project id= " + project.getId());
        ScanSettingDTO scanSetting = new ScanSettingDTO();
        scanSetting.setProjectId(project.getId());
        scanSetting.setPresetId(params.getCliSastParameters().getPreset().getId());
        scanSetting.setEngineConfigurationId(params.getCliSastParameters().getConfiguration().getId());
        cxRestSASTClient.createProjectScanSetting(scanSetting);
    }
}