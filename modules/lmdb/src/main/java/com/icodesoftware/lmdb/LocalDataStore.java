package com.icodesoftware.lmdb;

import com.icodesoftware.Recoverable;
import com.icodesoftware.service.Metadata;
import org.lmdbjava.Dbi;
import org.lmdbjava.Txn;

import java.nio.ByteBuffer;

public class LocalDataStore {

    private final Metadata metadata;
    private final Dbi<ByteBuffer> dbi;
    private final LMDBEnv env;

    public LocalDataStore(Metadata metadata, Dbi<ByteBuffer> dbi, LMDBEnv env){
        this.metadata = metadata;
        this.dbi = dbi;
        this.env = env;
    }

    public boolean put(Recoverable.DataBuffer key, Recoverable.DataBuffer value){
        try(final Txn<ByteBuffer> txn = env.txnWrite()){
            boolean suc = dbi.put(txn,key.src(),value.src());
            if(!suc)  return false;
            txn.commit();
            return true;
        }
    }
    public boolean get(Recoverable.DataBuffer key, Recoverable.DataBuffer value){
        try(final Txn<ByteBuffer> txn = env.txnRead()){
            ByteBuffer ret = dbi.get(txn,key.src());
            if(ret==null) return false;
            while (ret.hasRemaining()){
                value.writeByte(ret.get());
            }
            return true;
        }
    }

    public boolean delete(Recoverable.DataBuffer key){
        try(final Txn<ByteBuffer> txn = env.txnWrite()){
            boolean suc = dbi.delete(txn,key.src());
            if(!suc)  return false;
            txn.commit();
            return true;
        }
    }
    public Metadata metadata(){
        return metadata;
    }

    public String name(){
        return new String(dbi.getName());
    }

    public int partition(){
        return env.envSetting.partition;
    }

}
