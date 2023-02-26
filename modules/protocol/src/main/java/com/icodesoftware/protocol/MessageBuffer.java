package com.icodesoftware.protocol;

import com.icodesoftware.Session;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class MessageBuffer {

    public final static int PAYLOAD_SIZE = 485;//SIZE - HEADER_SIZE
    public final static int SIZE = 508;
    public final static int PENDING_ACK_SIZE = 10;
    public final static int RETRIES = 3;
    public final static int HEADER_SIZE = 23;

    private ByteBuffer byteBuffer;

    public MessageBuffer(){
        byteBuffer = ByteBuffer.allocateDirect(SIZE);
    }

    public void reset(byte[] data){
        byteBuffer.clear();
        byteBuffer.put(data);
    }
    public void reset(byte[] data,int offset,int length){
        byteBuffer.clear();
        byteBuffer.put(data,offset,length);
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
        int bits = header.ack?1:0;
        bits = header.broadcasting?bits|2:bits|0;
        bits = header.encrypted?bits|4:bits|0;
        byteBuffer.put((byte)bits);
        byteBuffer.putInt(header.channelId);
        byteBuffer.putInt(header.sessionId);
        byteBuffer.putInt(header.objectId);
        byteBuffer.putInt(header.sequence);
        byteBuffer.putShort(header.commandId);
        byteBuffer.putShort(header.batchSize);
        byteBuffer.putShort(header.batch);
        return this;
    }
    public MessageHeader readHeader(){
        MessageHeader header = new MessageHeader();
        int bits = byteBuffer.get();
        header.ack = (bits&1) == 1;
        header.broadcasting = (bits&2) == 2;
        header.encrypted = (bits&4) == 4;
        header.channelId = byteBuffer.getInt();
        header.sessionId = byteBuffer.getInt();
        header.objectId = byteBuffer.getInt();
        header.sequence = byteBuffer.getInt();
        header.commandId = byteBuffer.getShort();
        header.batchSize = byteBuffer.getShort();
        header.batch = byteBuffer.getShort();
        return header;
    }
    public int readInt(){
        return byteBuffer.getInt();
    }
    public MessageBuffer writeInt(int data){
        byteBuffer.putInt(data);
        return this;
    }
    public long readLong(){
        return byteBuffer.getLong();
    }
    public MessageBuffer writeLong(long data){
        byteBuffer.putLong(data);
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
    public double readDouble(){
        return byteBuffer.getDouble();
    }
    public MessageBuffer writeDouble(double data){
        byteBuffer.putDouble(data);
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
    public int readPayload(byte[] buffer){
        //fill buffer after reading header
        int length = byteBuffer.limit()-HEADER_SIZE;
        byteBuffer.get(buffer,0,length);
        return length;
    }
    public byte[] toArray(){
        byte[] _payload = new byte[byteBuffer.limit()];
        byteBuffer.get(_payload);
        return _payload;
    }
    public int toArray(byte[] buffer){
        int limit = byteBuffer.limit();
        byteBuffer.get(buffer,0,limit);
        return limit;
    }

    public static class MessageHeader implements Session.Header {
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
            return "H_"+channelId+"_"+sessionId+"_"+objectId+"_"+sequence;
        }
        @Override
        public boolean equals(Object obj){
            if(!(obj instanceof MessageHeader)) return false;
            MessageHeader messageHeader = (MessageHeader)obj;
            return messageHeader.channelId == channelId && messageHeader.sessionId == sessionId && messageHeader.objectId == objectId && messageHeader.sequence == sequence;
        }
        @Override
        public int hashCode(){
            return Arrays.hashCode(new int[]{channelId,sequence,objectId,sequence});
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

        @Override
        public boolean ack() {
            return ack;
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
        public int objectId() {
            return objectId;
        }

        @Override
        public int sequence() {
            return sequence;
        }

        @Override
        public short commandId() {
            return commandId;
        }

        @Override
        public short batchSize() {
            return batchSize;
        }

        @Override
        public short batch() {
            return batch;
        }

        @Override
        public boolean broadcasting() {
            return broadcasting;
        }

        @Override
        public boolean encrypted() {
            return encrypted;
        }
    }

}
