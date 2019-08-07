package com.checkmarx.cxconsole.parameters;

import com.checkmarx.cxconsole.clients.sast.constants.ReportType;
import com.checkmarx.cxconsole.clients.sast.dto.EngineConfigurationDTO;
import com.checkmarx.cxconsole.clients.sast.dto.PresetDTO;
import com.checkmarx.cxconsole.commands.constants.LocationType;
import com.checkmarx.cxconsole.parameters.exceptions.CLIParameterParsingException;
import com.google.common.base.Strings;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nirli on 29/10/2017.
 */
public class CLISASTParameters extends AbstractCLIScanParameters {

    private static final String DEFAULT_PRESET_NAME = "Checkmarx Default";
    private static final String DEFAULT_ENGINE_CONFIGURATION_NAME = "Default Configuration";

    /**
     * Definition of command line parameters to be used by Apache CLI parser
     */
    private Options commandLineOptions;

    private CLIMandatoryParameters cliMandatoryParameters;
    private CLISharedParameters cliSharedParameters;

    private PresetDTO presetDTO;
    private EngineConfigurationDTO configuration;
    private boolean isIncrementalScan = false;
    private boolean forceScan = true;

    //Mapping a Map<reportType, reportPath> / (e.g. PDF) to its file path
    private Map<ReportType, String> reports = new HashMap<>();

    private String xmlFile;
    private boolean isOsaEnabled = false;

    private String[] excludedFolders = new String[]{};
    private boolean hasExcludedFoldersParam = false;
    private String[] excludedFiles = new String[]{};
    private boolean hasExcludedFilesParam = false;

    private String locationURL;
    private String locationBranch;
    private String locationUser;
    private String locationPass;
    private String locationPrivateKeyFilePath;
    private Integer locationPort;
    private LocationType locationType;
    private String perforceWorkspaceMode;

    private boolean isSastThresholdEnabled = false;
    private int sastLowThresholdValue = Integer.MAX_VALUE;
    private int sastMediumThresholdValue = Integer.MAX_VALUE;
    private int sastHighThresholdValue = Integer.MAX_VALUE;

    private static final Option PARAM_XML_FILE = Option.builder("reportxml").hasArg(true).argName("file").desc("Name or path to results XML file. Optional.").build();
    private static final Option PARAM_PDF_FILE = Option.builder("reportpdf").hasArg(true).argName("file").desc("Name or path to results PDF file. Optional.").build();
    private static final Option PARAM_CSV_FILE = Option.builder("reportcsv").hasArg(true).argName("file").desc("Name or path to results CSV file. Optional.").build();
    private static final Option PARAM_RTF_FILE = Option.builder("reportrtf").hasArg(true).argName("file").desc("Name or path to results RTF file. Optional.").build();

    private static final Option PARAM_LOCATION_USER = Option.builder("locationuser").argName("username").hasArg(true)
            .desc("Source control or network username. Required if -LocationType is TFS/Perforce/shared.").build();
    private static final Option PARAM_LOCATION_PWD = Option.builder("locationpassword").argName("password").hasArg(true)
            .desc("Source control or network password. Required if -LocationType is TFS/Perforce/shared.").build();
    private static final Option PARAM_LOCATION_URL = Option.builder("locationurl").argName("url").hasArg(true)
            .desc("Source control URL. Required if -LocationType is TFS/SVN/GIT/Perforce. For Perforce SSL, set ssl:<URL> .").build();
    private static final Option PARAM_LOCATION_PORT = Option.builder("locationport").argName("url").hasArg(true)
            .desc("Source control system port. Default 8080/80/1666 (TFS/SVN/Perforce). Optional.").build();
    private static final Option PARAM_LOCATION_BRANCH = Option.builder("locationbranch").argName("branch").hasArg(true)
            .desc("Sources GIT branch. Required if -LocationType is GIT. Optional.").build();
    private static final Option PARAM_LOCATION_PRIVATE_KEY = Option.builder("locationprivatekey").argName("file").hasArg(true)
            .desc("GIT/SVN private key location. Required  if -LocationType is GIT/SVN in SSH mode.").build();
    private static final Option PARAM_PRESET = Option.builder("preset").argName("preset").hasArg(true)
            .desc("If preset is not specified, will use the predefined preset for an existing project, and Default preset for a new project. Optional.").build();
    private static final Option PARAM_CONFIGURATION = Option.builder("configuration").argName("configuration").hasArg(true)
            .desc("If configuration is not set, \"Default Configuration\" will be used for a new project. Possible values: [ \"Default Configuration\" | \"Japanese (Shift-JIS)\" ] Optional.").build();
    private static final Option PARAM_INCREMENTAL = Option.builder("incremental").hasArg(false).desc("Run incremental scan instead of full scan. Optional.").build();
    private static final Option PARAM_FORCE_SCAN = Option.builder("forcescan").hasArg(false).desc("Force scan on source code, which has not been changed since the last scan of the same project. Optional.").build();
    private static final Option PARAM_WORKSPACE = Option.builder("workspacemode").hasArg(true).desc("Use location path to specify Perforce workspace name. Optional.").build();
    private static final Option PARAM_ENABLE_OSA = Option.builder("enableosa").hasArg(false).desc("Enable Open Source Analysis (OSA). It requires the -LocationType to be folder/shared.  Optional.)").build();

