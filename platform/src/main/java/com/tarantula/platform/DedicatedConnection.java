package com.tarantula.platform;

import com.google.gson.JsonObject;
import com.tarantula.Connection;
import com.tarantula.platform.service.cluster.PortableRegistry;
import com.tarantula.platform.util.SystemUtil;

import java.util.Map;

public class DedicatedConnection extends ResponseHeader implements Connection {

    private String serverId;
    private String host;
    private int port;

    public DedicatedConnection(){

    }
    public DedicatedConnection(String serverId,String host,int port){
        this.serverId = serverId;
        this.host = host;
        this.port = port;
    }

    @Override
    public String type() {
        return "";
    }

    @Override
    public void type(String type) {

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
        return false;
    }

    @Override
    public void secured(boolean secured) {

    }

    @Override
    public String protocol() {
        return "udp";
    }

    @Override
    public void protocol(String protocol) {
    }
    public String subProtocol(){
        return "tarantula-service";
    }
    public void subProtocol(String subProtocol){

    }
    public String path(){
        return "";
    }
    public void path(String path){

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
    public int maxConnections(){
        return 0;
    }
    public void maxConnections(int maxConnections){

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
