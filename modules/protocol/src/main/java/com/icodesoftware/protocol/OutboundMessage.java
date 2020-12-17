package com.icodesoftware.protocol;

import java.nio.ByteBuffer;

/**
 * Created by yinghu lu on 9/26/2020.
 */
public class OutboundMessage {
    public static int MESSAGE_SIZE = 512;
    private ByteBuffer message;
    private int payloadSize;
    public OutboundMessage(){
        message = ByteBuffer.allocate(MESSAGE_SIZE);
    }
    public void ack(boolean ack){
        message.put(InboundMessage.ACK_POS,ack?(byte)1:(byte)0);
    }
    public void type(int type){
        message.putInt(InboundMessage.TYPE_POS,type);
    }
    public void messageId(int messageId){
        message.putInt(InboundMessage.MESSAGE_ID_POS,messageId);
    }
    public void connectionId(int connectionId){
        message.putInt(InboundMessage.CONNECTION_ID_POS,connectionId);
    }
    public void sessionId(int sessionId){
        message.putInt(InboundMessage.SESSION_ID_POD,sessionId);
    }
    public void sequence(int sequence){
        message.putInt(InboundMessage.SEQUENCE_POS,sequence);
    }
    public void timestamp(long timestamp){
        message.putLong(InboundMessage.TIMESTAMP_POS,timestamp);
    }
    public void payload(byte[] payload){
        message.position(InboundMessage.PAYLOAD_POS);
        message.put(payload);
        payloadSize = payload.length;
    }
    public byte[] message(){
        byte[] payload = new byte[InboundMessage.PAYLOAD_POS+payloadSize];
        message.position(InboundMessage.ACK_POS);
        message.get(payload);
        return payload;
    }
}