    private static final Option PARAM_EXCLUDE_FOLDERS = Option.builder("locationpathexclude").hasArgs().argName("folders list").desc("Comma separated list of folder path patterns to exclude from scan. Example: '-LocationPathExclude test*' excludes all folders which start with 'test' prefix. Optional.")
            .valueSeparator(',').build();
    private static final Option PARAM_EXCLUDE_FILES = Option.builder("locationfilesexclude").hasArgs().argName("files list").desc("Comma separated list of file name patterns to exclude from scan. Example: '-LocationFilesExclude *.class' excludes all files with '.class' extension. Optional.")
            .valueSeparator(',').build();

    private static final Option PARAM_SAST_LOW_THRESHOLD = Option.builder("sastlow").hasArg(true).argName("number of low SAST vulnerabilities")
            .desc("SAST low severity vulnerability threshold. If the number of low vulnerabilities exceeds the threshold, scan will end with an error. Optional. ").build();
    private static final Option PARAM_SAST_MEDIUM_THRESHOLD = Option.builder("sastmedium").hasArg(true).argName("number of medium SAST vulnerabilities")
            .desc("SAST medium severity vulnerability threshold. If the number of medium vulnerabilities exceeds the threshold, scan will end with an error. Optional. ").build();
    private static final Option PARAM_SAST_HIGH_THRESHOLD = Option.builder("sasthigh").hasArg(true).argName("number of high SAST vulnerabilities")
            .desc("SAST high severity vulnerability threshold. If the number of high vulnerabilities exceeds the threshold, scan will end with an error. Optional. ").build();

    private static final Option PARAM_RUN_POLICY_VIOLATIONS = Option.builder("checkpolicy").hasArg(false).argName("Check Policy Violations").desc("Mark the build as failed or unstable if the project's policy is violated. Optional.").build();
    private boolean checkPolicyViolations = false;

    CLISASTParameters() throws CLIParameterParsingException {
        initCommandLineOptions();
    }

    void initSastParams(CommandLine parsedCommandLineArguments, LocationType locationType) throws CLIParameterParsingException {
        String presetName = parsedCommandLineArguments.getOptionValue(PARAM_PRESET.getOpt());
        presetDTO = Strings.isNullOrEmpty(presetName) ? new PresetDTO(DEFAULT_PRESET_NAME) : new PresetDTO(presetName);

        String configurationName = parsedCommandLineArguments.getOptionValue(PARAM_CONFIGURATION.getOpt());
        configuration = Strings.isNullOrEmpty(configurationName) ? new EngineConfigurationDTO(DEFAULT_ENGINE_CONFIGURATION_NAME) :
                new EngineConfigurationDTO(configurationName);

        checkPolicyViolations = parsedCommandLineArguments.hasOption(PARAM_RUN_POLICY_VIOLATIONS.getOpt());
        isIncrementalScan = parsedCommandLineArguments.hasOption(PARAM_INCREMENTAL.getOpt());
        forceScan = parsedCommandLineArguments.hasOption(PARAM_FORCE_SCAN.getOpt());
        isOsaEnabled = parsedCommandLineArguments.hasOption(PARAM_ENABLE_OSA.getOpt());
        this.locationType = locationType;

        initReportFilesParams(parsedCommandLineArguments);
        initExcludedFilesAndFolderParams(parsedCommandLineArguments);

        locationUser = parsedCommandLineArguments.getOptionValue(PARAM_LOCATION_USER.getOpt());
        locationPass = parsedCommandLineArguments.getOptionValue(PARAM_LOCATION_PWD.getOpt());

        if (locationType == LocationType.PERFORCE && !parsedCommandLineArguments.hasOption(PARAM_LOCATION_PWD.getOpt())) {
            // In Perforce the password is not mandatory in case of a new user
            locationPass = "";
        }

        locationURL = parsedCommandLineArguments.getOptionValue(PARAM_LOCATION_URL.getOpt());
        locationBranch = parsedCommandLineArguments.getOptionValue(PARAM_LOCATION_BRANCH.getOpt());
        locationPrivateKeyFilePath = parsedCommandLineArguments.getOptionValue(PARAM_LOCATION_PRIVATE_KEY.getOpt());

        initLocationPort(parsedCommandLineArguments);
        perforceWorkspaceMode = parsedCommandLineArguments.getOptionValue(PARAM_WORKSPACE.getOpt());

        String sastLowThresholdStr = parsedCommandLineArguments.getOptionValue(PARAM_SAST_LOW_THRESHOLD.getOpt());
        String sastMediumThresholdStr = parsedCommandLineArguments.getOptionValue(PARAM_SAST_MEDIUM_THRESHOLD.getOpt());
        String sastHighThresholdStr = parsedCommandLineArguments.getOptionValue(PARAM_SAST_HIGH_THRESHOLD.getOpt());
        if (sastLowThresholdStr != null || sastMediumThresholdStr != null || sastHighThresholdStr != null) {
            isSastThresholdEnabled = true;
            if (sastLowThresholdStr != null) {
                sastLowThresholdValue = Integer.parseInt(sastLowThresholdStr);
            }

            if (sastMediumThresholdStr != null) {
                sastMediumThresholdValue = Integer.parseInt(sastMediumThresholdStr);
            }

            if (sastHighThresholdStr != null) {
                sastHighThresholdValue = Integer.parseInt(sastHighThresholdStr);
            }
        }
    }

