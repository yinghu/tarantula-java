package com.tarantula.platform.service.persistence;

import com.tarantula.platform.RecoverableObject;

import java.nio.ByteBuffer;

public class ActiveEntry extends RecoverableObject {

    public ActiveEntry(){}
    public ActiveEntry(String source){
        this.vertex = source;
        this.timestamp = System.currentTimeMillis();
    }

    public byte[] toByteArray(){
        ByteBuffer buffer = ByteBuffer.allocate(12+vertex.length());
        buffer.putInt(this.vertex.length());
        buffer.put(this.vertex.getBytes());
        buffer.putLong(this.timestamp);
        return buffer.array();
    }

    public void fromByteArray(byte[] data){
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int len = buffer.getInt();
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<len;i++){
            sb.append((char) buffer.get());
        }
        this.vertex = sb.toString();
        this.timestamp = buffer.getLong();
        //read data from byte array
    }
}
