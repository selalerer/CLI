package com.checkmarx.cxconsole.commands.job;

import com.checkmarx.cxconsole.clients.osa.client.CxRestOSAClient;
import com.checkmarx.cxconsole.clients.osa.exceptions.CxRestOSAClientException;
import com.checkmarx.cxconsole.clientsold.soap.exceptions.CxSoapClientValidatorException;
import com.checkmarx.cxconsole.clientsold.soap.sast.CxSoapSASTClient;
import com.checkmarx.cxconsole.commands.job.exceptions.CLIJobException;
import com.checkmarx.cxconsole.commands.job.utils.JobUtils;
import com.checkmarx.cxconsole.commands.job.utils.PathHandler;
import com.checkmarx.cxconsole.clients.osa.OSAConsoleScanWaitHandler;
import com.checkmarx.cxconsole.clients.osa.dto.CreateOSAScanRequest;
import com.checkmarx.cxconsole.clients.osa.dto.CreateOSAScanResponse;
import com.checkmarx.cxconsole.clients.osa.dto.OSAScanStatus;
import com.checkmarx.cxconsole.clients.osa.dto.OSASummaryResults;
import com.checkmarx.cxconsole.clients.osa.utils.OsaWSFSAUtil;
import com.checkmarx.cxviewer.ws.generated.CxWSResponseProjectsDisplayData;
import com.checkmarx.cxviewer.ws.generated.ProjectDisplayData;
import com.checkmarx.cxconsole.parameters.CLIOSAParameters;
import com.checkmarx.cxconsole.parameters.CLIScanParametersSingleton;
import com.checkmarx.cxconsole.thresholds.dto.ThresholdDto;
import org.apache.commons.lang3.StringUtils;

import static com.checkmarx.cxconsole.commands.job.utils.PrintResultsUtils.printOSAResultsToConsole;
import static com.checkmarx.cxconsole.clients.osa.dto.OSAScanStatusEnum.QUEUED;
import static com.checkmarx.cxconsole.exitcodes.Constants.ExitCodes.SCAN_SUCCEEDED_EXIT_CODE;
import static com.checkmarx.cxconsole.exitcodes.ErrorHandler.errorCodeResolver;
import static com.checkmarx.cxconsole.thresholds.ThresholdResolver.resolveThresholdExitCode;

/**
 * Created by nirli on 05/11/2017.
 */
public class CLIOSAScanJob extends CLIScanJob {

    private static final String OSA_REPORT_NAME = "CxOSAReport";
    private CxRestOSAClient cxRestOSAClient;
    private CxSoapSASTClient cxSoapSASTClient;

    public CLIOSAScanJob(CLIScanParametersSingleton params, boolean isAsyncScan) {
        super(params, isAsyncScan);
    }

