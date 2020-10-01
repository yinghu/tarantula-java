package com.tarantula.platform;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RoutingKey;

/**
 * Updated by yinghu lu on 8/9/2019.
 */
public class InstanceRoutingKey implements RoutingKey {

    private String bucket;
    private int routingNumber;
    private String instanceId;

    public InstanceRoutingKey(String applicationId,String instanceId,int routingNumber){
        this.bucket = applicationId;
        this.instanceId = instanceId;
        this.routingNumber = routingNumber;
    }
    @Override
    public String source(){
        return this.instanceId;
    }
    @Override
    public String bucket(){
        return bucket;
    }
    @Override
    public int routingNumber() {
        return routingNumber;
    }
    @Override
    public String route(){
        return new StringBuffer(bucket).append(Recoverable.PATH_SEPARATOR).append(routingNumber).toString();
    }

}
