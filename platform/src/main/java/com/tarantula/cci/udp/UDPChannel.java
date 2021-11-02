package com.tarantula.cci.udp;

import com.google.gson.JsonObject;
import com.icodesoftware.Channel;
import com.icodesoftware.Connection;
import com.icodesoftware.protocol.UserChannel;
import com.icodesoftware.util.RecoverableObject;

public class UDPChannel extends RecoverableObject implements Channel {

    private Connection connection;
    private UserChannel userChannel;
    private int channelId;
    private int sessionId;
    public UDPChannel(Connection connection,UserChannel userChannel,int sessionId){
        this.connection = connection;
        this.userChannel = userChannel;
        this.channelId = userChannel.channelId;
        this.sessionId = sessionId;
    }
    @Override
    public int channelId() {
        return channelId;
    }

    @Override
    public int sessionId() {
        return sessionId;
    }

    @Override
    public void write(byte[] bytes) {
        userChannel.write(sessionId,bytes);
    }

    public Connection connection(){
        return this.connection;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("channelId",channelId);
        jsonObject.addProperty("sessionId",sessionId);
        jsonObject.add("connection",connection.toJson());
        return jsonObject;
    }
}
