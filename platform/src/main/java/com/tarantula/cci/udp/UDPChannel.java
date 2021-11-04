package com.tarantula.cci.udp;

import com.google.gson.JsonObject;
import com.icodesoftware.Channel;
import com.icodesoftware.Connection;
import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.protocol.Messenger;
import com.icodesoftware.protocol.UDPEndpointServiceProvider;
import com.icodesoftware.protocol.UserChannel;
import com.icodesoftware.util.RecoverableObject;

public class UDPChannel extends RecoverableObject implements Channel {

    private Connection connection;
    private UserChannel userChannel;
    private int channelId;
    private int sessionId;
    private String serverKey;
    private UDPEndpointServiceProvider.RequestListener requestListener;
    public UDPChannel(Connection connection, UserChannel userChannel, int sessionId, String serverKey, UDPEndpointServiceProvider.RequestListener requestListener){
        this.connection = connection;
        this.userChannel = userChannel;
        this.channelId = userChannel.channelId;
        this.sessionId = sessionId;
        this.serverKey = serverKey;
        this.requestListener = requestListener;
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
    public void write(MessageBuffer.MessageHeader messageHeader,byte[] bytes) {
        //userChannel.write(sessionId,bytes);
    }
    public void onMessage(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer){
        byte[] ret = this.requestListener.onMessage(messageHeader,messageBuffer);
        if(ret!=null){
            messageBuffer.reset();
            messageHeader.commandId = Messenger.ON_REQUEST;
            messageBuffer.writeHeader(messageHeader);
            messageBuffer.writePayload(ret);
            messageBuffer.flip();
            userChannel.write(messageHeader.sessionId,messageBuffer.toArray());
        }
    }
    public Connection connection(){
        return this.connection;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("channelId",channelId);
        jsonObject.addProperty("sessionId",sessionId);
        jsonObject.addProperty("serverKey",serverKey);
        jsonObject.add("connection",connection.toJson());
        return jsonObject;
    }
}
