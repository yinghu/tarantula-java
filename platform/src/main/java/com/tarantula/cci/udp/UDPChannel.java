package com.tarantula.cci.udp;

import com.google.gson.JsonObject;
import com.icodesoftware.Channel;
import com.icodesoftware.Connection;
import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.protocol.Messenger;
import com.icodesoftware.protocol.UDPEndpointServiceProvider;
import com.icodesoftware.protocol.UserChannel;
import com.icodesoftware.util.BatchUtil;
import com.icodesoftware.util.CipherUtil;
import com.icodesoftware.util.RecoverableObject;

import java.util.Base64;

public class UDPChannel extends RecoverableObject implements Channel {

    private Connection connection;
    private UserChannel userChannel;
    private int channelId;
    private int sessionId;
    private byte[] serverKey;
    private UDPEndpointServiceProvider.RequestListener requestListener;
    public UDPChannel(Connection connection, UserChannel userChannel, int sessionId, byte[] serverKey, UDPEndpointServiceProvider.RequestListener requestListener){
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
            try{
                //byte[] payload = CipherUtil.encrypt(serverKey).doFinal(ret);
                if(ret.length <= MessageBuffer.PAYLOAD_SIZE){
                    messageBuffer.reset();
                    messageHeader.commandId = Messenger.ON_REQUEST;
                    messageHeader.encrypted = false;
                    messageBuffer.writeHeader(messageHeader);
                    messageBuffer.writePayload(ret);
                    messageBuffer.flip();
                    userChannel.write(messageHeader.sessionId,messageBuffer.toArray());
                }
                else{
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
                        userChannel.write(messageHeader.sessionId,messageBuffer.toArray());
                    }
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
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
        jsonObject.addProperty("serverKey", Base64.getEncoder().encodeToString(serverKey));
        jsonObject.add("connection",connection.toJson());
        return jsonObject;
    }
}
