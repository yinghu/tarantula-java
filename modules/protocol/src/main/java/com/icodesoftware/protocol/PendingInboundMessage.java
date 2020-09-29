package com.icodesoftware.protocol;

import java.nio.ByteBuffer;

/**
 * Created by yinghu lu on 9/24/2020.
 */
public class PendingInboundMessage {

    public static int ACK_POS = 0;
    public static int TYPE_POS = 1;
    public static int MESSAGE_ID_POS = 5;
    public static int CONNECTION_ID_POS = 9;
    public static int SEQ_POS = 17;
    public static int PAYLOAD_POS = 65;

    public final String serverId;
    private final ByteBuffer message;

    public PendingInboundMessage(String serverId, ByteBuffer message){
        this.serverId = serverId;
        this.message = message;
    }
    public boolean ack(){
        return message.get(ACK_POS)==1;
    }
    public int messageId(){
        return message.getInt(MESSAGE_ID_POS);
    }
    public int type(){
        return message.getInt(TYPE_POS);
    }
    public long connectionId(){
        return message.getLong(CONNECTION_ID_POS);
    }
    public byte[] sequence(){
        byte[] seq = new byte[48];
        message.position(SEQ_POS);
        message.get(seq);
        return seq;
    }
    public byte[] payload(){
        byte[] payload = new byte[message.limit()-PAYLOAD_POS];
        message.position(PAYLOAD_POS);
        message.get(payload);
        return payload;
    }
}
