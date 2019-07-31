package com.checkmarx.cxconsole.parameters;

import com.checkmarx.cxconsole.commands.exceptions.CLICommandParameterValidatorException;
import com.checkmarx.cxconsole.parameters.exceptions.CLIParameterParsingException;
import com.checkmarx.cxconsole.parameters.utils.ParametersUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import static com.checkmarx.cxconsole.CxConsoleLauncher.getArgumentsLessCommandName;

/**
 * Created by nirli on 30/10/2017.
 */
public class CLIScanParametersSingleton {

    private static final CLIScanParametersSingleton instance;

    static {
        try {
            instance = new CLIScanParametersSingleton();
        } catch (CLIParameterParsingException e) {
            throw new ExceptionInInitializerError(e.getMessage());
        } catch (CLICommandParameterValidatorException e) {
            throw new ExceptionInInitializerError(e.getMessage());
        }
    }

    private CLIScanParametersSingleton() throws CLIParameterParsingException, CLICommandParameterValidatorException {
        String[] args = getArgumentsLessCommandName();
        cliMandatoryParameters = new CLIMandatoryParameters();
        cliSharedParameters = new CLISharedParameters();
        cliSastParameters = new CLISASTParameters();
        cliOsaParameters = new CLIOSAParameters();

        parsedCommandLineArguments = ParametersUtils.parseArguments(args, getAllCLIOptions());
        cliMandatoryParameters.initMandatoryParams(parsedCommandLineArguments);
        cliSharedParameters.initSharedParams(parsedCommandLineArguments);
        cliSastParameters.initSastParams(parsedCommandLineArguments, cliSharedParameters.getLocationType());
        cliOsaParameters.initOsaParams(parsedCommandLineArguments);
        if (parsedCommandLineArguments.getArgs().length > 0) {
            throw new CLIParameterParsingException("Unable to parse due to missing or incorrect parameters. Recheck the provided parameters and try again.");
        }
    }

    public static CLIScanParametersSingleton getCLIScanParameter() {
        return instance;
    }

    private CLIMandatoryParameters cliMandatoryParameters;
    private CLISharedParameters cliSharedParameters;
    private CLISASTParameters cliSastParameters;
    private CLIOSAParameters cliOsaParameters;

    private CommandLine parsedCommandLineArguments;

    public CLIMandatoryParameters getCliMandatoryParameters() {
        return cliMandatoryParameters;
    }

    public CLISharedParameters getCliSharedParameters() {
        return cliSharedParameters;
    }

    public CLISASTParameters getCliSastParameters() {
        return cliSastParameters;
    }

    public CLIOSAParameters getCliOsaParameters() {
        return cliOsaParameters;
    }

    public CommandLine getParsedCommandLineArguments() {
        return parsedCommandLineArguments;
    }

    public Options getAllCLIOptions() {
        Options allParamsOption = new Options();
        for (Option opt : cliMandatoryParameters.getMandatoryParamsOptionGroup().getOptions()) {
            allParamsOption.addOption(opt);
        }
        for (Option opt : cliSharedParameters.getSharedParamsOptionGroup().getOptions()) {
            allParamsOption.addOption(opt);
        }
        for (Option opt : cliSastParameters.getSASTScanParamsOptionGroup().getOptions()) {
            allParamsOption.addOption(opt);
        }
        for (Option opt : cliOsaParameters.getOSAScanParamsOptionGroup().getOptions()) {
            allParamsOption.addOption(opt);
        }

        return allParamsOption;
    }

}