package com.tarantula.platform.event;

import com.tarantula.Event;
import com.tarantula.platform.Data;

/**
 * updated by yinghu on 5/10/2018
 */
public class InstanceOnRecoveryEvent extends Data implements Event {
    public InstanceOnRecoveryEvent(String applicationId,String instanceId,String systemId){
        this.applicationId = applicationId;
        this.instanceId = instanceId;
        this.systemId = systemId;
    }
}
