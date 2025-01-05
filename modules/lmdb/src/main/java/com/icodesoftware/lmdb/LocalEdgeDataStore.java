package com.icodesoftware.lmdb;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.util.BufferProxy;
import org.lmdbjava.*;

import java.nio.ByteBuffer;

public class LocalEdgeDataStore {

    private final Metadata metadata;
    private final Dbi<ByteBuffer> dbi;
    private final LMDBEnv env;
    public LocalEdgeDataStore(final Metadata metadata,final Dbi<ByteBuffer> dbi){
        this(metadata,dbi,null);
    }
    public LocalEdgeDataStore(final Metadata metadata,final Dbi<ByteBuffer> dbi,LMDBEnv env){
        this.metadata = metadata;
        this.dbi = dbi;
        this.env = env;
    }
    public Metadata metadata(){
        return metadata;
    }
    public boolean addEdge(Txn<ByteBuffer> txn,ByteBuffer key,ByteBuffer value){
        check();
        return dbi.put(txn,key,value, PutFlags.MDB_NODUPDATA);
    }

    public boolean deleteEdge(Txn<ByteBuffer> txn,ByteBuffer key,ByteBuffer value){
        check();
        return dbi.delete(txn,key,value);
    }

    public boolean deleteEdge(Txn<ByteBuffer> txn,ByteBuffer key){
        check();
        return dbi.delete(txn,key);
    }

    public Cursor<ByteBuffer> openCursor(Txn<ByteBuffer> txn){
        check();
        return dbi.openCursor(txn);
    }

    public void onEdge(Txn<ByteBuffer> txn, ByteBuffer key, DataStore.BufferStream stream){
        check();
        try(Cursor<ByteBuffer> cursor = dbi.openCursor(txn)){
            if(!cursor.get(key, GetOp.MDB_SET)) return;
            if(!cursor.seek(SeekOp.MDB_FIRST_DUP)) return;
            if(!stream.on(com.icodesoftware.util.BufferProxy.buffer(cursor.val()),null)) return;
            while (cursor.seek(SeekOp.MDB_NEXT_DUP)){
                if(!stream.on(com.icodesoftware.util.BufferProxy.buffer(cursor.val()),null)) break;
            }
        }
    }
    public void drop(Txn<ByteBuffer> txn,boolean delete){
        dbi.drop(txn,delete);
    }
    public void close(){
        dbi.close();
    }

    private void check(){
        if(dbi==null) throw new RuntimeException("lmdb not opened");
    }

    public boolean addEdge(ByteBuffer key,ByteBuffer value){
        try(final Txn<ByteBuffer> txn = env.txnWrite()){
            if(!dbi.put(txn,key,value, PutFlags.MDB_NODUPDATA)) return false;
            txn.commit();
            return true;
        }
    }
    public boolean deleteEdge(ByteBuffer key){
        try(final Txn<ByteBuffer> txn = env.txnWrite()){
            if(!dbi.delete(txn,key)) return false;
            txn.commit();
            return true;
        }
    }
    public boolean deleteEdge(ByteBuffer key,ByteBuffer value){
        try(final Txn<ByteBuffer> txn = env.txnWrite()){
            if(!dbi.delete(txn,key,value)) return false;
            txn.commit();
            return true;
        }
    }
    public void onEdge(ByteBuffer key, DataStore.BufferStream stream){
        try(final Txn<ByteBuffer> txn = env.txnRead(); final Cursor<ByteBuffer> cursor = dbi.openCursor(txn)){
            if(!cursor.get(key, GetOp.MDB_SET)) return;
            if(!cursor.seek(SeekOp.MDB_FIRST_DUP)) return;
            if(!stream.on(com.icodesoftware.util.BufferProxy.buffer(cursor.val()),null)) return;
            while (cursor.seek(SeekOp.MDB_NEXT_DUP)){
                if(!stream.on(BufferProxy.buffer(cursor.val()),null)) break;
            }
        }
    }

    public String name(){
        return new String(dbi.getName());
    }

    public int partition(){
        return env.envSetting.partition;
    }

}
