package com.tarantula.cci.udp;

import com.icodesoftware.protocol.ChannelListener;
import com.icodesoftware.Connection;
import com.icodesoftware.Session;
import com.icodesoftware.protocol.*;
import com.icodesoftware.util.BatchUtil;
import com.icodesoftware.util.CipherUtil;

public class UDPChannel extends GameChannel {

    private UDPEndpointServiceProvider.RequestListener requestListener;
    private UDPEndpointServiceProvider.ActionListener actionListener;
    private Session.TimeoutListener timeoutListener;
    private ChannelListener channelListener;

    public UDPChannel(Connection connection, UserChannel userChannel, byte[] serverKey, int timeout, UDPEndpointServiceProvider.CipherListener cipherListener){
        this.connection = connection;
        this.userChannel = userChannel;
        this.channelId = userChannel.channelId();
        this.serverKey = serverKey;
        this.timeout = timeout;
        this.messageBuffer = new MessageBuffer();
        this.cipherListener = cipherListener;
    }


    public void register(Session session,ChannelListener channelListener,UDPEndpointServiceProvider.RequestListener requestListener,UDPEndpointServiceProvider.ActionListener actionListener, Session.TimeoutListener timeoutListener){
        this.stub = session;
        this.channelListener = channelListener;
        this.requestListener = requestListener;
        this.actionListener = actionListener;
        this.timeoutListener = timeoutListener;
    }

    //server push call
    @Override
    public void write(Session.Header header,byte[] payload) {
        super.write(header,payload);
    }

    //udp request call
    public void onRequest(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer){
        byte[] ret = this.requestListener.onRequest(stub,messageHeader,messageBuffer);
        if(ret==null) return;
        boolean encrypted = messageHeader.encrypted;
        int batchSize = encrypted? MessageBuffer.PAYLOAD_SIZE- CipherUtil.cipherSize(ret.length) : MessageBuffer.PAYLOAD_SIZE;
        BatchUtil.Batch batch = BatchUtil.batch(ret.length,batchSize);
        for(BatchUtil.Offset offset : batch.offsets){
            messageBuffer.reset();
            messageHeader.ack = false;
            messageHeader.commandId = Messenger.ON_REQUEST;
            messageHeader.encrypted = encrypted;
            messageHeader.batch = offset.batch;
            messageHeader.batchSize = batch.size;
            messageBuffer.writeHeader(messageHeader);
            messageBuffer.writePayload(ret,offset.offset,offset.length);
            messageBuffer.flip();
            if(encrypted){
                messageBuffer.readHeader();
                if(!cipherListener.encrypt(messageHeader,messageBuffer)) break;
                messageBuffer.rewind();
            }
            userChannel.queue(messageHeader.sessionId,messageBuffer);
        }
    }

    //udp RPC call
    public void onAction(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer, UDPEndpointServiceProvider.RelayListener callback){
        actionListener.onAction(messageHeader,messageBuffer,callback);
    }

    @Override
    public void close(){
        super.close();
    }

    public void kickoff(){
        this.timeoutListener.timeout(this.stub.systemId(), this.stub.distributionId());
        this.channelListener.onLeft(this);
    }
    public void joined(){
        this.channelListener.onJoined(this);
    }
    public void validated(){
        this.channelListener.onValidated(this);
    }

}
