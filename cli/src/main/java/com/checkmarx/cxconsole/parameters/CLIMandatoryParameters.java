package com.checkmarx.cxconsole.parameters;

import com.checkmarx.cxconsole.clients.general.dto.ProjectDTO;
import com.checkmarx.cxconsole.clients.general.dto.TeamDTO;
import com.checkmarx.cxconsole.commands.exceptions.CLICommandParameterValidatorException;
import com.checkmarx.cxconsole.parameters.exceptions.CLIParameterParsingException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

/**
 * Created by nirli on 29/10/2017.
 */
public class CLIMandatoryParameters extends AbstractCLIScanParameters {

    /**
     * Definition of command line parameters to be used by Apache CLI parser
     */
    private Options commandLineOptions;

    private String host;
    private String originalHost;
    private String username;
    private String password;
    private String token;
    private String projectName;
    private ProjectDTO project;
    private TeamDTO team;
    private String srcPath;
    private String folderProjectName;
    private boolean hasPasswordParam = false;
    private boolean hasUserParam = false;
    private boolean hasTokenParam = false;

    private static final Option PARAM_HOST = Option.builder("cxserver").hasArg(true).argName("server").desc("IP address or resolvable name of CxSuite web server").build();
    private static final Option PARAM_USER = Option.builder("cxuser").hasArg(true).argName("username").desc("Login username. Mandatory, Unless token is used or SSO login is used on Windows ('-useSSO' flag)").build();
    private static final Option PARAM_PASSWORD = Option.builder("cxpassword").hasArg(true).argName("password").desc("Login password. Mandatory, Unless token is used or SSO login is used on Windows ('-useSSO' flag)").build();
    private static final Option PARAM_TOKEN = Option.builder("cxtoken").hasArg(true).argName("token").desc("Login token. Mandatory, Unless use rname and password are provided or SSO login is used on Windows ('-useSSO' flag)").build();
    private static final Option PARAM_PROJECT_NAME = Option.builder("projectname").argName("project name").hasArg(true).desc("A full absolute name of a project. " +
            "The full Project name includes the whole path to the project, including Server, service provider, company, and team. " +
            "Example:  -ProjectName \"CxServer\\SP\\Company\\Users\\bs java\" " +
            "If project with such a name doesn't exist in the system, new project will be created.").build();


    CLIMandatoryParameters() throws CLIParameterParsingException {
        initCommandLineOptions();
    }

    void initMandatoryParams(CommandLine parsedCommandLineArguments) throws CLICommandParameterValidatorException {
        host = parsedCommandLineArguments.getOptionValue(PARAM_HOST.getOpt());
        originalHost = parsedCommandLineArguments.getOptionValue(PARAM_HOST.getOpt());
        username = parsedCommandLineArguments.getOptionValue(PARAM_USER.getOpt());
        password = parsedCommandLineArguments.getOptionValue(PARAM_PASSWORD.getOpt());
        token = parsedCommandLineArguments.getOptionValue(PARAM_TOKEN.getOpt());

        hasUserParam = parsedCommandLineArguments.hasOption(PARAM_USER.getOpt());
        hasPasswordParam = parsedCommandLineArguments.hasOption(PARAM_PASSWORD.getOpt());
        hasTokenParam = parsedCommandLineArguments.hasOption(PARAM_TOKEN.getOpt());

        String projectNameWithTeamPath = parsedCommandLineArguments.getOptionValue(PARAM_PROJECT_NAME.getOpt());
        if (projectNameWithTeamPath != null) {
            projectNameWithTeamPath = projectNameWithTeamPath.replaceAll("/", "\\\\");
            projectName = extractProjectName(projectNameWithTeamPath);
            team = extractTeamPath(projectNameWithTeamPath, projectName);

            project = new ProjectDTO(projectName);
        }
    }

    private String extractProjectName(String projectNameWithFullPath) {
        String[] pathParts = projectNameWithFullPath.split("\\\\");
        if ((pathParts.length <= 0)) {
            return projectNameWithFullPath;
        } else {
            return pathParts[pathParts.length - 1];
        }
    }

    private TeamDTO extractTeamPath(String fullPath, String project) throws CLICommandParameterValidatorException {
        final int projectNameIndex = fullPath.lastIndexOf("\\" + project);
        if (-1 == projectNameIndex) {
            throw new CLICommandParameterValidatorException("Please provide team and project in the format TEAM_NAME\\PROJECT_NAME. Provided: " + fullPath);
        }
        final String teamPath = fullPath.substring(0, projectNameIndex);
        return !teamPath.startsWith("\\") ? new TeamDTO("\\" + teamPath) : new TeamDTO(teamPath);
    }


    public String getHost() {
        return host;
    }

    public String getOriginalHost() {
        return originalHost;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }

    public ProjectDTO getProject() {
        return project;
    }

    public String getFolderProjectName() {
        return folderProjectName;
    }

    public boolean isHasPasswordParam() {
        return hasPasswordParam;
    }

    public boolean isHasUserParam() {
        return hasUserParam;
    }

    public boolean isHasTokenParam() {
        return hasTokenParam;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setOriginalHost(String originalHost) {
        this.originalHost = originalHost;
    }

    public String getSrcPath() {
        return srcPath;
    }

    public TeamDTO getTeam() {
        return team;
    }

    @Override
    void initCommandLineOptions() {
        commandLineOptions = new Options();
        commandLineOptions.addOption(PARAM_HOST);
        commandLineOptions.addOption(PARAM_PASSWORD);
        commandLineOptions.addOption(PARAM_PROJECT_NAME);
        commandLineOptions.addOption(PARAM_TOKEN);
        commandLineOptions.addOption(PARAM_USER);
    }

    OptionGroup getMandatoryParamsOptionGroup() {
        OptionGroup mandatoryParamsOptionGroup = new OptionGroup();
        for (Option opt : commandLineOptions.getOptions()) {
            mandatoryParamsOptionGroup.addOption(opt);
        }

        return mandatoryParamsOptionGroup;
    }

    public Options getGenerateTokenMandatoryParamsOptionGroup() {
        Options mandatoryParamsOptions = new Options();
        mandatoryParamsOptions.addOption(PARAM_HOST);
        mandatoryParamsOptions.addOption(PARAM_USER);
        mandatoryParamsOptions.addOption(PARAM_PASSWORD);

        return mandatoryParamsOptions;
    }

    public Options getRevokeTokenMandatoryParamsOptions() {
        Options mandatoryParamsOptions = new Options();
        mandatoryParamsOptions.addOption(PARAM_HOST);
        mandatoryParamsOptions.addOption(PARAM_TOKEN);

        return mandatoryParamsOptions;
    }

    public String getMandatoryParams() {
        return PARAM_HOST + " hostName " + PARAM_USER + " login "
                + PARAM_PASSWORD + " password, or" + PARAM_TOKEN + " token. " + PARAM_PROJECT_NAME + " fullProjectName ";
    }

    public String getMandatoryParamsGenerateToken() {
        return PARAM_HOST + " hostName " + PARAM_USER + " login "
                + PARAM_PASSWORD + " password ";
    }

    public String getMandatoryParamsRevokeToken() {
        return PARAM_HOST + " hostName " + PARAM_TOKEN + " token";
    }

    public Options getCommandLineOptions() {
        return commandLineOptions;
    }
}
