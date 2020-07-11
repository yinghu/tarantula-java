package com.tarantula.platform;

import com.tarantula.OnInstance;
import com.tarantula.RecoverableFactory;
import com.tarantula.platform.service.cluster.PortableRegistry;

/**
 * Updated by yinghu on 8/23/19.
 */
public class OnInstanceQuery implements RecoverableFactory<OnInstance> {

    String instanceId;

    public OnInstanceQuery(String instanceId){
        this.instanceId = instanceId;
    }

    public OnInstance create() {
        OnInstanceTrack ocx = new OnInstanceTrack();
        return ocx;
    }

    public String distributionKey() {
        return this.instanceId;
    }


    public  int registryId(){
        return PortableRegistry.ON_INSTANCE_CID;
    }

    public String label(){
        return OnInstance.LABEL;
    }
}
