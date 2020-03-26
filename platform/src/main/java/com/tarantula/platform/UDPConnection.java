package com.tarantula.platform;


import com.tarantula.platform.service.cluster.PortableRegistry;
import com.tarantula.platform.util.SystemUtil;

import java.util.Map;

public class UDPConnection extends WebSocketConnection {


    public UDPConnection(){

    }
    public UDPConnection(String serverId, String host, int port){
        this.serverId = serverId;
        this.host = host;
        this.port = port;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("serverId",this.serverId);
        this.properties.put("host",this.host);
        this.properties.put("port",this.port);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.serverId = (String)properties.get("serverId");
        this.host = (String)properties.get("host");
        this.port = ((Number)properties.get("port")).intValue();
    }
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    public int getClassId() {
        return -1;
    }
    public Key key(){
        return new NaturalKey(this.serverId);
    }
    public String toString(){
        return new String(SystemUtil.toJson(toMap()));
    }
}
