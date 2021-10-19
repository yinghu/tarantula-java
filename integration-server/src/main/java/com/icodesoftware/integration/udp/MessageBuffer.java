package com.icodesoftware.integration.udp;

import java.nio.ByteBuffer;

public class MessageBuffer {
    private static int SIZE = 508;

    private ByteBuffer byteBuffer;

    public MessageBuffer(){
        byteBuffer = ByteBuffer.allocateDirect(SIZE);
    }

    public void reset(byte[] data){
        byteBuffer.clear();
        byteBuffer.put(data);
        byteBuffer.flip();
    }
    public MessageHeader readHeader(){
        MessageHeader header = new MessageHeader();
        header.ack = byteBuffer.get()==1;
        header.channelId = byteBuffer.getInt();
        header.sessionId = byteBuffer.getInt();
        header.objectId = byteBuffer.getInt();
        header.sequence = byteBuffer.getInt();
        header.commandId = byteBuffer.getShort();
        header.encrypted = byteBuffer.get()==1;
        return header;
    }

    public class MessageHeader{
        public boolean ack;
        public int channelId;
        public int sessionId;
        public int objectId;
        public int sequence;
        public int commandId;
        public boolean encrypted;
        @Override
        public String toString(){
            return "MSG->"+channelId+"<>"+sessionId+"<>"+ack+"<>"+objectId+"<>"+sequence+"<>"+commandId+"<>"+encrypted;
        }
    }

}
