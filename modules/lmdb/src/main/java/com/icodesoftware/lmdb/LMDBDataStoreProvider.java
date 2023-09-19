package com.icodesoftware.lmdb;

import com.google.gson.JsonElement;
import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.service.MapStoreListener;
import com.icodesoftware.service.Metadata;

import com.icodesoftware.service.ServiceContext;
import org.lmdbjava.Dbi;
import org.lmdbjava.DbiFlags;
import org.lmdbjava.Env;

import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class LMDBDataStoreProvider implements DataStoreProvider,MapStoreListener {

    private TarantulaLogger logger = JDKLogger.getLogger(LMDBDataStoreProvider.class);

    private String name;
    private String dataPath ="target/lmdb/data";
    private String integrationPath="target/lmdb/integration";
    private String indexPath = "target/lmdb/index";

    private String localPath = "target/lmdb/local";
    private Env<ByteBuffer> data;
    private Env<ByteBuffer> integration;
    private Env<ByteBuffer> index;
    private Env<ByteBuffer> local;

    private long storeSize = 1_048_576L; // 1MB = 1,048,576 (1024*1024)
    private int maxDatabaseNumber = 1024;
    private int maxReaders = 16;

    private final static int KEY_SIZE = 200;
    private final static int VALUE_SIZE = 1800;

    private final static int PENDING_BUFFER_SIZE = 16;
    private final static ConcurrentHashMap<String,LMDBDataStore> storeMap = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<String,LocalEdgeDataStore> edgMap = new ConcurrentHashMap<>();

    final static ArrayBlockingQueue<BufferCache> pendingQueue = new ArrayBlockingQueue<>(PENDING_BUFFER_SIZE);;


    private MapStoreListener integrationMapStoreListener;
    private MapStoreListener keyIndexMapStoreListener;
    private MapStoreListener dataMapStoreListener;
    private DistributionIdGenerator distributionIdGenerator;


    @Override
    public void configure(Map<String, Object> properties) {
        this.name = (String)properties.get("name");
        this.storeSize = storeSize*(int)properties.get("storeSizeMb");
        String _dataPath = ((JsonElement)properties.get("dataPath")).getAsString();
        String _integrationPath = ((JsonElement)properties.get("integrationPath")).getAsString();
        String _indexPath = ((JsonElement)properties.get("indexPath")).getAsString();
        String _localPath = ((JsonElement)properties.get("localPath")).getAsString();
        this.dataPath = properties.get("dir")+ FileSystems.getDefault().getSeparator()+_dataPath;
        this.integrationPath =properties.get("dir")+ FileSystems.getDefault().getSeparator()+_integrationPath;
        this.indexPath = properties.get("dir")+ FileSystems.getDefault().getSeparator()+_indexPath;
        this.localPath = properties.get("dir")+ FileSystems.getDefault().getSeparator()+_localPath;
    }

    @Override
    public void registerMapStoreListener(int scope, MapStoreListener mapStoreListener) {
        if(scope==Distributable.INTEGRATION_SCOPE){
            integrationMapStoreListener = mapStoreListener;
            return;
        }
        if(scope==Distributable.INDEX_SCOPE){
            keyIndexMapStoreListener = mapStoreListener;
            return;
        }
        if(scope==Distributable.DATA_SCOPE){
            dataMapStoreListener = mapStoreListener;
            return;
        }
        throw new RuntimeException("Scope ["+scope+"] not supported");
    }
    @Override
    public void registerDistributionIdGenerator(DistributionIdGenerator distributionIdGenerator){
        if(distributionIdGenerator!=null) this.distributionIdGenerator = distributionIdGenerator;
    }
    @Override
    public MapStoreListener mapStoreListener(int scope) {
        if(scope==Distributable.INTEGRATION_SCOPE){
            return integrationMapStoreListener;
        }
        if(scope==Distributable.INDEX_SCOPE){
            return keyIndexMapStoreListener;
        }
        if(scope==Distributable.DATA_SCOPE){
            return dataMapStoreListener;
        }
        throw new RuntimeException("Scope ["+scope+"] not supported");
    }

    @Override
    public DataStore createAccessIndexDataStore(String name) {
        return storeMap.computeIfAbsent(name,k->{
            Dbi<ByteBuffer> dbi = integration.openDbi(name, DbiFlags.MDB_CREATE);
            return new LMDBDataStore(Distributable.INTEGRATION_SCOPE,name,dbi,integration,this);
        });
    }

    public LocalEdgeDataStore createEdgeDB(int scope,String source,String label){
        final String edgeName = source+"_"+label;
        if(scope== Distributable.DATA_SCOPE){
            return edgMap.computeIfAbsent(edgeName,k-> new LocalEdgeDataStore(new LocalMetadata(scope,source,label),data.openDbi(edgeName, DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT)));
        }
        if(scope==Distributable.INTEGRATION_SCOPE){
            return edgMap.computeIfAbsent(edgeName,k-> new LocalEdgeDataStore(new LocalMetadata(scope,source,label),integration.openDbi(edgeName, DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT)));
        }
        if(scope==Distributable.INDEX_SCOPE){
            return edgMap.computeIfAbsent(edgeName,k-> new LocalEdgeDataStore(new LocalMetadata(scope,source,label),index.openDbi(edgeName, DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT)));
        }
        if(scope==Distributable.LOCAL_SCOPE){
            return edgMap.computeIfAbsent(edgeName,k-> new LocalEdgeDataStore(new LocalMetadata(scope,source,label),local.openDbi(edgeName, DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT)));
        }
        throw new RuntimeException("Scope ["+scope+"] not supported");
    }

    @Override
    public DataStore createKeyIndexDataStore(String name) {
        return storeMap.computeIfAbsent(name,k->{
            Dbi<ByteBuffer> dbi = index.openDbi(name, DbiFlags.MDB_CREATE);
            return new LMDBDataStore(Distributable.INDEX_SCOPE,name,dbi,index,this);
        });
    }
    @Override
    public DataStore createDataStore(String name){
        return storeMap.computeIfAbsent(name,k->{
            Dbi<ByteBuffer> dbi = data.openDbi(name, DbiFlags.MDB_CREATE);
            return new LMDBDataStore(Distributable.DATA_SCOPE,name,dbi,data,this);
        });
    }
    public DataStore createLocalDataStore(String name){
        return storeMap.computeIfAbsent(name,k->{
            Dbi<ByteBuffer> dbi = local.openDbi(name, DbiFlags.MDB_CREATE);
            return new LMDBDataStore(Distributable.LOCAL_SCOPE,name,dbi,local,this);
        });
    }
    @Override
    public List<String> list() {
        ArrayList<String> dlist = new ArrayList<>();
        storeMap.forEach((k,v)->{
            dlist.add(k);
        });
        return dlist;
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
        return this.name;
    }

    @Override
    public void start() throws Exception {
        if(distributionIdGenerator==null) throw new RuntimeException("DistributionIdGenerator Not Registered");
        data = Env.create().setMapSize(storeSize).setMaxDbs(maxDatabaseNumber).setMaxReaders(maxReaders).open(path(this.dataPath).toFile());
        integration = Env.create().setMapSize(storeSize).setMaxDbs(maxDatabaseNumber).setMaxReaders(maxReaders).open(path(this.integrationPath).toFile());
        index = Env.create().setMapSize(storeSize).setMaxDbs(maxDatabaseNumber).setMaxReaders(maxReaders).open(path(this.indexPath).toFile());
        local = Env.create().setMapSize(storeSize).setMaxDbs(maxDatabaseNumber).setMaxReaders(maxReaders).open(this.path(localPath).toFile());
        for(int i=0;i<PENDING_BUFFER_SIZE;i++){
            pendingQueue.offer(new BufferCache(KEY_SIZE,VALUE_SIZE,pendingQueue));
        }
        logger.warn("LMDB Provider started with store size ["+storeSize+"]["+pendingQueue.size()+"]");
    }

    @Override
    public void setup(ServiceContext serviceContext){
        if(keyIndexMapStoreListener!=null){
            keyIndexMapStoreListener.setup(serviceContext);
        }
        if(integrationMapStoreListener!=null){
            integrationMapStoreListener.setup(serviceContext);
        }
        if(dataMapStoreListener!=null){
            dataMapStoreListener.setup(serviceContext);
        }
        logger.warn("LMDB provider map store listener setup");
    }

    @Override
    public void waitForData() {
        if(keyIndexMapStoreListener!=null){
            keyIndexMapStoreListener.waitForData();
        }
        if(integrationMapStoreListener!=null){
            integrationMapStoreListener.waitForData();
        }
        if(dataMapStoreListener!=null){
            dataMapStoreListener.waitForData();
        }
        logger.warn("LMDB provider map store listener waitingForData");
    }

    @Override
    public void shutdown() throws Exception {
        storeMap.forEach((k,v)->v.close());
        storeMap.clear();
        edgMap.forEach((k,v)->v.dbi.close());
        edgMap.clear();
        data.close();
        integration.close();
        index.close();
        local.close();
        logger.warn("LMDB Shutting down with pending buffer size ["+pendingQueue.size()+"]");
        pendingQueue.clear();
    }


    public void onDistributing(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value){
        if(metadata.scope()==Distributable.INTEGRATION_SCOPE && integrationMapStoreListener!=null){
            integrationMapStoreListener.onDistributing(metadata,key,value);
            return;
        }
        if(metadata.scope()==Distributable.DATA_SCOPE && dataMapStoreListener!=null){
            dataMapStoreListener.onDistributing(metadata,key,value);
            return;
        }
        if(metadata.scope()==Distributable.INDEX_SCOPE && keyIndexMapStoreListener!=null){
            dataMapStoreListener.onDistributing(metadata,key,value);
        }
    }

    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer bufferStream){
        if(metadata.scope()==Distributable.INTEGRATION_SCOPE && integrationMapStoreListener!=null){
            return integrationMapStoreListener.onRecovering(metadata,key,bufferStream);
        }
        if(metadata.scope()==Distributable.DATA_SCOPE && dataMapStoreListener!=null){
            return dataMapStoreListener.onRecovering(metadata,key,bufferStream);

        }
        if(metadata.scope()==Distributable.INDEX_SCOPE && keyIndexMapStoreListener!=null){
            return dataMapStoreListener.onRecovering(metadata,key,bufferStream);
        }
        return false;
    }


    @Override
    public void onDeleting(Metadata metadata,Recoverable.DataBuffer key, Recoverable.DataBuffer value) {
        if(metadata.scope()==Distributable.INTEGRATION_SCOPE && integrationMapStoreListener!=null){
            integrationMapStoreListener.onDeleting(metadata,key,value);
        }
        if(metadata.scope()==Distributable.DATA_SCOPE && dataMapStoreListener!=null){
             dataMapStoreListener.onDeleting(metadata,key,value);

        }
        if(metadata.scope()==Distributable.INDEX_SCOPE && keyIndexMapStoreListener!=null){
            dataMapStoreListener.onDeleting(metadata,key,value);
        }
    }

    public void assign(Recoverable.DataBuffer dataBuffer){
        this.distributionIdGenerator.assign(dataBuffer);
    }

    private Path path(String path) throws Exception{
        Path _path = Paths.get(path);
        if(!Files.exists(_path)) Files.createDirectories(_path);
        return _path;
    }

    BufferCache fromCache(){
        BufferCache cache = pendingQueue.poll();
        if(cache!=null) return cache;
        return new BufferCache(KEY_SIZE,VALUE_SIZE,pendingQueue);
    }

}
