package com.tarantula.platform;

import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Connection;

import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;


public class ClientConnection extends ResponseHeader implements Connection, Portable {

    protected String configurationTypeId;
    protected String type;
    protected String serverId;

    protected boolean secured;
    protected String host;
    protected int port;

    public String configurationTypeId() {
        return this.configurationTypeId;
    }

    public void configurationTypeId(String configurationTypeId) {
        this.configurationTypeId = configurationTypeId;
    }

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



    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    public int getClassId() {
        return PortableEventRegistry.CLIENT_CONNECTION_CID;
    }
    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeUTF("1",configurationTypeId);
        portableWriter.writeUTF("2",type);
        portableWriter.writeUTF("3",serverId);
        portableWriter.writeUTF("4",host);
        portableWriter.writeInt("5",port);
        portableWriter.writeBoolean("6",secured);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        this.configurationTypeId = portableReader.readUTF("1");
        this.type = portableReader.readUTF("2");
        this.serverId = portableReader.readUTF("3");
        this.host = portableReader.readUTF("4");
        this.port = portableReader.readInt("5");
        this.secured = portableReader.readBoolean("6");
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Type",type);
        jsonObject.addProperty("ServerId",serverId);
        jsonObject.addProperty("Secured",secured);
        jsonObject.addProperty("Host",host);
        jsonObject.addProperty("Port",port);
        return jsonObject;
    }
}
