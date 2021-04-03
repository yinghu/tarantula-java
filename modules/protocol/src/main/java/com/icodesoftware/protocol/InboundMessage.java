package com.icodesoftware.protocol;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class InboundMessage {

    public static int ACK_POS = 0;
    public static int TYPE_POS = 1;
    public static int MESSAGE_ID_POS = 5;
    public static int CONNECTION_ID_POS = 9;
    public static int SEQUENCE_POS = 13;
    public static int SESSION_ID_POD = 17;
    public static int PAYLOAD_POS = 21;

    public final String serverId;
    private final ByteBuffer message;
    private final SocketAddress source;

    public InboundMessage(String serverId, ByteBuffer message, SocketAddress source){
        this.serverId = serverId;
        this.message = message;
        this.source = source;
    }
    public SocketAddress source(){
        return source;
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
    public int connectionId(){
        return message.getInt(CONNECTION_ID_POS);
    }
    public int sessionId(){
        return message.getInt(SESSION_ID_POD);
    }
    public int sequence(){
        return message.getInt(SEQUENCE_POS);
    }
    public byte[] payload(){
        byte[] payload = new byte[message.limit()-PAYLOAD_POS];
        message.position(PAYLOAD_POS);
        message.get(payload);
        return payload;
    }
}
