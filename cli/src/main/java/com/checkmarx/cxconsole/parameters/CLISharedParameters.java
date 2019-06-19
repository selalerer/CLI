package com.checkmarx.cxconsole.parameters;

import com.checkmarx.cxconsole.commands.constants.LocationType;
import com.checkmarx.cxconsole.parameters.exceptions.CLIParameterParsingException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import java.io.File;

/**
 * Created by nirli on 29/10/2017.
 */
public class CLISharedParameters extends AbstractCLIScanParameters {

    /**
     * Definition of command line parameters to be used by Apache CLI parser
     */
    private Options commandLineOptions;

    private String logFilePath = null;
    private boolean isVerbose = false;
    private String scanComment;
    private boolean isSsoLoginUsed = false;
    private boolean isVisibleOthers = true;
    private boolean trustAllCertificates = true;

    private LocationType locationType;
    private String locationPath;
    private String spFolderName;

    private static final Option PARAM_TRUSTED_CERT = Option.builder("trustedcertificates").desc("Only accept trusted certificates").build();
    private static final Option PARAM_VERBOSE = Option.builder("v").desc("Turns on verbose mode. All messages and events will be sent to the console/log file.  Optional.")
            .longOpt("verbose").hasArg(false).build();
    private static final Option PARAM_LOG_FILE_PATH = Option.builder("log").hasArg().argName("file").desc("Log file path. Optional.").build();
    private static final Option PARAM_PRIVATE = Option.builder("private").desc("Scan will not be visible to other users. Optional.").build();
    private static final Option PARAM_USE_SSO = Option.builder("usesso").desc("SSO login method is used, available only on Windows. Optional.").build();
    private static final Option PARAM_SCAN_COMMENT = Option.builder("comment").argName("text").desc("Scan comment. Example: -comment 'important scan1'. Optional.")
            .hasArg().build();
    private static final Option PARAM_CONFIG_FILE_PATH = Option.builder("config").hasArg().argName("file").desc("Config file path. Optional.").build();
    private static final Option PARAM_LOCATION_TYPE = Option.builder("locationtype").argName(LocationType.stringOfValues()).hasArg()
            .desc("Source location type: folder, shared, SVN, TFS, GIT, Perforce").build();
    private static final Option PARAM_LOCATION_PATH = Option.builder("locationpath").argName("path").hasArg()
            .desc("Local or shared path to sources or source repository branch. Required if -LocationType is folder/shared.").build();


    CLISharedParameters() throws CLIParameterParsingException {
        initCommandLineOptions();
    }

    void initSharedParams(CommandLine parsedCommandLineArguments) {
        logFilePath = parsedCommandLineArguments.getOptionValue(PARAM_LOG_FILE_PATH.getOpt());
        isVerbose = parsedCommandLineArguments.hasOption(PARAM_VERBOSE.getOpt());
        scanComment = parsedCommandLineArguments.getOptionValue(PARAM_SCAN_COMMENT.getOpt());
        isSsoLoginUsed = parsedCommandLineArguments.hasOption(PARAM_USE_SSO.getOpt());
        isVisibleOthers = !parsedCommandLineArguments.hasOption(PARAM_PRIVATE.getOpt());
        trustAllCertificates = !parsedCommandLineArguments.hasOption(PARAM_TRUSTED_CERT.getOpt());

        if (parsedCommandLineArguments.hasOption(PARAM_LOCATION_TYPE.getOpt())) {
            locationType = LocationType.byName(parsedCommandLineArguments.getOptionValue(PARAM_LOCATION_TYPE.getOpt()));
        }

        locationPath = parsedCommandLineArguments.getOptionValue(PARAM_LOCATION_PATH.getOpt());
        if (locationType == LocationType.FOLDER && locationPath != null) {
            File resultFile = new File(locationPath);
            if (!resultFile.isAbsolute()) {
                String path = System.getProperty("user.dir");
                locationPath = path + File.separator + locationPath;
            }
        }
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public boolean isVerbose() {
        return isVerbose;
    }

    public String getScanComment() {
        return scanComment;
    }

    public boolean isSsoLoginUsed() {
        return isSsoLoginUsed;
    }

    public boolean isVisibleOthers() {
        return isVisibleOthers;
    }

    public Options getCommandLineOptions() {
        return commandLineOptions;
    }

    public LocationType getLocationType() {
        return locationType;
    }

    public String getLocationPath() {
        return locationPath;
    }

    public String getSpFolderName() {
        return spFolderName;
    }

    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }

    public void setLocationPath(String locationPath) {
        this.locationPath = locationPath;
    }

    public Option getParamLogFile() {
        return PARAM_LOG_FILE_PATH;
    }

    public Option getParamLocationType() {
        return PARAM_LOCATION_TYPE;
    }

    public Option getParamConfigFilePath() {
        return PARAM_CONFIG_FILE_PATH;
    }

    @Override
    void initCommandLineOptions() {
        commandLineOptions = new Options();
        commandLineOptions.addOption(PARAM_VERBOSE);
        commandLineOptions.addOption(PARAM_LOG_FILE_PATH);
        commandLineOptions.addOption(PARAM_CONFIG_FILE_PATH);
        commandLineOptions.addOption(PARAM_LOCATION_TYPE);
        commandLineOptions.addOption(PARAM_LOCATION_PATH);
        commandLineOptions.addOption(PARAM_PRIVATE);
        commandLineOptions.addOption(PARAM_SCAN_COMMENT);
        commandLineOptions.addOption(PARAM_USE_SSO);
        commandLineOptions.addOption(PARAM_TRUSTED_CERT);
    }

    OptionGroup getSharedParamsOptionGroup() {
        OptionGroup sharedParamsOptionGroup = new OptionGroup();
        for (Option opt : commandLineOptions.getOptions()) {
            sharedParamsOptionGroup.addOption(opt);
        }

        return sharedParamsOptionGroup;
    }

    @Override
    public String getMandatoryParams() {
        return null;
    }

    public boolean isTrustAllCertificates() {
        return trustAllCertificates;
    }

}