package com.tarantula.platform.service.deployment;

import com.tarantula.RecoverableFactory;
import com.tarantula.platform.service.cluster.PortableRegistry;
import com.tarantula.platform.util.SystemUtil;

/**
 * Created by yinghu lu on 4/10/2019
 */
public class ServiceConfigurationQuery implements RecoverableFactory<ServiceConfiguration> {

    private String bucket;

    public ServiceConfigurationQuery(String bucket){
        this.bucket = bucket;
    }

    @Override
    public ServiceConfiguration create() {
        ServiceConfiguration ac = new ServiceConfiguration();
        //ac.distributable(true);
        ac.owner(this.bucket);
        //ac.index(SystemUtil.toString(new String[]{this.distributionKey(),this.label()}));
        return ac;
    }

    @Override
    public int registryId() {
        return PortableRegistry.SERVICE_CONFIGURATION_CID;
    }

    @Override
    public String label() {
        return ServiceConfiguration.LABEL;
    }

    @Override
    public String distributionKey() {
        return bucket;
    }

}
