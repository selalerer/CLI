package com.checkmarx.cxconsole.clients.general;

import com.checkmarx.cxconsole.clients.general.dto.ProjectDTO;
import com.checkmarx.cxconsole.clients.general.dto.TeamDTO;
import com.checkmarx.cxconsole.clients.general.exception.CxRestGeneralClientException;

import java.util.List;

/**
 * Created by nirli on 01/03/2018.
 */
public interface CxRestGeneralClient {

    List<TeamDTO> getTeams() throws CxRestGeneralClientException;

    List<ProjectDTO> getProjects() throws CxRestGeneralClientException;

    int createNewProject(ProjectDTO projectToCreate) throws CxRestGeneralClientException;

    boolean isLoggedIn();
}