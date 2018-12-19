package com.checkmarx.cxconsole.commands.job;

import com.checkmarx.cxconsole.clients.arm.CxRestArmClient;
import com.checkmarx.cxconsole.clients.arm.CxRestArmClientImpl;
import com.checkmarx.cxconsole.clients.arm.dto.CxArmConfig;
import com.checkmarx.cxconsole.clients.arm.exceptions.CxRestARMClientException;
import com.checkmarx.cxconsole.clients.general.dto.CxProviders;
import com.checkmarx.cxconsole.clients.general.dto.ProjectDTO;
import com.checkmarx.cxconsole.clients.general.exception.CxRestGeneralClientException;
import com.checkmarx.cxconsole.clients.general.exception.CxScanPrerequisitesValidatorException;
import com.checkmarx.cxconsole.clients.general.utils.ScanPrerequisitesValidator;
import com.checkmarx.cxconsole.clients.osa.exceptions.CxRestOSAClientException;
import com.checkmarx.cxconsole.clients.sast.CxRestSASTClient;
import com.checkmarx.cxconsole.clients.sast.CxRestSASTClientImpl;
import com.checkmarx.cxconsole.clients.sast.constants.RemoteSourceType;
import com.checkmarx.cxconsole.clients.sast.constants.ReportStatusValue;
import com.checkmarx.cxconsole.clients.sast.constants.ReportType;
import com.checkmarx.cxconsole.clients.sast.dto.*;
import com.checkmarx.cxconsole.clients.sast.exceptions.CxRestSASTClientException;
import com.checkmarx.cxconsole.clients.utils.RestClientUtils;
import com.checkmarx.cxconsole.commands.job.exceptions.CLIJobException;
import com.checkmarx.cxconsole.commands.job.utils.PrintResultsUtils;
import com.checkmarx.cxconsole.commands.utils.FilesUtils;
import com.checkmarx.cxconsole.parameters.CLIMandatoryParameters;
import com.checkmarx.cxconsole.parameters.CLIScanParametersSingleton;
import com.checkmarx.cxconsole.thresholds.dto.ThresholdDto;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.checkmarx.cxconsole.exitcodes.Constants.ExitCodes.SCAN_SUCCEEDED_EXIT_CODE;
import static com.checkmarx.cxconsole.exitcodes.ErrorHandler.errorCodeResolver;
import static com.checkmarx.cxconsole.thresholds.ThresholdResolver.resolveThresholdExitCode;

/**
 * Created by nirli on 05/11/2017.
 */
public class CLISASTScanJob extends CLIScanJob {

    private CxRestSASTClient cxRestSASTClient;

    public CLISASTScanJob(CLIScanParametersSingleton params, boolean isAsyncScan) {
        super(params, isAsyncScan);
    }

