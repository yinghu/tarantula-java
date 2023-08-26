package com.icodesoftware.lmdb;

import com.google.gson.JsonElement;
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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LMDBDataStoreProvider implements DataStoreProvider,MapStoreListener {

    private TarantulaLogger logger = JDKLogger.getLogger(LMDBDataStoreProvider.class);

    private String dataPath ="target/lmdb/data";
    private String integrationPath="target/lmdb/integration";
    private String indexPath = "target/lmdb/index";

    private String keyPath = "target/lmdb/key";
    private Env<ByteBuffer> data;
    private Env<ByteBuffer> integration;
    private Env<ByteBuffer> index;

    private Env<ByteBuffer> key;
    private Dbi<ByteBuffer> keyDbi;

    private long storeSize = 10_485_760;//10M
    private int maxDatabaseNumber = 1024;
    private int maxReaders = 16;

    private long startId = 1_000_000;//
    private final static ConcurrentHashMap<String,LMDBDataStore> storeMap = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<String,Dbi<ByteBuffer>> edgMap = new ConcurrentHashMap<>();
    @Override
    public void configure(Map<String, Object> properties) {
        String _dataPath = ((JsonElement)properties.get("dataPath")).getAsString();
        String _integrationPath = ((JsonElement)properties.get("integrationPath")).getAsString();
        String _indexPath = ((JsonElement)properties.get("indexPath")).getAsString();
        String _keyPath = ((JsonElement)properties.get("keyPath")).getAsString();
        this.dataPath = properties.get("dir")+ FileSystems.getDefault().getSeparator()+_dataPath;
        this.integrationPath =properties.get("dir")+ FileSystems.getDefault().getSeparator()+_integrationPath;
        this.indexPath = properties.get("dir")+ FileSystems.getDefault().getSeparator()+_indexPath;
        this.keyPath = properties.get("dir")+ FileSystems.getDefault().getSeparator()+_keyPath;
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
            Dbi<ByteBuffer> dbi = integration.openDbi(name, DbiFlags.MDB_CREATE);
            return new LMDBDataStore(name,dbi,integration,this);
        });
    }

    public Dbi<ByteBuffer> createEdgeDB(String edgeName){
        return edgMap.computeIfAbsent(edgeName,k->data.openDbi(edgeName, DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT));
    }

    @Override
    public DataStore createKeyIndexDataStore(String name) {
        return storeMap.computeIfAbsent(name,k->{
            Dbi<ByteBuffer> dbi = index.openDbi(name, DbiFlags.MDB_CREATE);
            return new LMDBDataStore(name,dbi,index,this);
        });
    }

    @Override
    public DataStore create(String name, int partition) {
        return storeMap.computeIfAbsent(name,k->{
            Dbi<ByteBuffer> dbi = data.openDbi(name, DbiFlags.MDB_CREATE);
            return new LMDBDataStore(name,dbi,data,this);
        });
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
        data = Env.create().setMapSize(storeSize).setMaxDbs(maxDatabaseNumber).setMaxReaders(maxReaders).open(path(this.dataPath).toFile());
        integration = Env.create().setMapSize(storeSize).setMaxDbs(maxDatabaseNumber).setMaxReaders(maxReaders).open(path(this.integrationPath).toFile());
        index = Env.create().setMapSize(storeSize).setMaxDbs(maxDatabaseNumber).setMaxReaders(maxReaders).open(path(this.indexPath).toFile());
        key = Env.create().setMapSize(storeSize).setMaxDbs(maxDatabaseNumber).setMaxReaders(maxReaders).open(this.path(keyPath).toFile());
        keyDbi = key.openDbi("keys",DbiFlags.MDB_CREATE);
        logger.warn("LMDB Provider started");
    }

    @Override
    public void shutdown() throws Exception {
        keyDbi.close();
        storeMap.forEach((k,v)->v.close());
        storeMap.clear();
        edgMap.forEach((k,v)->v.close());
        edgMap.clear();
        data.close();
        integration.close();
        index.close();
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

    private Path path(String path) throws Exception{
        Path _path = Paths.get(path);
        if(!Files.exists(_path)) Files.createDirectories(_path);
        return _path;
    }

}
