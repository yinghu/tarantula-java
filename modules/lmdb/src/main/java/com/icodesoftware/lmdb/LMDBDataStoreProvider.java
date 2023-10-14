package com.icodesoftware.lmdb;

import com.google.gson.JsonElement;
import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.service.MapStoreListener;
import com.icodesoftware.service.Metadata;

import com.icodesoftware.service.ServiceContext;
import org.lmdbjava.Dbi;
import org.lmdbjava.DbiFlags;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

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

public class LMDBDataStoreProvider implements DataStoreProvider,MapStoreListener{

    private TarantulaLogger logger = JDKLogger.getLogger(LMDBDataStoreProvider.class);

    private String name;
    private String dataPath ="target/lmdb/data";
    private String integrationPath="target/lmdb/integration";
    private String indexPath = "target/lmdb/index";
    private String localPath = "target/lmdb/local";

    private String logPath = "target/lmdb/log";

    public Env<ByteBuffer> data;
    private Env<ByteBuffer> integration;
    private Env<ByteBuffer> index;
    private Env<ByteBuffer> local;
    private Env<ByteBuffer> log;
    private long storeSize = 1_048_576L; // 1MB = 1,048,576 (1024*1024)
    private int maxDatabaseNumber = 1024;
    private int maxReaders = 100;

    private final static int KEY_SIZE = 200;
    private final static int VALUE_SIZE = 1800;

    private final static int PENDING_BUFFER_SIZE = 16;
    private final static ConcurrentHashMap<String,DataStore> storeMap = new ConcurrentHashMap<>();
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
        String _logPath = ((JsonElement)properties.get("logPath")).getAsString();
        this.dataPath = properties.get("dir")+ FileSystems.getDefault().getSeparator()+_dataPath;
        this.integrationPath =properties.get("dir")+ FileSystems.getDefault().getSeparator()+_integrationPath;
        this.indexPath = properties.get("dir")+ FileSystems.getDefault().getSeparator()+_indexPath;
        this.localPath = properties.get("dir")+ FileSystems.getDefault().getSeparator()+_localPath;
        this.logPath = properties.get("dir")+ FileSystems.getDefault().getSeparator()+_logPath;
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
    public DataStore createAccessIndexDataStore(String name) {
        return storeMap.computeIfAbsent(name,k->createDataStore(Distributable.INTEGRATION_SCOPE,name,null));
    }

    public LocalEdgeDataStore createEdgeDB(int scope,String source,String label){
        final String edgeName = source+"#"+label;
        return edgMap.computeIfAbsent(edgeName,k->localEdgeDataStore(scope,source,label,null));
    }

