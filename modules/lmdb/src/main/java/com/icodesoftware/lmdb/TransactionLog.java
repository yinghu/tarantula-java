package com.icodesoftware.lmdb;

import com.icodesoftware.util.BufferProxy;
import com.icodesoftware.util.IOStreamDataBuffer;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.SnowflakeKey;

import java.io.ByteArrayOutputStream;

public class TransactionLog extends RecoverableObject {

    public static final String LABEL = "transaction_log";
    public boolean deleting;

    public long updatingRevision;

    public int scope;

    public String source;
    public String edgeLabel;

    public byte[] key;

    public byte[] value;
    public byte[] edgeKey;

    public TransactionLog(){
        this.label = LABEL;
        this.onEdge = true;
    }
    public TransactionLog(boolean deleting, int scope, String source, String edgeLabel, byte[] key, byte[] edgeKey, long updatingRevision){
        this();
        this.deleting = deleting;
        this.scope = scope;
        this.source = source;
        this.edgeLabel = edgeLabel;
        this.key = key;
        if(edgeKey != null) this.edgeKey = edgeKey;
        this.updatingRevision = updatingRevision;
    }
    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeBoolean(deleting);
        buffer.writeLong(updatingRevision);
        buffer.writeInt(scope);
        buffer.writeUTF8(source);
        buffer.writeUTF8(edgeLabel);
        buffer.writeInt(key.length);
        buffer.write(key);
        if(edgeLabel==null) return true;
        if(edgeKey==null){
            buffer.writeInt(0);
            return true;
        }
        buffer.writeInt(edgeKey.length);
        buffer.write(edgeKey);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        deleting = buffer.readBoolean();
        updatingRevision = buffer.readLong();
        scope = buffer.readInt();
        source = buffer.readUTF8();
        edgeLabel = buffer.readUTF8();
        key = new byte[buffer.readInt()];
        buffer.read(key);
        if(edgeLabel==null) return true;
        int esize = buffer.readInt();
        if(esize==0) return true;
        edgeKey = new byte[esize];
        buffer.read(edgeKey);
        return true;
    }

    @Override
    public void fromBinary(byte[] payload) {
        DataBuffer buffer = BufferProxy.wrap(payload);
        deleting = buffer.readBoolean();
        updatingRevision = buffer.readLong();
        scope = buffer.readInt();
        source = buffer.readUTF8();
        edgeLabel = buffer.readUTF8();
        key = new byte[buffer.readInt()];
        buffer.read(key);
        int esize = buffer.readInt();
        if(esize>0){
            edgeKey = new byte[esize];
            buffer.read(edgeKey);
        }
        int vsize = buffer.readInt();
        if(vsize>0){
            value =  new byte[vsize];
            buffer.read(value);
        }
    }

    @Override
    public byte[] toBinary() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(2000);
        DataBuffer buffer = IOStreamDataBuffer.writer(outputStream);
        buffer.writeBoolean(deleting);
        buffer.writeLong(updatingRevision);
        buffer.writeInt(scope);
        buffer.writeUTF8(source);
        buffer.writeUTF8(edgeLabel);
        buffer.writeInt(key.length);
        buffer.write(key);

        buffer.writeInt(edgeLabel!=null?edgeKey.length:0);
        if(edgeLabel!=null){
            buffer.write(edgeKey);
        }
        buffer.writeInt(value!=null?value.length:0);
        if(value!=null){
            buffer.write(value);
        }
        buffer.flip();
        return outputStream.toByteArray();
    }

    @Override
    public int getFactoryId() {
        return PersistencePortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PersistencePortableRegistry.TRANSACTION_LOG_CID;
    }

    public static TransactionLog log(long transactionId, boolean deleting, int scope, String source, String edgeLabel, byte[] key, byte[] edgeKey, long updatingRevision){
        TransactionLog transactionLog = new TransactionLog(deleting,scope,source,edgeLabel,key,edgeKey,updatingRevision);
        transactionLog.ownerKey(new SnowflakeKey(transactionId));
        return transactionLog;
    }

}
