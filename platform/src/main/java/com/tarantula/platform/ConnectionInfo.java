package com.tarantula.platform;

import com.tarantula.Connection;
import com.tarantula.platform.service.cluster.PortableRegistry;
import com.tarantula.platform.util.SystemUtil;

import java.util.Map;

public class ConnectionInfo extends ResponseHeader implements Connection {

    private String type;
    private String serverId;
    private boolean secured;
    private String protocol;
    private String host;
    private int port;
    private String path;
    @Override
    public String type() {
        return this.type;
    }

    @Override
    public void type(String type) {
        this.type = type;
    }

    @Override
    public String serverId() {
        return this.serverId;
    }

    @Override
    public void serverId(String serverId) {
        this.serverId = serverId;
    }

    @Override
    public boolean secured() {
        return secured;
    }

    @Override
    public void secured(boolean secured) {
        this.secured = secured;
    }

    @Override
    public String protocol() {
        return protocol;
    }

    @Override
    public void protocol(String protocol) {
        this.protocol = protocol;
    }
    public String path(){
        return this.path;
    }
    public void path(String path){
        this.path = path;
    }
    @Override
    public String host() {
        return this.host;
    }

    @Override
    public void host(String host) {
        this.host = host;
    }

    @Override
    public int port() {
        return this.port;
    }

    @Override
    public void port(int port) {
        this.port = port;
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
        properties.forEach((String k,Object v)->{
            this.properties.put(k,v);
        });
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
        return new String(SystemUtil.toJson(toMap()));
    }
}
