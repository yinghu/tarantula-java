package com.tarantula.platform.service.persistence;

import com.icodesoftware.util.RecoverableObject;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class RevisionObject extends RecoverableObject {

    public byte[] data;

    public RevisionObject(){
    }
    public RevisionObject(byte[] payload,long revision){
        this.data = payload;
        this.revision = revision;
    }
    public byte[] toBinary(){
        ByteBuffer buffer = ByteBuffer.allocate(data.length+8);
        buffer.putLong(revision).put(data);
        return buffer.array();
    }
    public void fromBinary(byte[] payload){
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(payload,0,8);
        buffer.flip();
        revision = buffer.getLong();
        data = Arrays.copyOfRange(payload,8,payload.length);
    }

}
