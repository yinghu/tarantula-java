package com.icodesoftware.protocol;

import java.nio.ByteBuffer;

public class MessageBuffer {
    private static int SIZE = 508;
    private static int HEADER_SIZE = 25;

    private ByteBuffer byteBuffer;

    public MessageBuffer(){
        byteBuffer = ByteBuffer.allocateDirect(SIZE);
    }

    public void reset(byte[] data){
        byteBuffer.clear();
        byteBuffer.put(data);
        byteBuffer.flip();
    }
    public void reset(){
        byteBuffer.clear();
    }
    public MessageBuffer writeHeader(MessageHeader header){
        byteBuffer.put(header.ack?(byte) 1:0);
        byteBuffer.putInt(header.channelId);
        byteBuffer.putInt(header.sessionId);
        byteBuffer.putInt(header.objectId);
        byteBuffer.putInt(header.sequence);
        byteBuffer.putShort(header.commandId);
        byteBuffer.putShort(header.batchSize);
        byteBuffer.putShort(header.batch);
        byteBuffer.put(header.broadcasting?(byte) 1:0);
        byteBuffer.put(header.encrypted?(byte) 1:0);
        return this;
    }
    public MessageHeader readHeader(){
        MessageHeader header = new MessageHeader();
        header.ack = byteBuffer.get()==1;
        header.channelId = byteBuffer.getInt();
        header.sessionId = byteBuffer.getInt();
        header.objectId = byteBuffer.getInt();
        header.sequence = byteBuffer.getInt();
        header.commandId = byteBuffer.getShort();
        header.batchSize = byteBuffer.getShort();
        header.batch = byteBuffer.getShort();
        header.broadcasting = byteBuffer.get()==1;
        header.encrypted = byteBuffer.get()==1;
        return header;
    }
    public byte[] readPayload(){
        byte[] _payload = new byte[byteBuffer.limit()-HEADER_SIZE];
        byteBuffer.get(_payload);
        return _payload;
    }
    public byte[] toArray(){
        byteBuffer.flip();
        byte[] _payload = new byte[byteBuffer.limit()];
        byteBuffer.position(0);
        byteBuffer.get(_payload);
        return _payload;
    }

    public static class MessageHeader{
        public boolean ack;
        public int channelId;
        public int sessionId;
        public int objectId;
        public int sequence;
        public short commandId;
        public short batchSize;
        public short batch;
        public boolean broadcasting;
        public boolean encrypted;
        @Override
        public String toString(){
            return "MSH->"+channelId+"<>"+sessionId+"<>"+objectId+"<>"+sequence;
        }
        public MessageHeader copy(){
            MessageHeader copy = new MessageHeader();
            copy.ack = ack;
            copy.channelId = channelId;
            copy.sessionId = sessionId;
            copy.objectId = objectId;
            copy.sequence = sequence;
            copy.commandId = commandId;
            copy.batchSize = batchSize;
            copy.batch = batch;
            copy.broadcasting = broadcasting;
            copy.encrypted = encrypted;
            return copy;
        }
    }

}
