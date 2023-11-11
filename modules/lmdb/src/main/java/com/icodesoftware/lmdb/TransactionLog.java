package com.icodesoftware.lmdb;

import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.SnowflakeKey;

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
        this.edgeKey = edgeKey;
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
        for(byte b: key){
            buffer.writeByte(b);
        }
        if(edgeLabel==null) return true;
        if(edgeKey==null){
            buffer.writeInt(0);
            return true;
        }
        buffer.writeInt(edgeKey.length);
        for(byte b: edgeKey){
            buffer.writeByte(b);
        }
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
        for(int i=0;i<key.length;i++){
            key[i]=buffer.readByte();
        }
        if(edgeLabel==null) return true;
        int esize = buffer.readInt();
        if(esize==0) return true;
        edgeKey = new byte[esize];
        for(int i=0;i<edgeKey.length;i++){
            edgeKey[i]=buffer.readByte();
        }
        return true;
    }

    @Override
    public int getFactoryId() {
        return PersistencePortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PersistencePortableRegistry.TRANSACTION_LOG_CID;
    }

    public static TransactionLog log(long transactionId,boolean deleting,int scope,String source,String edgeLabel,byte[] key,byte[] edgeKey,long updatingRevision){
        TransactionLog transactionLog = new TransactionLog(deleting,scope,source,edgeLabel,key,edgeKey,updatingRevision);
        transactionLog.ownerKey(new SnowflakeKey(transactionId));
        return transactionLog;
    }

}
