package com.tarantula.platform;

import com.icodesoftware.Connection;
import com.tarantula.platform.service.cluster.PortableRegistry;
import com.tarantula.platform.util.SystemUtil;

import java.util.Map;

public class UniverseConnection extends ResponseHeader implements Connection {

    private String type;
    private String serverId;
    private long connectionId;
    private int sequence;
    private boolean secured;
    private String protocol;
    private String host;
    private int port;
    private String path;
    private int maxConnections;
    private Connection server;
    private InboundMessageListener listener;

    public UniverseConnection(){

    }

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

    public int sequence(){
        return this.sequence;
    }
    public void sequence(int sequence){
        this.sequence = sequence;
    }
    public long connectionId(){
        return this.connectionId;
    }
    public void connectionId(long connectionId){
        this.connectionId = connectionId;
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
    public String subProtocol(){
        return "tarantula-service";
    }
    public void subProtocol(String subProtocol){

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
    public int maxConnections(){
        return this.maxConnections;
    }
    public void maxConnections(int maxConnections){
        this.maxConnections = maxConnections;
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
        this.properties.put("hasServer",server!=null);
        if(server!=null){
            this.properties.put("stype",this.type);
            this.properties.put("ssecured",this.secured);
            this.properties.put("sprotocol",this.protocol);
            this.properties.put("shost",this.host);
            this.properties.put("sport",this.port);
            this.properties.put("spath",this.path);
        }
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.type = (String)properties.get("type");
        this.serverId = (String)properties.get("serverId");
        this.connectionId = ((Number)properties.get("connectionId")).longValue();
        this.sequence = ((Number)properties.get("sequence")).intValue();
        this.secured =(Boolean)properties.get("secured");
        this.protocol = (String)properties.get("protocol");
        this.host = (String)properties.get("host");
        this.port = ((Number)properties.get("port")).intValue();
        this.path = (String)properties.get("path");
        this.maxConnections = ((Number)properties.get("maxConnections")).intValue();
        this.disabled = (boolean)properties.get("disabled");
        if((Boolean) properties.get("hasServer")){
            server = new UniverseConnection();
            server.type((String) properties.get("stype"));
            server.secured((Boolean)properties.get("secured"));
            server.protocol((String)properties.get("sprotocol"));
            server.host((String)properties.get("shost"));
            server.port(((Number)properties.get("sport")).intValue());
            server.path((String)properties.get("spath"));
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
        return new String(SystemUtil.toJson(toMap()));
    }
    public Connection server(){
        return server!=null?server:this;
    }
    public void server(Connection connection){
        this.server = connection;
    }
    public void registerInboundMessageListener(InboundMessageListener listener){
        this.listener = listener;
    }
    public void update(int code,byte[] payload){
        listener.onUpdated(code,payload);
    }
}
