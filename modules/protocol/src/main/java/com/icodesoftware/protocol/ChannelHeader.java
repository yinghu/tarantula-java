package com.icodesoftware.protocol;


import com.icodesoftware.Connection;
import com.icodesoftware.Session;

import com.icodesoftware.util.BatchUtil;
import com.icodesoftware.util.CipherUtil;
import com.icodesoftware.util.RecoverableObject;

public class ChannelHeader extends RecoverableObject implements Channel {

    protected String configurationTypeId;

    protected int channelId;
    protected int sessionId;
    protected int timeout;
    protected byte[] serverKey;
    protected Connection connection;

    protected UserChannel userChannel;

    protected UDPEndpointServiceProvider.CipherListener cipherListener;
    protected MessageBuffer messageBuffer;
    protected Session stub;


    public String configurationTypeId() {
        return this.configurationTypeId;
    }

    public void configurationTypeId(String configurationTypeId) {
        this.configurationTypeId = configurationTypeId;
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
    public int timeout() {
        return timeout;
    }

    @Override
    public byte[] serverKey() {
        return serverKey;
    }

    @Override
    public void write(Session.Header header, byte[] payload) {
        if(payload==null||payload.length==0) return;
        MessageBuffer.MessageHeader messageHeader = (MessageBuffer.MessageHeader)header;
        boolean encrypted = messageHeader.encrypted;
        int batchSize = encrypted? MessageBuffer.PAYLOAD_SIZE- CipherUtil.cipherSize(payload.length) : MessageBuffer.PAYLOAD_SIZE;
        BatchUtil.Batch batch = BatchUtil.batch(payload.length,batchSize);
        if(batch.size > MessageBuffer.MAX_BATCH_SIZE){
            return;
        }
        synchronized (messageBuffer){
            for(BatchUtil.Offset offset : batch.offsets){
                messageBuffer.reset();
                messageHeader.ack = false;
                messageHeader.commandId = Messenger.ON_PUSH;
                messageHeader.channelId = channelId;
                messageHeader.sessionId = sessionId;
                messageHeader.encrypted = encrypted;
                messageHeader.batch = offset.batch;
                messageHeader.batchSize = batch.size;
                messageBuffer.writeHeader(messageHeader);
                messageBuffer.writePayload(payload,offset.offset,offset.length);
                messageBuffer.flip();
                if(encrypted){
                    messageBuffer.readHeader();
                    if(!cipherListener.encrypt(messageHeader,messageBuffer)) break;
                    messageBuffer.rewind();
                }
                userChannel.queue(messageHeader.sessionId,messageBuffer);
            }
        }
    }

    @Override
    public Connection connection() {
        return connection;
    }

    @Override
    public void close(){
        userChannel.kickoff(sessionId);
    }


}
