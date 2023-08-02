package com.tarantula.platform.service.persistence;


import java.nio.ByteBuffer;
import java.util.Arrays;

public class RevisionObject {

    public final static int MAX_REPLICATION_NODE_NUMBER = 3;
    private final static int META_SIZE = 18;
    public final static int NODE_DATA_SIZE = 9;

    public final boolean local;
    public final byte[] data;
    public final long revision;
    public final byte[] nodeList;
    public final boolean successful;

    public final static RevisionObject FALSE = new RevisionObject(false);
    public final static RevisionObject TRUE = new RevisionObject(true);

    private RevisionObject(boolean successful){
        this.revision = 0;
        this.data = null;
        this.local = false;
        this.nodeList = null;
        this.successful  = successful;
    }
    private RevisionObject(long revision,byte[] data,boolean local,byte[] node){
        this.revision = revision;
        this.data = data;
        this.local = local;
        this.nodeList = node;
        this.successful = true;
    }

    public static byte[] toBinary(long revision,byte[] data,boolean local,byte[] node){
        ByteBuffer buffer = ByteBuffer.allocate(data.length+META_SIZE);
        buffer.put(local?(byte)1:(byte)0);
        buffer.putLong(revision).put(node).put(data);
        return buffer.array();
    }

    public static RevisionObject fromBinary(byte[] payload){
        ByteBuffer buffer = ByteBuffer.allocate(META_SIZE);
        buffer.put(payload,0,META_SIZE);
        buffer.flip();
        boolean _local = buffer.get()==1;
        long rev = buffer.getLong();
        byte[] nd = new byte[NODE_DATA_SIZE];
        buffer.get(nd);
        return new RevisionObject(rev,Arrays.copyOfRange(payload,META_SIZE,payload.length),_local,nd);
    }
    public static RevisionObject fromUpdate(long revision,byte[] update,byte[] node){
        return new RevisionObject(revision,update,false,node);
    }

}
