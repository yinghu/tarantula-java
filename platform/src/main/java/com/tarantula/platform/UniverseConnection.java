package com.tarantula.platform;

import com.icodesoftware.Connection;
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
        this.properties.put("connectionId",this.connectionId);
        this.properties.put("sequence",this.sequence);
        this.properties.put("secured",this.secured);
        this.properties.put("protocol",this.protocol);
        this.properties.put("host",this.host);
        this.properties.put("port",this.port);
        this.properties.put("path",this.path);
        this.properties.put("maxConnections",maxConnections);
        this.properties.put("disabled",this.disabled);
        this.properties.put("messageId",this.messageId);
        this.properties.put("messageIdOffset",this.messageIdOffset);
        this.properties.put("hasServer",server!=null);
        if(server!=null){
            this.properties.put("stype",this.server.type());
            this.properties.put("ssecured",this.server.secured());
            this.properties.put("sprotocol",this.server.protocol());
            this.properties.put("shost",this.server.host());
            this.properties.put("sport",this.server.port());
            this.properties.put("spath",this.server.path());
            this.properties.put("ssessionId",this.server.sessionId());
        }
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.type = (String)properties.get("type");
        this.serverId = (String)properties.get("serverId");
        this.connectionId = ((Number)properties.get("connectionId")).intValue();
        this.sequence = ((Number)properties.get("sequence")).intValue();
        this.secured =(Boolean)properties.get("secured");
        this.protocol = (String)properties.get("protocol");
        this.host = (String)properties.get("host");
        this.port = ((Number)properties.get("port")).intValue();
        this.path = (String)properties.get("path");
        this.maxConnections = ((Number)properties.get("maxConnections")).intValue();
        this.disabled = (boolean)properties.get("disabled");
        this.messageId = ((Number)properties.get("messageId")).intValue();
        this.messageIdOffset = ((Number)properties.get("messageIdOffset")).intValue();
        if((Boolean) properties.get("hasServer")){
            server = new UniverseConnection();
            server.type((String) properties.get("stype"));
            server.secured((Boolean)properties.get("secured"));
            server.protocol((String)properties.get("sprotocol"));
            server.host((String)properties.get("shost"));
            server.port(((Number)properties.get("sport")).intValue());
            server.path((String)properties.get("spath"));
            server.sessionId(((Number)properties.get("ssessionId")).intValue());
            server.messageId(this.messageId);
            server.messageIdOffset(this.messageIdOffset);
        }
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
    public Connection server(){
        return server!=null?server:this;
    }
    public void server(Connection connection){
        this.server = connection;
    }

}
