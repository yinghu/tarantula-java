package com.tarantula.cci.udp;

import com.google.gson.JsonObject;
import com.icodesoftware.Connection;
import com.icodesoftware.Session;
import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.protocol.Messenger;
import com.icodesoftware.protocol.UDPEndpointServiceProvider;
import com.icodesoftware.protocol.UserChannel;
import com.icodesoftware.util.BatchUtil;

import java.util.Base64;

public class UDPChannel extends GameChannel {

    private UserChannel userChannel;

    private UDPEndpointServiceProvider.RequestListener requestListener;
    private Session.TimeoutListener timeoutListener;
    private MessageBuffer messageBuffer;
    public UDPChannel(Connection connection, UserChannel userChannel,byte[] serverKey,int timeout){
        this.connection = connection;
        this.userChannel = userChannel;
        this.channelId = userChannel.channelId();
        this.serverKey = serverKey;
        this.timeout = timeout;
        messageBuffer = new MessageBuffer();
    }
    public void register(Session session,int sessionId, UDPEndpointServiceProvider.RequestListener requestListener,Session.TimeoutListener timeoutListener){
        this.owner = session.systemId();
        this.routingNumber = session.stub();
        this.sessionId = sessionId;
        this.requestListener = requestListener;
        this.timeoutListener = timeoutListener;
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
    public void write(MessageBuffer.MessageHeader messageHeader,byte[] payload) {
        if(payload==null||payload.length==0) return;
        BatchUtil.Batch batch = BatchUtil.batch(payload.length,MessageBuffer.PAYLOAD_SIZE);
        synchronized (messageBuffer){
            for(BatchUtil.Offset offset : batch.offsets){
                messageBuffer.reset();
                messageHeader.commandId = Messenger.ON_REQUEST;
                messageHeader.channelId = channelId;
                messageHeader.sessionId = sessionId;
                messageHeader.encrypted = false;
                messageHeader.batch = offset.batch;
                messageHeader.batchSize = batch.size;
                messageBuffer.writeHeader(messageHeader);
                messageBuffer.writePayload(payload,offset.offset,offset.length);
                messageBuffer.flip();
                userChannel.queue(messageHeader.sessionId,messageBuffer.toArray());
            }
        }
    }
    public void onMessage(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer){
        byte[] ret = this.requestListener.onMessage(messageHeader,messageBuffer);
        if(ret==null) return;
        BatchUtil.Batch batch = BatchUtil.batch(ret.length,MessageBuffer.PAYLOAD_SIZE);
        for(BatchUtil.Offset offset : batch.offsets){
            messageBuffer.reset();
            messageHeader.commandId = Messenger.ON_REQUEST;
            messageHeader.encrypted = false;
            messageHeader.batch = offset.batch;
            messageHeader.batchSize = batch.size;
            messageBuffer.writeHeader(messageHeader);
            messageBuffer.writePayload(ret,offset.offset,offset.length);
            messageBuffer.flip();
            userChannel.queue(messageHeader.sessionId,messageBuffer.toArray());
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
        jsonObject.addProperty("timeout",timeout);
        jsonObject.addProperty("serverKey", Base64.getEncoder().encodeToString(serverKey));
        jsonObject.add("connection",connection.toJson());
        return jsonObject;
    }
    public void sessionId(int sessionId){
        this.sessionId = sessionId;
    }

    public void close(){
        userChannel.kickoff(sessionId);
    }
    public void kickoff(){
        this.timeoutListener.timeout(this.owner,this.routingNumber);
    }
}
