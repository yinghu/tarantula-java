package com.tarantula.platform.service.deployment;

import com.tarantula.Configuration;
import com.tarantula.RecoverableFactory;
import com.tarantula.platform.ApplicationConfiguration;
import com.tarantula.platform.service.cluster.PortableRegistry;
import com.tarantula.platform.util.SystemUtil;

/**
 * Updated by yinghu lu on 7/19/2020.
 */
public class ApplicationConfigurationQuery implements RecoverableFactory<ApplicationConfiguration> {


    private String serviceConfigurationId;
    private String tag;
    public ApplicationConfigurationQuery(String serviceConfigurationId,String tag){
        this.serviceConfigurationId = serviceConfigurationId;
        this.tag = tag;
    }

    @Override
    public ApplicationConfiguration create() {
        ApplicationConfiguration ac = new ApplicationConfiguration();
        return ac;
    }

    @Override
    public int registryId() {
        return PortableRegistry.APPLICATION_CONFIGURATION_CID;
    }

    @Override
    public String label() {
        return SystemUtil.toString(new String[]{Configuration.LABEL,tag});
    }


    @Override
    public String distributionKey() {
        return serviceConfigurationId;
    }
}
