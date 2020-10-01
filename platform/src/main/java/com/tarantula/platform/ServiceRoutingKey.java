package com.tarantula.platform;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RoutingKey;

/**
 * Updated by yinghu lu on 8/9/2019.
 */
public class ServiceRoutingKey implements RoutingKey {

    private String bucket;
    private String tag;
    private int routingNumber;

    public ServiceRoutingKey(String bucket, String tag, int routingNumber){
        this.bucket = bucket;
        this.tag = tag;
        this.routingNumber = routingNumber;
    }
    @Override
    public String source(){
        return this.tag;
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
        return new StringBuffer(bucket).append(Recoverable.PATH_SEPARATOR).append(tag).append(Recoverable.PATH_SEPARATOR).append(routingNumber).toString();
    }

}
