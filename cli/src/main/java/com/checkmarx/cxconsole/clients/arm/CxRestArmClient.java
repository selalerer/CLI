package com.checkmarx.cxconsole.clients.arm;

import com.checkmarx.cxconsole.clients.arm.dto.Policy;
import com.checkmarx.cxconsole.clients.arm.exceptions.CxRestARMClientException;

import java.util.List;

/**
 * Created by eyala on 7/9/2018.
 */
public interface CxRestArmClient {
     void close();
     List<Policy> getProjectViolations(int projectId, String provider) throws CxRestARMClientException;
     String getPolicyStatus(int projectId);
}
