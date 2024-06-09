package com.icodesoftware.lmdb;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.Serviceable;
import org.lmdbjava.*;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class LMDBEnv implements Serviceable {

    public static final LMDBEnv DATA_ENV = new LMDBEnv(EnvSetting.DataSetting);
    public static final LMDBEnv INTEGRATION_ENV = new LMDBEnv(EnvSetting.IntegrationSetting);
    public static final LMDBEnv INDEX_ENV = new LMDBEnv(EnvSetting.IndexSetting);
    public static final LMDBEnv LOG_ENV = new LMDBEnv(EnvSetting.LogSetting);
    public static final LMDBEnv LOCAL_ENV = new LMDBEnv(EnvSetting.LocalSetting);

    public EnvSetting envSetting;
    public Env<ByteBuffer> env;
    public LMDBDataStoreProvider lmdbDataStoreProvider;
    public LMDBEnv(EnvSetting envSetting){
        this.envSetting = envSetting;
    }

    @Override
    public void start() throws Exception {
        if(!envSetting.enabled) return;
        if(!lmdbDataStoreProvider.envNoSyncFlag){
            env = Env.create().setMapSize(storeSize(this.envSetting)).setMaxDbs(lmdbDataStoreProvider.maxDatabaseNumber).setMaxReaders(lmdbDataStoreProvider.maxReaders).open(path(this.envSetting.storePath).toFile());
            return;
        }
        EnvFlags[] flags = new EnvFlags[]{EnvFlags.MDB_NOSYNC};
        env = Env.create().setMapSize(storeSize(this.envSetting)).setMaxDbs(lmdbDataStoreProvider.maxDatabaseNumber).setMaxReaders(lmdbDataStoreProvider.maxReaders).open(path(this.envSetting.storePath).toFile(),flags);
    }

    public DataStore createDataStore(int scope,String name,Txn<ByteBuffer> txn,long transactionId){
        if(!envSetting.enabled) throw new RuntimeException("lmdb ["+envSetting.name+"] disabled");
        Dbi<ByteBuffer> dbi = txn==null? env.openDbi(name, DbiFlags.MDB_CREATE) : env.openDbi(txn,name.getBytes(),null,false,DbiFlags.MDB_CREATE);
        return txn==null? new CachedLMDBDataStore(scope,name,dbi,env,lmdbDataStoreProvider) : new LMDBDataStore(scope,name,dbi,env,lmdbDataStoreProvider,txn,transactionId);
    }

    public LocalEdgeDataStore localEdgeDataStore(int scope,String source,String label,Txn<ByteBuffer> txn){
        if(!envSetting.enabled) throw new RuntimeException("lmdb ["+envSetting.name+"] disabled");
        String edgeName = source+"#"+label;
        Dbi<ByteBuffer> dbi = txn==null? env.openDbi(edgeName,DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT) : env.openDbi(txn,edgeName.getBytes(),null,false,DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT);
        return new LocalEdgeDataStore(new LocalMetadata(scope,source,label),dbi);
    }

    public Txn<ByteBuffer> txnWrite(){
        if(!envSetting.enabled) throw new RuntimeException("lmdb ["+envSetting.name+"] disabled");
        return env.txnWrite();
    }
    public Txn<ByteBuffer> txnRead(){
        if(!envSetting.enabled) throw new RuntimeException("lmdb ["+envSetting.name+"] disabled");
        return env.txnRead();
    }
    public List<byte[]> getDbiNames(){
        if(!envSetting.enabled) return new ArrayList<>();
        return env.getDbiNames();
    }
    public void copy(File file){
        if(!envSetting.enabled) return;
        env.sync(true);
        env.copy(file,CopyFlags.MDB_CP_COMPACT);
    }

    @Override
    public void shutdown() throws Exception {
        if(!envSetting.enabled) return;
        env.sync(true);
        env.close();
    }
    private Path path(String path) throws Exception{
        Path _path = Paths.get(path);
        if(!Files.exists(_path)) Files.createDirectories(_path);
        return _path;
    }
    private long storeSize(EnvSetting envSetting){
        if(envSetting.mbSize==0) return lmdbDataStoreProvider.storeSize;
        return lmdbDataStoreProvider.storeBaseMbSize*envSetting.mbSize;
    }
}
