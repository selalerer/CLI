package com.checkmarx.cxconsole;

import com.checkmarx.cxconsole.clients.login.utils.SSLUtilities;
import com.checkmarx.cxconsole.commands.CLICommand;
import com.checkmarx.cxconsole.commands.CommandFactory;
import com.checkmarx.cxconsole.commands.exceptions.CLICommandException;
import com.checkmarx.cxconsole.commands.exceptions.CLICommandFactoryException;
import com.checkmarx.cxconsole.commands.exceptions.CLICommandParameterValidatorException;
import com.checkmarx.cxconsole.parameters.CLIScanParametersSingleton;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import com.checkmarx.cxconsole.utils.ConsoleUtils;
import com.checkmarx.cxconsole.utils.CustomStringList;
import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import java.util.ArrayList;
import java.util.Arrays;

import static com.checkmarx.cxconsole.exitcodes.Constants.ExitCodes.GENERAL_ERROR_EXIT_CODE;
import static com.checkmarx.cxconsole.exitcodes.Constants.ExitCodes.SCAN_SUCCEEDED_EXIT_CODE;
import static com.checkmarx.cxconsole.exitcodes.ErrorHandler.errorCodeResolver;
import static com.checkmarx.cxconsole.exitcodes.ErrorHandler.errorMsgResolver;

/**
 * @author Oleksiy Mysnyk
 */
public class CxConsoleLauncher {

    private static Logger log = Logger.getLogger(CxConsoleLauncher.class);

    private static final String INVALID_COMMAND_PARAMETERS_MSG = "Command parameters are invalid: ";
    private static String[] argumentsLessCommandName;

    /**
     * Entry point to CxScan Console
     *
     * @param args
     */
    public static void main(String[] args) {
        int exitCode = -1;
        DOMConfigurator.configure("./log4j.xml");

        exitCode = runCli(args);
        if (exitCode == SCAN_SUCCEEDED_EXIT_CODE) {
            log.info("Job completed successfully - exit code " + exitCode);
        } else {
            log.error("Failure - " + errorMsgResolver(exitCode) + " - error code " + exitCode);
        }

        System.exit(exitCode);
    }

    /**
     * Entry point to CxScan Console that returns exitCode
     * This entry point is used by Jenkins plugin
     *
     * @param args
     */
    public static int runCli(String[] args) {

        if (args == null || args.length == 0) {
            log.fatal("Missing command name. Available commands: " + CommandFactory.getCommandNames());
            return GENERAL_ERROR_EXIT_CODE;
        }

        validateVerboseCommand(args);

        log.info("CxConsole version " + ConsoleUtils.getBuildVersion());
        log.info("CxConsole scan session started");
        log.info("");

        initConfigurationManager(args);

        // Temporary solution
        SSLUtilities.trustAllHostnames();
        SSLUtilities.trustAllHttpsCertificates();

        String commandName = args[0];
        argumentsLessCommandName = java.util.Arrays.copyOfRange(args, 1, args.length);
        makeArgumentsLowCase(argumentsLessCommandName);
        CLICommand command = null;
        CLIScanParametersSingleton cliScanParametersSingleton;
        try {
            CommandFactory.verifyCommand(commandName);
            cliScanParametersSingleton = CLIScanParametersSingleton.getCLIScanParameter();
            command = CommandFactory.getCommand(commandName, cliScanParametersSingleton);
            command.checkParameters();
            log.trace("Parameters were checked successfully");
        } catch (CLICommandParameterValidatorException e) {
            command.printHelp();
            log.fatal(INVALID_COMMAND_PARAMETERS_MSG + e.getMessage() + "\n");
            return errorCodeResolver(e.getMessage());
        } catch (ExceptionInInitializerError | CLICommandFactoryException e) {
            log.fatal(e.getMessage());
            return errorCodeResolver(e.getMessage());
        }

        int exitCode;
        try {
            exitCode = command.execute();
            log.info("CxConsole session finished");
            return exitCode;
        } catch (CLICommandException e) {
            log.error(e.getMessage());
            return errorCodeResolver(e.getMessage());
        }
    }

    private static void makeArgumentsLowCase(String[] argumentsLessCommandName) {
        for (int i = 0; i < argumentsLessCommandName.length; i++) {
            if (argumentsLessCommandName[i].startsWith("-")) {
                argumentsLessCommandName[i] = argumentsLessCommandName[i].toLowerCase();
            }
        }
    }

    private static void initConfigurationManager(String[] args) {
        int configIndx = Arrays.asList(args).indexOf("-config");
        String confPath = null;
        if (configIndx != -1 && args.length > (configIndx + 1) && args[configIndx + 1] != null && !args[configIndx + 1].startsWith("-")) {
            confPath = args[configIndx + 1];
        }
        ConfigMgr.initCfgMgr(confPath);
    }


    private static void validateVerboseCommand(String[] args) {
        ArrayList<String> customArgs = new CustomStringList(Arrays.asList(args));
        if (!customArgs.contains("-v".trim()) && !customArgs.contains("-verbose")) {
            Appender caAppender = Logger.getRootLogger().getAppender("CA");
            ((ConsoleAppender) caAppender).setThreshold(Level.ERROR);
        } else {
            log.info("Verbose mode is activated. All messages and events will be sent to the console or log file.");
        }
    }

    public static String[] getArgumentsLessCommandName() {
        return argumentsLessCommandName;
    }
}