package com.tarantula.platform;

import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Connection;

import com.icodesoftware.protocol.DataBuffer;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.io.IOException;


public class ClientConnection extends ResponseHeader implements Connection, Portable {

    protected String type;
    protected String serverId;

    protected boolean secured;
    protected String protocol;
    protected String subProtocol;
    protected String host;
    protected int port;
    protected String path;

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
        return this.subProtocol;
    }

    @Override
    public void subProtocol(String subProtocol) {
        this.subProtocol = subProtocol;
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
    public byte[] toBinary(){
        DataBuffer dataBuffer = new DataBuffer();
        dataBuffer.putUTF8(type);
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
        this.serverId = dataBuffer.getUTF8();
        this.host = dataBuffer.getUTF8();
        this.port = dataBuffer.getInt();
        this.secured = dataBuffer.getByte()==1;
    }
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    public int getClassId() {
        return PortableRegistry.CLIENT_CONNECTION_CID;
    }
    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeUTF("1",type);
        portableWriter.writeUTF("3",serverId);
        portableWriter.writeUTF("4",host);
        portableWriter.writeInt("5",port);
        portableWriter.writeBoolean("6",secured);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        this.type = portableReader.readUTF("1");
        this.serverId = portableReader.readUTF("3");
        this.host = portableReader.readUTF("4");
        this.port = portableReader.readInt("5");
        this.secured = portableReader.readBoolean("6");
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type",type);
        jsonObject.addProperty("serverId",serverId);
        jsonObject.addProperty("secured",secured);
        jsonObject.addProperty("protocol",protocol);
        jsonObject.addProperty("subProtocol",subProtocol());
        jsonObject.addProperty("host",host);
        jsonObject.addProperty("port",port);
        jsonObject.addProperty("path",path);
        return jsonObject;
    }
}