    @Override
    public DataStore createKeyIndexDataStore(String name) {
        return storeMap.computeIfAbsent(name,k-> createDataStore(Distributable.INDEX_SCOPE,name,null));
    }
    @Override
    public DataStore createDataStore(String name){
        return storeMap.computeIfAbsent(name,k->createDataStore(Distributable.DATA_SCOPE, name, null));
    }
    public DataStore createLocalDataStore(String name){
        return storeMap.computeIfAbsent(name,k->createDataStore(Distributable.LOCAL_SCOPE,name,null));
    }
    public DataStore createLogDataStore(String name){
        return storeMap.computeIfAbsent(name,k->createDataStore(Distributable.LOG_SCOPE,name,null));
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
    public List<String> list(int scope){
        ArrayList<String> dlist = new ArrayList<>();
        storeMap.forEach((k,v)->{
            if(v.scope()==scope) dlist.add(k);
        });
        return dlist;
    }

    @Override
    public DataStore lookup(String name) {
        return storeMap.get(name);
    }

    public Transaction transaction(int scope){
        return new LocalTransaction(scope,this);
    }

    public Txn<ByteBuffer> txn(int scope){
        if(scope==Distributable.DATA_SCOPE) return data.txnWrite();
        if(scope==Distributable.INTEGRATION_SCOPE) return integration.txnWrite();
        if(scope==Distributable.INDEX_SCOPE) return index.txnWrite();
        if(scope==Distributable.LOCAL_SCOPE) return local.txnWrite();
        if(scope==Distributable.LOG_SCOPE) return log.txnWrite();
        throw new RuntimeException("Scope ["+scope+"] not supported");
    }

    public DataStore createDataStore(int scope,String name,Txn<ByteBuffer> txn){
        if(scope==Distributable.DATA_SCOPE){
            Dbi<ByteBuffer> dbi = txn==null? data.openDbi(name,DbiFlags.MDB_CREATE) : data.openDbi(txn,name.getBytes(),null,DbiFlags.MDB_CREATE);
            return txn==null? new CachedLMDBDataStore(scope,name,dbi,data,this) : new LMDBDataStore(scope,name,dbi,data,this,txn);
        }
        if(scope==Distributable.INTEGRATION_SCOPE){
            Dbi<ByteBuffer> dbi = txn==null? integration.openDbi(name,DbiFlags.MDB_CREATE) : integration.openDbi(txn,name.getBytes(),null,DbiFlags.MDB_CREATE);
            return txn==null? new CachedLMDBDataStore(scope,name,dbi,integration,this) : new LMDBDataStore(scope,name,dbi,integration,this,txn);
        }
        if(scope==Distributable.INDEX_SCOPE){
            Dbi<ByteBuffer> dbi = txn==null? index.openDbi(name,DbiFlags.MDB_CREATE) : index.openDbi(txn,name.getBytes(),null,DbiFlags.MDB_CREATE);
            return txn==null? new CachedLMDBDataStore(scope,name,dbi,index,this) : new LMDBDataStore(scope,name,dbi,index,this,txn);
        }
        if(scope==Distributable.LOCAL_SCOPE){
            Dbi<ByteBuffer> dbi = txn==null? local.openDbi(name,DbiFlags.MDB_CREATE) : local.openDbi(txn,name.getBytes(),null,DbiFlags.MDB_CREATE);
            return txn==null? new CachedLMDBDataStore(scope,name,dbi,local,this) : new LMDBDataStore(scope,name,dbi,local,this,txn);
        }
        if(scope==Distributable.LOG_SCOPE){
            Dbi<ByteBuffer> dbi = txn==null? log.openDbi(name,DbiFlags.MDB_CREATE) : log.openDbi(txn,name.getBytes(),null,DbiFlags.MDB_CREATE);
            return txn==null? new CachedLMDBDataStore(scope,name,dbi,log,this) : new LMDBDataStore(scope,name,dbi,log,this,txn);
        }
        throw new RuntimeException("Scope ["+scope+"] not supported");
    }
    public LocalEdgeDataStore localEdgeDataStore(int scope,String source,String label,Txn<ByteBuffer> txn){
        if(scope==Distributable.DATA_SCOPE){
            String edgeName = source+"#"+label;
            Dbi<ByteBuffer> dbi = txn==null? data.openDbi(edgeName,DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT) : data.openDbi(txn,edgeName.getBytes(),null,DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT);
            return new LocalEdgeDataStore(new LocalMetadata(scope,source,label),dbi);
        }
        if(scope==Distributable.INTEGRATION_SCOPE){
            String edgeName = source+"#"+label;
            Dbi<ByteBuffer> dbi = txn==null? integration.openDbi(edgeName,DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT) : integration.openDbi(txn,edgeName.getBytes(),null,DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT);
            return new LocalEdgeDataStore(new LocalMetadata(scope,source,label),dbi);
        }
        if(scope==Distributable.INDEX_SCOPE){
            String edgeName = source+"#"+label;
            Dbi<ByteBuffer> dbi = txn==null? index.openDbi(edgeName,DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT) : index.openDbi(txn,edgeName.getBytes(),null,DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT);
            return new LocalEdgeDataStore(new LocalMetadata(scope,source,label),dbi);
        }
        if(scope==Distributable.LOCAL_SCOPE){
            String edgeName = source+"#"+label;
            Dbi<ByteBuffer> dbi = txn==null? local.openDbi(edgeName,DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT) : local.openDbi(txn,edgeName.getBytes(),null,DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT);
            return new LocalEdgeDataStore(new LocalMetadata(scope,source,label),dbi);
        }
        if(scope==Distributable.LOG_SCOPE){
            String edgeName = source+"#"+label;
            Dbi<ByteBuffer> dbi = txn==null? log.openDbi(edgeName,DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT) : log.openDbi(txn,edgeName.getBytes(),null,DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT);
            return new LocalEdgeDataStore(new LocalMetadata(scope,source,label),dbi);
        }
        throw new RuntimeException("Scope ["+scope+"] not supported");
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
        log = Env.create().setMapSize(storeSize).setMaxDbs(maxDatabaseNumber).setMaxReaders(maxReaders).open(this.path(logPath).toFile());
        for(int i=0;i<PENDING_BUFFER_SIZE;i++){
            pendingQueue.offer(new BufferCache(KEY_SIZE,VALUE_SIZE,pendingQueue));
        }
        data.getDbiNames().forEach(n->{
            String dname = new String(n);
            if(!dname.contains("#")){
                logger.warn("Data DB : "+dname);
                createDataStore(dname);
            }
        });
        integration.getDbiNames().forEach(n->{
            String dname = new String(n);
            if(!dname.contains("#")){
                logger.warn("Integration DB : "+dname);
                createAccessIndexDataStore(dname);
            }
        });
        index.getDbiNames().forEach(n->{
            String dname = new String(n);
            if(!dname.contains("#")){
                logger.warn("Index DB : "+dname);
                createKeyIndexDataStore(dname);
            }

        });
        local.getDbiNames().forEach(n->{
            String dname = new String(n);
            if(!dname.contains("#")){
                logger.warn("Local DB : "+dname);
                createLocalDataStore(dname);
            }
        });
        log.getDbiNames().forEach(n->{
            String dname = new String(n);
            if(!dname.contains("#")){
                logger.warn("Log DB : "+dname);
                createLogDataStore(dname);
            }
        });
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
        logger.warn("Setup LMDB provider map store listeners");
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
        logger.warn("Waiting data for LMDB provider map store listeners");
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

    public void onDistributing(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value,long transactionId){
        if(metadata.scope()==Distributable.INTEGRATION_SCOPE && integrationMapStoreListener!=null){
            integrationMapStoreListener.onDistributing(metadata,key,value,transactionId);
            return;
        }
        if(metadata.scope()==Distributable.DATA_SCOPE && dataMapStoreListener!=null){
            dataMapStoreListener.onDistributing(metadata,key,value,transactionId);
            return;
        }
        if(metadata.scope()==Distributable.INDEX_SCOPE && keyIndexMapStoreListener!=null){
            keyIndexMapStoreListener.onDistributing(metadata,key,value,transactionId);
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
            return keyIndexMapStoreListener.onRecovering(metadata,key,bufferStream);
        }
        return false;
    }


    @Override
    public boolean onDeleting(Metadata metadata,Recoverable.DataBuffer key, Recoverable.DataBuffer value,long transactionId) {
        if(metadata.scope()==Distributable.INTEGRATION_SCOPE && integrationMapStoreListener!=null){
            return integrationMapStoreListener.onDeleting(metadata,key,value,transactionId);
        }
        if(metadata.scope()==Distributable.DATA_SCOPE && dataMapStoreListener!=null){
             return dataMapStoreListener.onDeleting(metadata,key,value,transactionId);

        }
        if(metadata.scope()==Distributable.INDEX_SCOPE && keyIndexMapStoreListener!=null){
            return keyIndexMapStoreListener.onDeleting(metadata,key,value,transactionId);
        }
        return true;
    }

    @Override
    public void onCommit(int scope,long transactionId) {
        if(scope==Distributable.DATA_SCOPE && this.dataMapStoreListener!=null){
            this.dataMapStoreListener.onCommit(scope,transactionId);
            return;
        }
        if(scope==Distributable.INTEGRATION_SCOPE && this.integrationMapStoreListener!=null){
            this.integrationMapStoreListener.onCommit(scope,transactionId);
            return;
        }
        if(scope==Distributable.INDEX_SCOPE && this.keyIndexMapStoreListener!=null){
            this.keyIndexMapStoreListener.onCommit(scope,transactionId);
        }
    }

    @Override
    public void onAbort(int scope,long transactionId) {
        if(scope==Distributable.DATA_SCOPE && this.dataMapStoreListener!=null){
            this.dataMapStoreListener.onAbort(scope,transactionId);
            return;
        }
        if(scope==Distributable.INTEGRATION_SCOPE && this.integrationMapStoreListener!=null){
            this.integrationMapStoreListener.onAbort(scope,transactionId);
            return;
        }
        if(scope==Distributable.INDEX_SCOPE && this.keyIndexMapStoreListener!=null){
            this.keyIndexMapStoreListener.onAbort(scope,transactionId);
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
