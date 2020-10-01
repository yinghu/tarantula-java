package com.tarantula.platform.service.deployment;


import com.icodesoftware.RecoverableFactory;
import com.tarantula.InstanceRegistry;
import com.tarantula.platform.InstanceIndex;
import com.tarantula.platform.service.cluster.PortableRegistry;

/**
 * Update by yinghu on 4/16/2019.
 */
public class InstanceRegistryQuery implements RecoverableFactory<InstanceIndex> {


    String applicationId;

    public InstanceRegistryQuery(String applicationId){
        this.applicationId = applicationId;
    }

    public InstanceIndex create() {
        InstanceIndex ix = new InstanceIndex();
        ix.applicationId(applicationId);
        return ix;
    }

    public String distributionKey() {
        return this.applicationId;
    }
    public  int registryId(){
        return PortableRegistry.INSTANCE_INDEX_CID;
    }

    public String label(){
        return InstanceRegistry.LABEL;
    }
}
