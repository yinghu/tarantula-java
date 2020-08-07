package com.tarantula.platform.service.deployment;

import com.tarantula.Configuration;
import com.tarantula.RecoverableFactory;
import com.tarantula.platform.ApplicationConfiguration;
import com.tarantula.platform.service.cluster.PortableRegistry;


/**
 * Updated by yinghu lu on 7/19/2020.
 */
public class ApplicationConfigurationQuery implements RecoverableFactory<Configuration> {


    private String lobbyId;

    public ApplicationConfigurationQuery(String lobbyId){
        this.lobbyId = lobbyId;
    }

    @Override
    public Configuration create() {
        ApplicationConfiguration ac = new ApplicationConfiguration();
        return ac;
    }

    @Override
    public int registryId() {
        return PortableRegistry.APPLICATION_CONFIGURATION_CID;
    }

    @Override
    public String label() {
        return Configuration.LABEL;
    }


    @Override
    public String distributionKey() {
        return lobbyId;
    }
}
