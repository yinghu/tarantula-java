package com.tarantula.platform;

/**
 * Created by yinghu lu on 3/20/2019.
 */
public class SessionIdle extends OnApplicationHeader {


    public SessionIdle(String label,String systemId,int stub){
        this.label = label;
        this.systemId = systemId;
        this.stub = stub;
    }
    public SessionIdle(String label,String systemId,int stub,String instanceId){
        this.label = label;
        this.systemId = systemId;
        this.stub = stub;
        this.instanceId = instanceId;
    }
}
