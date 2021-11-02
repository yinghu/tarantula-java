package com.tarantula.platform;

import com.icodesoftware.util.NaturalKey;
import com.tarantula.platform.service.cluster.PortableRegistry;
import com.icodesoftware.util.JsonUtil;
import java.util.Map;

public class UniverseConnection extends ClientConnection {


    public UniverseConnection(){

    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("type",this.type);
        this.properties.put("serverId",this.serverId);
        this.properties.put("secured",this.secured);
        this.properties.put("protocol",this.protocol);
        this.properties.put("host",this.host);
        this.properties.put("port",this.port);
        this.properties.put("path",this.path);
        this.properties.put("disabled",this.disabled);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.type = (String)properties.get("type");
        this.serverId = (String)properties.get("serverId");
        this.secured =(Boolean)properties.get("secured");
        this.protocol = (String)properties.get("protocol");
        this.host = (String)properties.get("host");
        this.port = ((Number)properties.get("port")).intValue();
        this.path = (String)properties.get("path");
        this.disabled = (boolean)properties.get("disabled");
    }
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    public int getClassId() {
        return PortableRegistry.ON_CONNECTION_CID;
    }
    public void distributionKey(String distributionKey){
    }
    public Key key(){
        return new NaturalKey(this.serverId);
    }
    public String toString(){
        return new String(JsonUtil.toJson(toMap()));
    }

}
