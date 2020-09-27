package com.tarantula.cci;

import java.nio.ByteBuffer;

/**
 * Created by yinghu lu on 9/26/2020.
 */
public class PendingOutboundMessage {
    public static int MESSAGE_SIZE = 512;
    private ByteBuffer message;
    public PendingOutboundMessage(){
        message = ByteBuffer.allocate(MESSAGE_SIZE);
    }
    public void ack(boolean ack){
        message.put(PendingInboundMessage.ACK_POS,ack?(byte)1:(byte)0);
    }
    public void type(int type){
        message.putInt(PendingInboundMessage.TYPE_POS,type);
    }
    public void messageId(int messageId){
        message.putInt(PendingInboundMessage.MESSAGE_ID_POS,messageId);
    }
    public void sequence(byte[] sequence){
        message.position(PendingInboundMessage.SEQ_POS);
        message.put(sequence);
    }
    public void payload(byte[] payload){
        message.position(PendingInboundMessage.PAYLOAD_POS);
        message.put(payload);
    }
    public ByteBuffer message(){
        message.flip();
        return this.message;
    }
}
