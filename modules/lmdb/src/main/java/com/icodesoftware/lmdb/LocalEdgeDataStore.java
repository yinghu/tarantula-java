package com.icodesoftware.lmdb;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.Metadata;
import org.lmdbjava.*;

import java.nio.ByteBuffer;

public class LocalEdgeDataStore {

    public final Metadata metadata;
    public final Dbi<ByteBuffer> dbi;

    public LocalEdgeDataStore(final Metadata metadata,final Dbi<ByteBuffer> dbi){
        this.metadata = metadata;
        this.dbi = dbi;
    }

    public boolean addEdge(Txn<ByteBuffer> txn,ByteBuffer key,ByteBuffer value){
        if(dbi==null) return false;
        return dbi.put(txn,key,value, PutFlags.MDB_NODUPDATA);
    }
    public boolean deleteEdge(Txn<ByteBuffer> txn,ByteBuffer key,ByteBuffer value){
        if(dbi==null) return false;
        return dbi.delete(txn,key,value);
    }
    public boolean deleteEdge(Txn<ByteBuffer> txn,ByteBuffer key){
        if(dbi==null) return false;
        return dbi.delete(txn,key);
    }
    public void onEdge(Txn<ByteBuffer> txn, ByteBuffer key, DataStore.BufferStream stream){
        if(dbi==null) return;
        try(Cursor<ByteBuffer> cursor = dbi.openCursor(txn)){
            if(!cursor.get(key, GetOp.MDB_SET)) return;
            if(!cursor.seek(SeekOp.MDB_FIRST_DUP)) return;
            if(!stream.on(BufferProxy.buffer(cursor.val()),null)) return;
            while (cursor.seek(SeekOp.MDB_NEXT_DUP)){
                if(!stream.on(BufferProxy.buffer(cursor.val()),null)) break;
            }
        }
    }

}
