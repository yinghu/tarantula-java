package com.tarantula.integration.udp;

import java.net.SocketAddress;

public class Session {
    public String systemId;
    public int stub;
    public String token;
    public SocketAddress endpoint;

    public Session(String systemId){
        this.systemId = systemId;
    }

    public Session(String systemId,int stub,String token,SocketAddress socketAddress){
        this.systemId = systemId;
        this.stub = stub;
        this.token = token;
        this.endpoint = socketAddress;
    }
    @Override
    public int hashCode(){
        return systemId.hashCode();
    }
    @Override
    public boolean equals(Object object){
        return systemId.equals(((Session)object).systemId);
    }
    public String toString(){
        return systemId;
    }

}
