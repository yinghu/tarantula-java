package com.tarantula.platform;

import com.icodesoftware.Connection;

import com.icodesoftware.protocol.DataBuffer;

/**
 * Created by yinghu lu on 12/17/2020.
 */
public class ClientConnection extends ResponseHeader implements Connection {

    protected String type;
    protected String serverId;
    protected int connectionId;
    protected int sessionId;
    protected int sequence;
    protected boolean secured;
    protected String protocol;
    protected String host;
    protected int port;
    protected String path;
    protected int messageId;
    protected int messageIdOffset;
    protected int maxConnections;
    protected Connection server;

    @Override
    public String type() {
        return type;
    }

    @Override
    public void type(String type) {
        this.type = type;
    }

    @Override
    public String serverId() {
        return serverId;
    }

    @Override
    public void serverId(String serverId) {
        this.serverId = serverId;
    }

    @Override
    public int connectionId() {
        return connectionId;
    }

    @Override
    public void connectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    @Override
    public int sessionId() {
        return this.sessionId;
    }

    @Override
    public void sessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public int sequence() {
        return sequence;
    }

    @Override
    public void sequence(int sequence) {
        this.sequence = sequence;
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
        return this.protocol;
    }

    @Override
    public void protocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public String subProtocol() {
        return "tarantula-service";
    }

    @Override
    public void subProtocol(String s) {

    }

    @Override
    public String host() {
        return host;
    }

    @Override
    public void host(String host) {
        this.host = host;
    }

    @Override
    public int port() {
        return port;
    }

    @Override
    public void port(int port) {
        this.port = port;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public void path(String path) {
        this.path = path;
    }

    @Override
    public int messageId() {
        return messageId;
    }

    @Override
    public int messageIdOffset() {
        return 0;
    }

    @Override
    public void messageId(int messageId) {
        this.messageId = messageId;
    }

    @Override
    public void messageIdOffset(int messageIdOffset) {
        this.messageIdOffset = messageIdOffset;
    }

    @Override
    public int maxConnections() {
        return maxConnections;
    }

    @Override
    public void maxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    @Override
    public Connection server() {
        return server;
    }

    @Override
    public void server(Connection connection) {
        this.server = connection;
    }
    @Override
    public byte[] toBinary(){
        DataBuffer dataBuffer = new DataBuffer();
        dataBuffer.putUTF8(type);
        dataBuffer.putInt(connectionId);
        dataBuffer.putUTF8(serverId);
        dataBuffer.putUTF8(host);
        dataBuffer.putInt(port);
        dataBuffer.putByte(secured?(byte)1:0);
        return dataBuffer.toArray();
    }
    @Override
    public void fromBinary(byte[] payload){
        DataBuffer dataBuffer = new DataBuffer(payload);
        this.type = dataBuffer.getUTF8();
        this.connectionId = dataBuffer.getInt();
        this.serverId = dataBuffer.getUTF8();
        this.host = dataBuffer.getUTF8();
        this.port = dataBuffer.getInt();
        this.secured = dataBuffer.getByte()==1;
    }
}