    private void initLocationPort(CommandLine parsedCommandLineArguments) {
        if (parsedCommandLineArguments.hasOption(PARAM_LOCATION_PORT.getOpt())) {
            String portStr = parsedCommandLineArguments.getOptionValue(PARAM_LOCATION_PORT.getOpt());
            locationPort = Integer.parseInt(portStr);
        } else {
            if (locationType == LocationType.SVN) {
                if (locationURL.toLowerCase().startsWith("svn://")) {
                    locationPort = 3690;
                } else {
                    locationPort = 80;
                }
            } else if (locationType == LocationType.TFS) {
                locationPort = 8080;
            } else if (locationType == LocationType.PERFORCE) {
                locationPort = 1666;
            }
        }
    }

    private void initExcludedFilesAndFolderParams(CommandLine parsedCommandLineArguments) {
        if (parsedCommandLineArguments.hasOption(PARAM_EXCLUDE_FOLDERS.getOpt())) {
            hasExcludedFoldersParam = true;
            excludedFolders = parsedCommandLineArguments.getOptionValues(PARAM_EXCLUDE_FOLDERS.getOpt());
        }

        if (parsedCommandLineArguments.hasOption(PARAM_EXCLUDE_FILES.getOpt())) {
            hasExcludedFilesParam = true;
            excludedFiles = parsedCommandLineArguments.getOptionValues(PARAM_EXCLUDE_FILES.getOpt());
        }
    }

    private void initReportFilesParams(CommandLine parsedCommandLineArguments) {
        if (parsedCommandLineArguments.hasOption(PARAM_XML_FILE.getOpt())) {
            reports.put(ReportType.XML, parsedCommandLineArguments.getOptionValue(PARAM_XML_FILE.getOpt()));
        }
        if (parsedCommandLineArguments.hasOption(PARAM_PDF_FILE.getOpt())) {
            reports.put(ReportType.PDF, parsedCommandLineArguments.getOptionValue(PARAM_PDF_FILE.getOpt()));
        }
        if (parsedCommandLineArguments.hasOption(PARAM_CSV_FILE.getOpt())) {
            reports.put(ReportType.CSV, parsedCommandLineArguments.getOptionValue(PARAM_CSV_FILE.getOpt()));
        }
        if (parsedCommandLineArguments.hasOption(PARAM_RTF_FILE.getOpt())) {
            reports.put(ReportType.RTF, parsedCommandLineArguments.getOptionValue(PARAM_RTF_FILE.getOpt()));
        }
    }

    public boolean isSastThresholdEnabled() {
        return isSastThresholdEnabled;
    }

    public int getSastLowThresholdValue() {
        return sastLowThresholdValue;
    }

    public int getSastMediumThresholdValue() {
        return sastMediumThresholdValue;
    }

    public int getSastHighThresholdValue() {
        return sastHighThresholdValue;
    }

    public PresetDTO getPreset() {
        return presetDTO;
    }

    public EngineConfigurationDTO getConfiguration() {
        return configuration;
    }

