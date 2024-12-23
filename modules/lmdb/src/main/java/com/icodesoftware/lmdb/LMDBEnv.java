package com.icodesoftware.lmdb;

import com.icodesoftware.DataStore;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
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

    private static final TarantulaLogger logger = JDKLogger.getLogger(LMDBEnv.class);

    public EnvSetting envSetting;
    public Env<ByteBuffer> env;
    public LocalLMDBProvider lmdbDataStoreProvider;

    public LMDBEnv(EnvSetting envSetting){
        this.envSetting = envSetting;
    }

    @Override
    public void start() throws Exception {
        if(!envSetting.enabled) return;
        logger.warn("Starting Env : "+envSetting.name+" : "+envSetting.storePath+" : "+lmdbDataStoreProvider.diskSyncOnCommit());
        if(lmdbDataStoreProvider.diskSyncOnCommit()){
            env = Env.create().setMapSize(storeSize(this.envSetting)).setMaxDbs(lmdbDataStoreProvider.maxDatabaseNumber()).setMaxReaders(lmdbDataStoreProvider.maxReaderNumber()).open(path(this.envSetting.storePath).toFile());
            return;
        }
        EnvFlags[] flags = new EnvFlags[]{EnvFlags.MDB_NOSYNC};
        env = Env.create().setMapSize(storeSize(this.envSetting)).setMaxDbs(lmdbDataStoreProvider.maxDatabaseNumber()).setMaxReaders(lmdbDataStoreProvider.maxReaderNumber()).open(path(this.envSetting.storePath).toFile(),flags);
    }

    public DataStore createDataStore(String name,Txn<ByteBuffer> txn,long transactionId){
        if(!envSetting.enabled) throw new RuntimeException("lmdb ["+envSetting.name+"] disabled");
        Dbi<ByteBuffer> dbi = txn==null? env.openDbi(name, DbiFlags.MDB_CREATE) : env.openDbi(txn,name.getBytes(),null,false,DbiFlags.MDB_CREATE);
        return txn==null? new CachedLMDBDataStore(name,dbi,this) : new LMDBDataStore(name,dbi,txn,transactionId,this);
    }

    public LocalEdgeDataStore localEdgeDataStore(String source,String label,Txn<ByteBuffer> txn){
        if(!envSetting.enabled) throw new RuntimeException("lmdb ["+envSetting.name+"] disabled");
        String edgeName = source+"#"+label;
        Dbi<ByteBuffer> dbi = txn==null? env.openDbi(edgeName,DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT) : env.openDbi(txn,edgeName.getBytes(),null,false,DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT);
        return new LocalEdgeDataStore(new LocalMetadata(envSetting.scope,source,label),dbi);
    }

    public Txn<ByteBuffer> txnWrite(){
        if(!envSetting.enabled) throw new RuntimeException("lmdb ["+envSetting.name+"] disabled");
        return env.txnWrite();
    }
    public Txn<ByteBuffer> txnRead(){
        if(!envSetting.enabled) throw new RuntimeException("lmdb ["+envSetting.name+"] disabled");
        return env.txnRead();
    }
    public Txn<ByteBuffer> txn(Txn<ByteBuffer> parent){
        if(!envSetting.enabled) throw new RuntimeException("lmdb ["+envSetting.name+"] disabled");
        if(parent==null) new RuntimeException("parent context cannot be null");
        return env.txn(parent);
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
        logger.warn("Sync on shutdown : "+envSetting.storePath);
        env.sync(true);
        env.close();
    }
    private Path path(String path) throws Exception{
        Path _path = Paths.get(path);
        if(!Files.exists(_path)) Files.createDirectories(_path);
        return _path;
    }
    private long storeSize(EnvSetting envSetting){
        if(envSetting.mbSize==0) return lmdbDataStoreProvider.storeSize();
        logger.warn("Env ["+envSetting.name+"] starting with store size :"+envSetting.mbSize+"MB");
        return EnvSetting.toBytesFromMb(envSetting.mbSize);
    }
}
