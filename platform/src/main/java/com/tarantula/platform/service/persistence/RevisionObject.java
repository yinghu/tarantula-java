package com.tarantula.platform.service.persistence;


import java.nio.ByteBuffer;
import java.util.Arrays;

public class RevisionObject {

    public final boolean local;
    public final byte[] data;
    public final long revision;

    public final boolean successful;

    public final static RevisionObject FALSE = new RevisionObject(false);
    public final static RevisionObject TRUE = new RevisionObject(true);

    private RevisionObject(boolean successful){
        this.revision = 0;
        this.data = null;
        this.local = false;
        this.successful  = successful;
    }
    private RevisionObject(long revision,byte[] data,boolean local){
        this.revision = revision;
        this.data = data;
        this.local = local;
        this.successful = true;
    }

    public static byte[] toBinary(long revision,byte[] data,boolean local){
        ByteBuffer buffer = ByteBuffer.allocate(data.length+9);
        buffer.put(local?(byte)1:(byte)0);
        buffer.putLong(revision).put(data);
        return buffer.array();
    }

    public static RevisionObject fromBinary(byte[] payload){
        ByteBuffer buffer = ByteBuffer.allocate(9);
        buffer.put(payload,0,9);
        buffer.flip();
        boolean _local = buffer.get()==1;
        return new RevisionObject(buffer.getLong(),Arrays.copyOfRange(payload,9,payload.length),_local);
    }
    public static RevisionObject fromUpdate(long revision,byte[] update){
        return new RevisionObject(revision,update,false);
    }

}
