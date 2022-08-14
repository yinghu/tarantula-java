package com.tarantula.platform.service.persistence;


import java.nio.ByteBuffer;
import java.util.Arrays;

public class RevisionObject {

    public final byte[] data;
    public final long revision;

    private RevisionObject(long revision,byte[] data){
        this.revision = revision;
        this.data = data;
    }

    public static byte[] toBinary(long revision,byte[] data){
        ByteBuffer buffer = ByteBuffer.allocate(data.length+8);
        buffer.putLong(revision).put(data);
        return buffer.array();
    }
    public static RevisionObject fromBinary(byte[] payload){
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(payload,0,8);
        buffer.flip();
        return new RevisionObject(buffer.getLong(),Arrays.copyOfRange(payload,8,payload.length));
    }

}