    @Override
    public Integer call() throws CLIJobException {
        int exitCode = SCAN_SUCCEEDED_EXIT_CODE;
        CLIMandatoryParameters cliMandatoryParameters = params.getCliMandatoryParameters();
        log.info(String.format("Project name is %s", cliMandatoryParameters.getProject().getName()));
        if (!cxRestLoginClient.isLoggedIn()) {
            login();
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

        switch (params.getCliSharedParameters().getLocationType()) {
            case FOLDER:
                handleLocalFolderSource(cliMandatoryParameters.getProject().getId());
                break;
            case SHARED:
                handleSharedFolderSource(cliMandatoryParameters.getProject().getId());
                break;
            case SVN:
                handleSVNSource(cliMandatoryParameters.getProject().getId());
                break;
            case TFS:
                handleTFSSource(cliMandatoryParameters.getProject().getId());
                break;
            case PERFORCE:
                handlePerforceSource(cliMandatoryParameters.getProject().getId());
                break;
            case GIT:
                handleGITSource(cliMandatoryParameters.getProject().getId());
                break;
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
        } catch (Exception e) {
            log.trace("Error occurred during scan progress monitoring: " + e.getMessage());
            throw new CLIJobException("Error occurred during scan progress monitoring: " + e.getMessage());
        } finally {
            executor.shutdownNow();
        }

        if (isAsyncScan) {
            log.info("SAST scan queued. Job finished");
            return SCAN_SUCCEEDED_EXIT_CODE;
        } else {
            log.info("SAST scan finished. Retrieving scan results");
            String comment = params.getCliSharedParameters().getScanComment();
            if (comment != null) {
                try {
                    cxRestSASTClient.updateScanComment(scanId, comment);
                } catch (CxRestSASTClientException e) {
                    throw new CLIJobException(e);
                }
            }

            for (Map.Entry<ReportType, String> report : params.getCliSastParameters().getReportsPath().entrySet()) {
                createReportFile(report, scanId, cliMandatoryParameters.getProject().getName());
            }
        }

        try {
            ResultsStatisticsDTO sastScanResults = cxRestSASTClient.getScanResults(scanId);
            PrintResultsUtils.printSASTResultsToConsole(sastScanResults);
            if (params.getCliSastParameters().isSastThresholdEnabled()) {
                ThresholdDto thresholdDto = new ThresholdDto(params.getCliSastParameters().getSastHighThresholdValue(), params.getCliSastParameters().getSastMediumThresholdValue(),
                        params.getCliSastParameters().getSastLowThresholdValue(), sastScanResults);
                return resolveThresholdExitCode(thresholdDto);
            }
        } catch (CxRestSASTClientException e) {
            log.error("Error retrieving SAST scan result: " + e.getMessage());
        }

        if (params.getCliSastParameters().isCheckPolicyViolations()) {
            CxArmConfig armConfig;
            try {
                armConfig = cxRestSASTClient.getCxArmConfiguration();
            } catch (CxRestOSAClientException e) {
                log.error("Error occurred during CxSAST get CXArm configuration. Error message: " + e.getMessage());
                return errorCodeResolver(e.getMessage());
            }
            try {
                CxRestArmClient armClient = new CxRestArmClientImpl(cxRestLoginClient, armConfig.getCxARMPolicyURL());
                exitCode = RestClientUtils.getArmViolationExitCode(armClient, CxProviders.SAST, params.getCliMandatoryParameters().getProject().getId(), log);
            } catch (CxRestARMClientException e) {
                log.error("Error occurred during getting CxARM violations. Error message: " + e.getMessage());
                return errorCodeResolver(e.getMessage());
            }
        }

        return exitCode;
    }


    private void createReportFile(Map.Entry<ReportType, String> report, int scanId, String projectName) {
        String reportFilePath = report.getValue();
        ReportType reportType = report.getKey();

        File reportFile = new File(reportFilePath);
        if (!reportFile.isAbsolute()) {
            reportFile = new File(System.getProperty("user.dir") + File.separator + projectName + File.separator + reportFile);
        }

        if (!reportFile.getParentFile().exists()) {
            reportFile.getParentFile().mkdirs();
        }

        log.info("Creating report file at: " + reportFile);
        try {
            int reportId = cxRestSASTClient.createReport(scanId, reportType);
            ReportStatusValue reportStatus = cxRestSASTClient.getReportStatus(reportId);
            while (reportStatus == ReportStatusValue.IN_PROCESS) {
                Thread.sleep(500);
                reportStatus = cxRestSASTClient.getReportStatus(reportId);
            }
            if (reportStatus == ReportStatusValue.CREATED) {
                cxRestSASTClient.createReportFile(reportId, reportFile);
            } else {
                log.error("Error creating " + reportType + " report file");
            }
        } catch (CxRestSASTClientException | InterruptedException e) {
            log.error("Error creating report: " + reportFilePath + " :" + e.getMessage());
        }
    }


    private void handleGITSource(int projectId) throws CLIJobException {
        try {
            byte[] keyFile = new byte[0];
            if (params.getCliSastParameters().getLocationPrivateKeyFilePath() != null) {
                keyFile = filePathToByteArray(params.getCliSastParameters().getLocationPrivateKeyFilePath());
            }
            cxRestSASTClient.createGITScan(projectId, params.getCliSastParameters().getLocationURL(),
                    params.getCliSastParameters().getLocationBranch(), keyFile);
        } catch (CxRestSASTClientException e) {
            throw new CLIJobException(e.getMessage());
        } catch (IOException e) {
            throw new CLIJobException("Error reading from key file: " + e.getMessage());
        }
    }

    private void handlePerforceSource(int projectId) throws CLIJobException {
        String[] paths = params.getCliSharedParameters().getLocationPath().split(";");
        try {
            PerforceScanSettingDTO perforceScanSettingDTO = new PerforceScanSettingDTO(params.getCliSastParameters().getLocationUser(),
                    params.getCliSastParameters().getLocationPass(), paths, params.getCliSastParameters().getLocationURL(), params.getCliSastParameters().getLocationPort(),
                    null, null);
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
                    null);
            cxRestSASTClient.createRemoteSourceScan(projectId, svnScanSettingDTO, RemoteSourceType.TFS);
        } catch (CxRestSASTClientException e) {
            throw new CLIJobException(e.getMessage());
        }
    }

    private void handleSVNSource(int projectId) throws CLIJobException {
        String[] paths = params.getCliSharedParameters().getLocationPath().split(";");
        try {
            byte[] keyFile = new byte[0];
            if (params.getCliSastParameters().getLocationPrivateKeyFilePath() != null) {
                keyFile = filePathToByteArray(params.getCliSastParameters().getLocationPrivateKeyFilePath());
            }
            SVNAndTFSScanSettingDTO svnScanSettingDTO = new SVNAndTFSScanSettingDTO(params.getCliSastParameters().getLocationUser(),
                    params.getCliSastParameters().getLocationPass(), paths, params.getCliSastParameters().getLocationURL(), params.getCliSastParameters().getLocationPort(),
                    keyFile);
            cxRestSASTClient.createRemoteSourceScan(projectId, svnScanSettingDTO, RemoteSourceType.SVN);
        } catch (CxRestSASTClientException e) {
            throw new CLIJobException(e.getMessage());
        } catch (IOException e) {
            throw new CLIJobException("Error reading from key file: " + e.getMessage());
        }
    }

    private byte[] filePathToByteArray(String fileLocation) throws IOException {
        File resultFile = new File(fileLocation);
        if (!resultFile.isAbsolute()) {
            String path = System.getProperty("user.dir");
            fileLocation = path + File.separator + fileLocation;
        }
        FileInputStream fis = new FileInputStream(new File(fileLocation));
        return IOUtils.toByteArray(fis);
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