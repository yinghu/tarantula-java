package com.tarantula.cci.udp;

import com.google.gson.JsonObject;
import com.icodesoftware.Channel;
import com.icodesoftware.Connection;
import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.util.RecoverableObject;

import java.nio.ByteBuffer;
import java.util.Base64;

public class GameChannel extends RecoverableObject implements Channel {

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
}