    public boolean isIncrementalScan() {
        return isIncrementalScan;
    }

    public boolean isForceScan() {
        return forceScan;
    }

    public boolean isOsaEnabled() {
        return isOsaEnabled;
    }

    public void setOsaEnabled(boolean osaEnabled) {
        isOsaEnabled = osaEnabled;
    }

    public Map<ReportType, String> getReportsPath() {
        return reports;
    }

    public String getXmlFile() {
        return xmlFile;
    }

    public Options getCommandLineOptions() {
        return commandLineOptions;
    }

    public String getLocationURL() {
        return locationURL;
    }

    public String getLocationBranch() {
        return locationBranch;
    }

    public String getLocationUser() {
        return locationUser;
    }

    public String getLocationPass() {
        return locationPass;
    }

    public String getLocationPrivateKeyFilePath() {
        return locationPrivateKeyFilePath;
    }

    public Integer getLocationPort() {
        return locationPort;
    }

    public String getPerforceWorkspaceMode() {
        return perforceWorkspaceMode;
    }

    public String[] getExcludedFolders() {
        return excludedFolders;
    }

    public boolean isHasExcludedFoldersParam() {
        return hasExcludedFoldersParam;
    }

    public String[] getExcludedFiles() {
        return excludedFiles;
    }

    public boolean isHasExcludedFilesParam() {
        return hasExcludedFilesParam;
    }

    public boolean isCheckPolicyViolations() {
        return checkPolicyViolations;
    }

    public void setPerforceWorkspaceMode(String perforceWorkspaceMode) {
        this.perforceWorkspaceMode = perforceWorkspaceMode;
    }

    @Override
    void initCommandLineOptions() {
        commandLineOptions = new Options();
        commandLineOptions.addOption(PARAM_LOCATION_USER);
        commandLineOptions.addOption(PARAM_LOCATION_PWD);
        commandLineOptions.addOption(PARAM_LOCATION_URL);
        commandLineOptions.addOption(PARAM_LOCATION_PORT);
        commandLineOptions.addOption(PARAM_LOCATION_BRANCH);
        commandLineOptions.addOption(PARAM_LOCATION_PRIVATE_KEY);
        commandLineOptions.addOption(PARAM_PRESET);
        commandLineOptions.addOption(PARAM_CONFIGURATION);
        commandLineOptions.addOption(PARAM_INCREMENTAL);
        commandLineOptions.addOption(PARAM_FORCE_SCAN);
        commandLineOptions.addOption(PARAM_WORKSPACE);
        commandLineOptions.addOption(PARAM_ENABLE_OSA);
        commandLineOptions.addOption(PARAM_SAST_LOW_THRESHOLD);
        commandLineOptions.addOption(PARAM_SAST_MEDIUM_THRESHOLD);
        commandLineOptions.addOption(PARAM_SAST_HIGH_THRESHOLD);

        OptionGroup reportGroup = new OptionGroup();
        reportGroup.setRequired(false);
        reportGroup.addOption(PARAM_XML_FILE);
        reportGroup.addOption(PARAM_PDF_FILE);
        reportGroup.addOption(PARAM_CSV_FILE);
        reportGroup.addOption(PARAM_RTF_FILE);
        commandLineOptions.addOptionGroup(reportGroup);
        commandLineOptions.addOption(PARAM_EXCLUDE_FOLDERS);
        commandLineOptions.addOption(PARAM_EXCLUDE_FILES);
    }

    @Override
    public String getMandatoryParams() {
        return cliMandatoryParameters.getMandatoryParams() + cliSharedParameters.getParamLocationType() + " locationType ";
    }

    public String getOptionalParams() {
        return "[ " + PARAM_XML_FILE + " results.xml ] "
                + "[ " + PARAM_PDF_FILE + " results.pdf ] "
                + "[ " + PARAM_CSV_FILE + " results.csv ] "
                + "[ " + PARAM_EXCLUDE_FOLDERS + " \"DirName1,DirName2,DirName3\" ] "
                + "[ " + PARAM_EXCLUDE_FILES + " \"FileName1,FileName2,FileName3\" ] "
                + "[ " + cliSharedParameters.getParamLogFile() + " logFile.log ] "
                + "[ " + cliSharedParameters.getParamConfigFilePath() + " config ] ";
    }

    OptionGroup getSASTScanParamsOptionGroup() {
        OptionGroup sastParamsOptionGroup = new OptionGroup();
        for (Option opt : commandLineOptions.getOptions()) {
            sastParamsOptionGroup.addOption(opt);
        }

        return sastParamsOptionGroup;
    }
}
