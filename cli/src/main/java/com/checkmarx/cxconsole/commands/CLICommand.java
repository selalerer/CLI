package com.checkmarx.cxconsole.commands;

import com.checkmarx.cxconsole.clients.exception.CxRestClientException;
import com.checkmarx.cxconsole.commands.exceptions.CLICommandException;
import com.checkmarx.cxconsole.commands.exceptions.CLICommandParameterValidatorException;
import com.checkmarx.cxconsole.parameters.CLIScanParametersSingleton;
import com.checkmarx.cxconsole.utils.LoggerUtils;
import com.google.common.base.Strings;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.checkmarx.cxconsole.commands.utils.CommandUtils.resolveServerProtocol;
import static com.checkmarx.cxconsole.exitcodes.Constants.ErrorMassages.SERVER_CONNECTIVITY_VALIDATION_ERROR;
import static com.checkmarx.cxconsole.exitcodes.ErrorHandler.errorCodeResolver;

/**
 * Created by nirli on 30/10/2017.
 */
public abstract class CLICommand {

    private static Logger log = Logger.getLogger(CLICommand.class);

    protected CLIScanParametersSingleton params;

    int exitCode;

    String commandName;

    Integer timeoutInSeconds;

    ExecutorService executor = Executors.newSingleThreadExecutor();

    HelpFormatter helpFormatter = new HelpFormatter();

    boolean isAsyncScan = false;

    private static final int UNASSIGNED_EXIT_CODE = -1;

    CLICommand(CLIScanParametersSingleton params) {
        this.params = params;
        exitCode = UNASSIGNED_EXIT_CODE;
        initHelpMessage();
    }

    private void initHelpMessage() {
        helpFormatter.setLeftPadding(4);
    }

    public final int execute() throws CLICommandException {

        if (!Strings.isNullOrEmpty(params.getCliSharedParameters().getLogFilePath())) {
            try {
                initLogging();
            } catch (IOException e) {
                log.error("Can't create new log file to path: " + params.getCliSharedParameters().getLogFilePath());
            }
        } else {
            log.info("Default log file location: " + System.getProperty("user.dir") + File.separator + "logs\\cx_console.log");
        }

        try {
            String hostWithProtocol = resolveServerProtocol(params.getCliMandatoryParameters().getOriginalHost());
            params.getCliMandatoryParameters().setOriginalHost(hostWithProtocol);
            log.info("Server connectivity test succeeded to: " + params.getCliMandatoryParameters().getOriginalHost());
        } catch (CxRestClientException e) {
            throw new CLICommandException(SERVER_CONNECTIVITY_VALIDATION_ERROR + e.getMessage());
        }

        printCommandsDebug();
        try {
            return executeCommand();
        } catch (CLICommandException e) {
            return errorCodeResolver(e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Command specific operations. Should be implemented by every
     * complete executable command.
     */
    protected abstract int executeCommand() throws CLICommandException;

    public abstract void checkParameters() throws CLICommandParameterValidatorException;

    private void printCommandsDebug() {
        log.debug("----------------------------Configured Commands:-----------------------------");
        log.debug("Cx CLI plugin version 8.70.0");
        log.debug("Command type: " + getCommandName());
        for (Option opt : params.getParsedCommandLineArguments().getOptions()) {
            String option = opt.getOpt();
            if (!Objects.equals(option, "cxpassword") && !Objects.equals(option, "locationpassword")) {
                if (opt.getValue() == null) {
                    log.debug("Option: " + StringUtils.capitalize(opt.getOpt()) + "   Value: True");
                } else if (Objects.equals(option, "osalocationpath")
                        || Objects.equals(option, "osafilesexclude")
                        || Objects.equals(option, "osaarchivetoextract")
                        || Objects.equals(option, "locationpathexclude")
                        || Objects.equals(option, "locationfilesexclude")) {
                    log.debug("Option: " + StringUtils.capitalize(opt.getOpt()) + "   Value: " + StringUtils.join(opt.getValues(), ", "));
                } else {
                    log.debug("Option: " + StringUtils.capitalize(opt.getOpt()) + "   Value: " + opt.getValue());
                }
            } else if (Objects.equals(option, "cxpassword") || Objects.equals(option, "locationpassword")) {
                log.debug("Option: CxPassword   Value: **********");
            }
        }
        log.debug("-----------------------------------------------------------------------------");
    }

    public abstract String getCommandName();


    public abstract void printHelp();

    private void initLogging() throws IOException {
        String logPath = "";
        String logPathFromParam = params.getCliSharedParameters().getLogFilePath();
        logPath = LoggerUtils.getLogFileLocation(logPathFromParam, params.getCliMandatoryParameters().getProject().getName());
        Appender faAppender = Logger.getRootLogger().getAppender("FA");
        try (Writer writer = new FileWriter(logPath)) {
            ((RollingFileAppender) faAppender).setWriter(writer);
            log.info("Log file location: " + logPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract String getUsageExamples();
}