package com.icodesoftware.lmdb;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.service.MapStoreListener;
import com.icodesoftware.service.Metadata;
import org.lmdbjava.Dbi;
import org.lmdbjava.DbiFlags;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LMDBDataStoreProvider implements DataStoreProvider,MapStoreListener {

    //private TarantulaLogger logger = JDKLogger.getLogger(LMDBDataStoreProvider.class);
    private Env<ByteBuffer> data;
    private Env<ByteBuffer> key;
    private Dbi<ByteBuffer> keyDbi;
    private String dir = "/var/tarantula/tds/lmdb";
    private long storeSize = 10_485_760;//10M
    private int maxDatabaseNumber = 1024;
    private int maxReaders = 16;

    private long startId = 1_000_000;//
    private final static ConcurrentHashMap<String,LMDBDataStore> storeMap = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<String,Dbi<ByteBuffer>> edgMap = new ConcurrentHashMap<>();
    @Override
    public void configure(Map<String, Object> properties) {
        dir = (String) properties.get("dir");
    }

    private MapStoreListener mapStoreListener;
    @Override
    public void registerMapStoreListener(int scope, MapStoreListener mapStoreListener) {
        this.mapStoreListener = mapStoreListener;
    }

    @Override
    public MapStoreListener mapStoreListener(int scope) {
        return null;
    }

    @Override
    public DataStore createAccessIndexDataStore(String name) {
        return storeMap.computeIfAbsent(name,k->{
            Dbi<ByteBuffer> dbi = data.openDbi(name, DbiFlags.MDB_CREATE);
            return new LMDBDataStore(name,dbi,data,this);
        });
    }

    public Dbi<ByteBuffer> createEdgeDB(String edgeName){
        return edgMap.computeIfAbsent(edgeName,k->data.openDbi(edgeName, DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT));
    }

    @Override
    public DataStore createKeyIndexDataStore(String name) {
        return createAccessIndexDataStore(name);
    }

    @Override
    public DataStore create(String name, int partition) {
        return createAccessIndexDataStore(name);
    }

    @Override
    public List<String> list() {
        return null;
    }

    @Override
    public DataStore lookup(String name) {
        return storeMap.get(name);
    }

    @Override
    public void backup(int scope) {

    }

    @Override
    public void backup(int scope, OnBackup backup) {

    }

    @Override
    public void recover(int scope, OnBackup backup) {

    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public void start() throws Exception {
        Path dataPath = Paths.get(dir+"/data");
        if(!Files.exists(dataPath)) Files.createDirectories(dataPath);
        data = Env.create().setMapSize(storeSize).setMaxDbs(maxDatabaseNumber).setMaxReaders(maxReaders).open(dataPath.toFile());
        Path keyPath = Paths.get(dir+"/key");
        if(!Files.exists(keyPath)) Files.createDirectories(keyPath);
        key = Env.create().setMapSize(storeSize).setMaxDbs(maxDatabaseNumber).setMaxReaders(maxReaders).open(keyPath.toFile());
        keyDbi = key.openDbi("keys",DbiFlags.MDB_CREATE);
        //logger.warn("LMDB started");
    }

    @Override
    public void shutdown() throws Exception {
        storeMap.forEach((k,v)->v.close());
        storeMap.clear();
        edgMap.forEach((k,v)->v.close());
        edgMap.clear();
        data.close();
        key.close();
    }

    @Override
    public <T extends Recoverable> void onBackingUp(Metadata metadata, String key, T t) {

    }

    @Override
    public void onDistributing(Metadata metadata, String stringKey, byte[] key, byte[] value) {

    }
    public void onDistributing(Metadata metadata, ByteBuffer key, ByteBuffer value){
        if(mapStoreListener==null) return;
        mapStoreListener.onDistributing(metadata,key,value);
    }
    @Override
    public byte[] onRecovering(Metadata metadata, String stringKey, byte[] key) {
        return null;
    }

    @Override
    public void onDeleting(Metadata metadata, byte[] key) {

    }

    public long nextId(String name) {

        ByteBuffer idKey = ByteBuffer.allocateDirect(key.getMaxKeySize());
        idKey.put(name.getBytes());
        idKey.flip();
        ByteBuffer id = ByteBuffer.allocateDirect(key.getMaxKeySize());
        long pendingId = startId;
        Txn<ByteBuffer> txn = key.txnWrite();
        try {
            if(keyDbi.get(txn,idKey)!=null) {
                pendingId = txn.val().getLong();
            }
            idKey.rewind();
            id.putLong(pendingId+1).flip();
            keyDbi.put(txn,idKey,id);
            txn.commit();
            return pendingId+1;
        }finally {
            txn.close();
        }
    }

}
