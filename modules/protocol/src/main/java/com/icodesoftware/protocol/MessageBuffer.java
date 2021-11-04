package com.icodesoftware.protocol;

import java.nio.ByteBuffer;

public class MessageBuffer {
    public final static int PAYLOAD_SIZE = 483;
    private final static int SIZE = 508;
    private final static int HEADER_SIZE = 25;

    private ByteBuffer byteBuffer;

    public MessageBuffer(){
        byteBuffer = ByteBuffer.allocateDirect(SIZE);
    }

    public void reset(byte[] data){
        byteBuffer.clear();
        byteBuffer.put(data);
    }
    public void reset(){
        byteBuffer.clear();
    }
    public void flip(){
        byteBuffer.flip();
    }
    public void rewind(){
        byteBuffer.rewind();
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
    public int readInt(){
        return byteBuffer.getInt();
    }
    public MessageBuffer writeInt(int data){
        byteBuffer.putInt(data);
        return this;
    }
    public short readShort(){
        return byteBuffer.getShort();
    }
    public MessageBuffer writeShort(short data){
        byteBuffer.putShort(data);
        return this;
    }
    public byte readByte(){
        return byteBuffer.get();
    }
    public MessageBuffer writeByte(byte data){
        byteBuffer.put(data);
        return this;
    }
    public float readFloat(){
        return byteBuffer.getFloat();
    }
    public MessageBuffer writeFloat(float data){
        byteBuffer.putFloat(data);
        return this;
    }
    public String readUTF8(){
        int len = byteBuffer.getInt();
        byte[] ret = new byte[len];
        byteBuffer.get(ret);
        return new String(ret);
    }
    public MessageBuffer writeUTF8(String data){
        byteBuffer.putInt(data.length());
        byteBuffer.put(data.getBytes());
        return this;
    }
    public MessageBuffer writePayload(byte[] payload){
        byteBuffer.put(payload);
        return this;
    }
    public MessageBuffer writePayload(byte[] payload,int offset,int length){
        byteBuffer.put(payload,offset,length);
        return this;
    }
    public byte[] readPayload(){
        byte[] _payload = new byte[byteBuffer.limit()-HEADER_SIZE];
        byteBuffer.get(_payload);
        return _payload;
    }
    public byte[] toArray(){
        byte[] _payload = new byte[byteBuffer.limit()];
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
