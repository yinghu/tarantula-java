package com.icodesoftware.lmdb.partition;

import com.icodesoftware.lmdb.BufferProxy;
import com.icodesoftware.lmdb.EnvSetting;
import com.icodesoftware.service.Serviceable;
import org.lmdbjava.*;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LMDBPartitionDaemon implements LMDBPartition,Serviceable {

    private static EnvFlags[] flags = new EnvFlags[]{EnvFlags.MDB_NOSYNC};
    private static int maxStores = 10;
    private static int maxReaders = 10;

    private final EnvSetting envSetting;
    private Env<ByteBuffer> env;

    public final int partition;

    public LMDBPartitionDaemon(EnvSetting envSetting){
        this.envSetting = envSetting;
        this.partition = envSetting.partition;
    }

    @Override
    public void start() throws Exception {
        Path path = Paths.get(envSetting.storePath);
        env = Env.create().setMapSize(EnvSetting.toBytesFromMb(envSetting.mbSize)).setMaxDbs(maxStores).setMaxReaders(maxReaders).open(path.toFile(),flags);
    }

    @Override
    public void shutdown() throws Exception {
        env.sync(true);
        env.close();
    }

    public boolean put(String dbiName,ByteBuffer key,ByteBuffer value){
        Dbi<ByteBuffer> dbi = env.openDbi(dbiName, DbiFlags.MDB_CREATE);
        try(Txn<ByteBuffer> txn = env.txnWrite()){
            if(!dbi.put(txn,key,value)) return false;
            txn.commit();
            return true;
        }
    }

    public ByteBuffer get(String dbiName, ByteBuffer key){
        Dbi<ByteBuffer> dbi = env.openDbi(dbiName, DbiFlags.MDB_CREATE);
        try(Txn<ByteBuffer> txn = env.txnRead()){
            if(dbi.get(txn,key)==null){
                return null;
            }
            return BufferProxy.copy(txn.val()).src();
        }
    }

    public boolean delete(String dbiName,ByteBuffer key){
        Dbi<ByteBuffer> dbi = env.openDbi(dbiName, DbiFlags.MDB_CREATE);
        try(Txn<ByteBuffer> txn = env.txnWrite()){
            if(!dbi.delete(txn,key)) return false;
            txn.commit();
            return true;
        }
    }


}
