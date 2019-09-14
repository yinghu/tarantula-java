package com.tarantula.integration.udp;

public class Session {
    public String systemId;
    public int stub;
    public String instanceId;
    public String token;

    public Session(String systemId,String instanceId){
        this.systemId = systemId;
        this.instanceId = instanceId;
    }

    public Session(String systemId,int stub,String instanceId,String token){
        this.systemId = systemId;
        this.stub = stub;
        this.instanceId = instanceId;
        this.token = token;
    }
    @Override
    public int hashCode(){
        return (systemId+instanceId).hashCode();
    }
    @Override
    public boolean equals(Object object){
        return systemId.equals(((Session)object).systemId)&&instanceId.equals(((Session)object).instanceId);
    }
    public String toString(){
        return systemId+"<>"+instanceId;
    }

}