    @Override
    public Integer call() throws CLIJobException {
        OSASummaryResults osaSummaryResults;
        CLIOSAParameters cliosaParameters = params.getCliOsaParameters();
        try {
            log.info("Project name is \"" + params.getCliMandatoryParameters().getProjectName() + "\"");

            // Connect to Checkmarx service, if not already connected.
            super.restLogin();
            cxRestOSAClient = new CxRestOSAClient(cxRestLoginClient);
            if (this.cxSoapLoginClient.getSessionId() == null ) {
                super.soapLogin();
                sessionId = cxSoapLoginClient.getSessionId();
            }
            cxSoapSASTClient = new CxSoapSASTClient(this.cxSoapLoginClient.getCxSoapClient());

            //Request osa Scan
            log.info("");
            log.info("Request OSA scan");

            long projectId = locateProjectOnServer();

            String[] osaLocationPath = cliosaParameters.getOsaLocationPath() != null ? cliosaParameters.getOsaLocationPath() : new String[]{params.getCliSharedParameters().getLocationPath()};
            log.info("Setting up OSA analysis request");
            log.info("OSA source location: " + StringUtils.join(osaLocationPath, ", "));
            CreateOSAScanRequest osaScanRequest;
            log.debug("    #############################################  Starting FSA    ###########################################    ");
            osaScanRequest = OsaWSFSAUtil.createOsaScanRequest(projectId, osaLocationPath, cliosaParameters);
            log.debug("    #############################################  Finished FSA   ###########################################    ");

            log.info("Sending OSA scan request");
            CreateOSAScanResponse osaScan;
            try {
                osaScan = cxRestOSAClient.createOSAScan(osaScanRequest);
            } catch (CxRestOSAClientException e) {
                log.error("Error create OSA scan: " + e.getMessage());
                throw new CLIJobException("Error create OSA scan: " + e.getMessage());
            }
            String osaProjectSummaryLink = OsaWSFSAUtil.composeProjectOSASummaryLink(params.getCliMandatoryParameters().getOriginalHost(), projectId);
            log.info("OSA scan created successfully");

            if (isAsyncScan) {
                log.info("Asynchronous scan, Waiting for OSA scan to queue");
            } else {
                log.info("Full scan initiated, Waiting for OSA scan to finish");
            }

            //wait for OSA scan to finish
            OSAConsoleScanWaitHandler osaConsoleScanWaitHandler = new OSAConsoleScanWaitHandler();
            OSAScanStatus returnStatus;
            try {
                returnStatus = cxRestOSAClient.waitForOSAScanToFinish(osaScan.getScanId(), -1, osaConsoleScanWaitHandler, isAsyncScan);
            } catch (CxRestOSAClientException e) {
                log.error("Error retrieving OSA scan status: " + e.getMessage());
                throw new CLIJobException("Error retrieving OSA scan status: " + e.getMessage());
            }

            if (isAsyncScan && returnStatus.getStatus() == QUEUED) {
                return SCAN_SUCCEEDED_EXIT_CODE;
            }
            if (!isAsyncScan) {
                log.info("OSA scan finished successfully");
                //OSA scan results
                try {
                    osaSummaryResults = cxRestOSAClient.getOSAScanSummaryResults(osaScan.getScanId());
                } catch (CxRestOSAClientException e) {
                    log.error("Error retrieving OSA scan summary results: " + e.getMessage());
                    throw new CLIJobException("Error retrieving OSA scan summary results: " + e.getMessage());
                }
                printOSAResultsToConsole(osaSummaryResults, osaProjectSummaryLink);

                //OSA reports
                String jsonFile = cliosaParameters.getOsaJson();
                try {
                    if (jsonFile != null) {
                        log.info("Creating CxOSA Reports");
                        log.info("-----------------------");
                        String workDirectory = JobUtils.gerWorkDirectory(params);

                        //OSA json reports
                        if (jsonFile != null) {
                            String resultFilePath = PathHandler.resolveReportPath(params.getCliMandatoryParameters().getProjectName(), "JSON", jsonFile, "", workDirectory);
                            cxRestOSAClient.createOsaJson(osaScan.getScanId(), resultFilePath, osaSummaryResults);
                        }
                    }
                } catch (CxRestOSAClientException e) {
                    log.error("Error occurred during CxOSA reports. Error message: " + e.getMessage());
                    return errorCodeResolver(e.getMessage());
                }

                //Osa threshold calculation
                if (cliosaParameters.isOsaThresholdEnabled()) {
                    ThresholdDto thresholdDto = new ThresholdDto(ThresholdDto.ScanType.OSA_SCAN, cliosaParameters.getOsaHighThresholdValue(), cliosaParameters.getOsaMediumThresholdValue(),
                            cliosaParameters.getOsaLowThresholdValue(), osaSummaryResults.getTotalHighVulnerabilities(),
                            osaSummaryResults.getTotalMediumVulnerabilities(), osaSummaryResults.getTotalLowVulnerabilities());
                    return resolveThresholdExitCode(thresholdDto);
                }
            } else {
                log.info("OSA scan queued successfully. Job finished");
            }
        } finally {
            if (cxRestOSAClient != null) {
                cxRestOSAClient.close();
            }
        }
        if (super.getErrorMsg() != null) {
            return errorCodeResolver(super.getErrorMsg());
        }

        return SCAN_SUCCEEDED_EXIT_CODE;
    }

    private long locateProjectOnServer() throws CLIJobException {
        CxWSResponseProjectsDisplayData projectData;
        try {
            projectData = cxSoapSASTClient.getProjectsDisplayData(sessionId);
            for (ProjectDisplayData data : projectData.getProjectList().getProjectDisplayData()) {
                String projectFullName = data.getGroup() + "\\" + data.getProjectName();
                if (projectFullName.equalsIgnoreCase(params.getCliMandatoryParameters().getProjectNameWithPath())) {
                    return data.getProjectID();
                }
            }
        } catch (CxSoapClientValidatorException e) {
            throw new CLIJobException("The project: " + params.getCliMandatoryParameters().getProjectNameWithPath() + " was not found on the server. OSA scan requires an existing project on the server");
        }

        throw new CLIJobException("The project: " + params.getCliMandatoryParameters().getProjectNameWithPath() + " was not found on the server. OSA scan requires an existing project on the server");
    }
}