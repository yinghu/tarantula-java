package com.tarantula.cci.udp;

import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Channel;
import com.icodesoftware.Connection;
import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;

public class GameChannel extends RecoverableObject implements Channel, Portable {

    protected int channelId;
    protected int sessionId;
    protected byte[] serverKey;
    protected Connection connection;

    public GameChannel(){

    }
    public GameChannel(int channelId,int sessionId){
        this.channelId = channelId;
        this.sessionId = sessionId;
    }
    public GameChannel(int channelId,int sessionId,Connection connection,byte[] serverKey){
        this.channelId = channelId;
        this.sessionId = sessionId;
        this.connection = connection;
        this.serverKey = serverKey;
    }

    @Override
    public int channelId() {
        return channelId;
    }

    @Override
    public int sessionId() {
        return sessionId;
    }
    public byte[] toBinary(){
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.putInt(channelId).putInt(sessionId).flip();
        return byteBuffer.array();
    }
    public void fromBinary(byte[] payload){
        ByteBuffer byteBuffer = ByteBuffer.wrap(payload);
        this.channelId = byteBuffer.getInt();
        this.sessionId = byteBuffer.getInt();
    }
    @Override
    public void write(MessageBuffer.MessageHeader messageHeader, byte[] bytes) {

    }

    @Override
    public Connection connection() {
        return connection;
    }
    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("channelId",channelId);
        jsonObject.addProperty("sessionId",sessionId);
        jsonObject.addProperty("serverKey", Base64.getEncoder().encodeToString(serverKey));
        jsonObject.add("connection",connection.toJson());
        return jsonObject;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.GAME_CHANNEL_CID;
    }
    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeInt("1",channelId);
        portableWriter.writeInt("2",sessionId);
        portableWriter.writeByteArray("3",serverKey);
        portableWriter.writePortable("4",(Portable) connection);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        channelId = portableReader.readInt("1");
        sessionId = portableReader.readInt("2");
        serverKey = portableReader.readByteArray("3");
        connection = portableReader.readPortable("4");
    }
}
