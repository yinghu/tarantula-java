package com.tarantula.platform.service.deployment;

import com.icodesoftware.Configuration;
import com.icodesoftware.RecoverableFactory;
import com.tarantula.platform.ApplicationConfiguration;
import com.tarantula.platform.service.cluster.PortableRegistry;

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
