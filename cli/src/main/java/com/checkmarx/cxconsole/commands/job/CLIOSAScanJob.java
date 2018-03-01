package com.checkmarx.cxconsole.commands.job;

import com.checkmarx.cxconsole.clients.general.exception.CxScanPrerequisitesValidatorException;
import com.checkmarx.cxconsole.clients.general.utils.ScanPrerequisitesValidator;
import com.checkmarx.cxconsole.clients.osa.CxRestOSAClient;
import com.checkmarx.cxconsole.clients.osa.OSAConsoleScanWaitHandler;
import com.checkmarx.cxconsole.clients.osa.dto.CreateOSAScanRequest;
import com.checkmarx.cxconsole.clients.osa.dto.CreateOSAScanResponse;
import com.checkmarx.cxconsole.clients.osa.dto.OSAScanStatus;
import com.checkmarx.cxconsole.clients.osa.dto.OSASummaryResults;
import com.checkmarx.cxconsole.clients.osa.exceptions.CxRestOSAClientException;
import com.checkmarx.cxconsole.clients.osa.utils.OsaWSFSAUtil;
import com.checkmarx.cxconsole.clientsold.soap.sast.CxSoapSASTClient;
import com.checkmarx.cxconsole.commands.job.exceptions.CLIJobException;
import com.checkmarx.cxconsole.commands.job.utils.JobUtils;
import com.checkmarx.cxconsole.commands.job.utils.PathHandler;
import com.checkmarx.cxconsole.constants.ScanType;
import com.checkmarx.cxconsole.parameters.CLIOSAParameters;
import com.checkmarx.cxconsole.parameters.CLIScanParametersSingleton;
import com.checkmarx.cxconsole.thresholds.dto.ThresholdDto;
import org.apache.commons.lang3.StringUtils;

import static com.checkmarx.cxconsole.clients.osa.dto.OSAScanStatusEnum.QUEUED;
import static com.checkmarx.cxconsole.commands.job.utils.PrintResultsUtils.printOSAResultsToConsole;
import static com.checkmarx.cxconsole.exitcodes.Constants.ExitCodes.SCAN_SUCCEEDED_EXIT_CODE;
import static com.checkmarx.cxconsole.exitcodes.ErrorHandler.errorCodeResolver;
import static com.checkmarx.cxconsole.thresholds.ThresholdResolver.resolveThresholdExitCode;

/**
 * Created by nirli on 05/11/2017.
 */
public class CLIOSAScanJob extends CLIScanJob {

    private static final String OSA_REPORT_NAME = "CxOSAReport";
    private CxRestOSAClient cxRestOSAClient;

    public CLIOSAScanJob(CLIScanParametersSingleton params, boolean isAsyncScan) {
        super(params, isAsyncScan);
    }

    @Override
    public Integer call() throws CLIJobException {
        OSASummaryResults osaSummaryResults;
        CLIOSAParameters cliosaParameters = params.getCliOsaParameters();
        try {
            log.info("Project name is \"" + params.getCliMandatoryParameters().getProject().getName() + "\"");

            // Connect to Checkmarx service, if not already connected.
            if (!cxRestLoginClient.isLoggedIn()) {
                super.login();
            }
            cxRestOSAClient = new CxRestOSAClient(cxRestLoginClient);
            try {
                ScanPrerequisitesValidator scanPrerequisitesValidator = new ScanPrerequisitesValidator(cxRestGeneralClient,
                        params.getCliMandatoryParameters().getTeam(), params.getCliMandatoryParameters().getProject());
            } catch (CxScanPrerequisitesValidatorException e) {
//                log.error("Failed to initialize OSA scan prerequisites: " + e.getMessage());
                throw new CLIJobException("Failed to initialize OSA scan prerequisites: " + e.getMessage());
            }

            //Request osa Scan
            log.info("Request OSA scan");

            String[] osaLocationPath = cliosaParameters.getOsaLocationPath() != null ? cliosaParameters.getOsaLocationPath() : new String[]{params.getCliSharedParameters().getLocationPath()};
            log.info("Setting up OSA analysis request");
            log.info("OSA source location: " + StringUtils.join(osaLocationPath, ", "));
            CreateOSAScanRequest osaScanRequest;
            log.debug("    #############################################  Starting FSA    ###########################################    ");
            osaScanRequest = OsaWSFSAUtil.createOsaScanRequest(params.getCliMandatoryParameters().getProject().getId(),
                    osaLocationPath, cliosaParameters);
            log.debug("    #############################################  Finished FSA   ###########################################    ");

            log.info("Sending OSA scan request");
            CreateOSAScanResponse osaScan;
            try {
                osaScan = cxRestOSAClient.createOSAScan(osaScanRequest);
            } catch (CxRestOSAClientException e) {
                log.error("Error create OSA scan: " + e.getMessage());
                throw new CLIJobException("Error create OSA scan: " + e.getMessage());
            }
            String osaProjectSummaryLink = OsaWSFSAUtil.composeProjectOSASummaryLink(params.getCliMandatoryParameters().getOriginalHost(),
                    params.getCliMandatoryParameters().getProject().getId());
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
                            String resultFilePath = PathHandler.resolveReportPath(params.getCliMandatoryParameters().getProject().getName(), "JSON", jsonFile, "", workDirectory);
                            cxRestOSAClient.createOsaJson(osaScan.getScanId(), resultFilePath, osaSummaryResults);
                        }
                    }
                } catch (CxRestOSAClientException e) {
                    log.error("Error occurred during CxOSA reports. Error message: " + e.getMessage());
                    return errorCodeResolver(e.getMessage());
                }

                //Osa threshold calculation
                if (cliosaParameters.isOsaThresholdEnabled()) {
                    ThresholdDto thresholdDto = new ThresholdDto(ScanType.OSA_SCAN, cliosaParameters.getOsaHighThresholdValue(), cliosaParameters.getOsaMediumThresholdValue(),
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

}