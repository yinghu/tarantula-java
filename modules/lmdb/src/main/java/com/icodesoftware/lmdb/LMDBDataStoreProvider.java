package com.icodesoftware.lmdb;

import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.*;

import com.icodesoftware.util.JsonUtil;
import org.lmdbjava.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;


public class LMDBDataStoreProvider implements DataStoreProvider,MapStoreListener{

    private TarantulaLogger logger = JDKLogger.getLogger(LMDBDataStoreProvider.class);

    private String name;

    private String baseDir = "target/lmdb";

    private EnvSetting dataSetting = EnvSetting.DataSetting;
    private EnvSetting integrationSetting = EnvSetting.IntegrationSetting;
    private EnvSetting indexSetting = EnvSetting.IndexSetting;
    private EnvSetting logSetting = EnvSetting.LogSetting;
    private EnvSetting localSetting = EnvSetting.LocalSetting;

    public Env<ByteBuffer> data;
    private Env<ByteBuffer> integration;
    private Env<ByteBuffer> index;
    private Env<ByteBuffer> local;
    private Env<ByteBuffer> log;
    private long storeSize = 10_048_576L; // 1MB = 1,048,576 (1024*1024)
    private int maxDatabaseNumber = 1024;
    private int maxReaders = 100;

    private final static int KEY_SIZE = 200;
    private final static int VALUE_SIZE = 2000;

    private final static int PENDING_BUFFER_SIZE = 32;

    private boolean envNoSyncFlag = true;
    private final static ConcurrentHashMap<String,DataStore> storeMap = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<String,LocalEdgeDataStore> edgMap = new ConcurrentHashMap<>();

    final static ArrayBlockingQueue<BufferCache> pendingQueue = new ArrayBlockingQueue<>(PENDING_BUFFER_SIZE);;


    private MapStoreListener integrationMapStoreListener;
    private MapStoreListener keyIndexMapStoreListener;
    private MapStoreListener dataMapStoreListener;
    private DistributionIdGenerator distributionIdGenerator;

    MetricsListener metricsListener = (k,v)->{};
    private JsonObject jsonObject;
    private LocalDataMigration migration;
    @Override
    public void configure(Map<String, Object> properties) {
        this.name = (String)properties.get("name");
        this.storeSize = storeSize*(int)properties.get("storeSizeMb");
        this.envNoSyncFlag = (boolean)properties.get("envNoSyncFlag");
        dataSetting = (EnvSetting) properties.get(EnvSetting.data);
        integrationSetting = (EnvSetting) properties.get(EnvSetting.integration);
        indexSetting = (EnvSetting) properties.get(EnvSetting.index);
        logSetting = (EnvSetting) properties.get(EnvSetting.log);
        localSetting = (EnvSetting) properties.get(EnvSetting.local);
        this.baseDir = (String)properties.get("dir");
        this.migration = new LocalDataMigration((JsonObject)properties.get("migration"));
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
        return storeMap.computeIfAbsent(name,k->createDataStore(Distributable.INTEGRATION_SCOPE,name,null,0));
    }

    public LocalEdgeDataStore createEdgeDB(int scope,String source,String label){
        final String edgeName = source+"#"+label;
        return edgMap.computeIfAbsent(edgeName,k->localEdgeDataStore(scope,source,label,null));
    }

    @Override
    public DataStore createKeyIndexDataStore(String name) {
        return storeMap.computeIfAbsent(name,k-> createDataStore(Distributable.INDEX_SCOPE,name,null,0));
    }
    @Override
    public DataStore createDataStore(String name){
        return storeMap.computeIfAbsent(name,k->createDataStore(Distributable.DATA_SCOPE, name, null,0));
    }
    public DataStore createLocalDataStore(String name){
        return storeMap.computeIfAbsent(name,k->createDataStore(Distributable.LOCAL_SCOPE,name,null,0));
    }
    public DataStore createLogDataStore(String name){
        return storeMap.computeIfAbsent(name,k->createDataStore(Distributable.LOG_SCOPE,name,null,0));
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

    public DataStore createDataStore(int scope,String name,Txn<ByteBuffer> txn,long transactionId){
        if(scope==Distributable.DATA_SCOPE){
            Dbi<ByteBuffer> dbi = txn==null? data.openDbi(name,DbiFlags.MDB_CREATE) : data.openDbi(txn,name.getBytes(),null,false,DbiFlags.MDB_CREATE);
            return txn==null? new CachedLMDBDataStore(scope,name,dbi,data,this) : new LMDBDataStore(scope,name,dbi,data,this,txn,transactionId);
        }
        if(scope==Distributable.INTEGRATION_SCOPE){
            Dbi<ByteBuffer> dbi = txn==null? integration.openDbi(name,DbiFlags.MDB_CREATE) : integration.openDbi(txn,name.getBytes(),null,false,DbiFlags.MDB_CREATE);
            return txn==null? new CachedLMDBDataStore(scope,name,dbi,integration,this) : new LMDBDataStore(scope,name,dbi,integration,this,txn,transactionId);
        }
        if(scope==Distributable.INDEX_SCOPE){
            Dbi<ByteBuffer> dbi = txn==null? index.openDbi(name,DbiFlags.MDB_CREATE) : index.openDbi(txn,name.getBytes(),null,false,DbiFlags.MDB_CREATE);
            return txn==null? new CachedLMDBDataStore(scope,name,dbi,index,this) : new LMDBDataStore(scope,name,dbi,index,this,txn,transactionId);
        }
        if(scope==Distributable.LOCAL_SCOPE){
            Dbi<ByteBuffer> dbi = txn==null? local.openDbi(name,DbiFlags.MDB_CREATE) : local.openDbi(txn,name.getBytes(),null,false,DbiFlags.MDB_CREATE);
            return txn==null? new CachedLMDBDataStore(scope,name,dbi,local,this) : new LMDBDataStore(scope,name,dbi,local,this,txn,transactionId);
        }
        if(scope==Distributable.LOG_SCOPE){
            Dbi<ByteBuffer> dbi = txn==null? log.openDbi(name,DbiFlags.MDB_CREATE) : log.openDbi(txn,name.getBytes(),null,false,DbiFlags.MDB_CREATE);
            return txn==null? new CachedLMDBDataStore(scope,name,dbi,log,this) : new LMDBDataStore(scope,name,dbi,log,this,txn,transactionId);
        }
        throw new RuntimeException("Scope ["+scope+"] not supported");
    }
    public LocalEdgeDataStore localEdgeDataStore(int scope,String source,String label,Txn<ByteBuffer> txn){
        if(scope==Distributable.DATA_SCOPE){
            String edgeName = source+"#"+label;
            Dbi<ByteBuffer> dbi = txn==null? data.openDbi(edgeName,DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT) : data.openDbi(txn,edgeName.getBytes(),null,false,DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT);
            return new LocalEdgeDataStore(new LocalMetadata(scope,source,label),dbi);
        }
        if(scope==Distributable.INTEGRATION_SCOPE){
            String edgeName = source+"#"+label;
            Dbi<ByteBuffer> dbi = txn==null? integration.openDbi(edgeName,DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT) : integration.openDbi(txn,edgeName.getBytes(),null,false,DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT);
            return new LocalEdgeDataStore(new LocalMetadata(scope,source,label),dbi);
        }
        if(scope==Distributable.INDEX_SCOPE){
            String edgeName = source+"#"+label;
            Dbi<ByteBuffer> dbi = txn==null? index.openDbi(edgeName,DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT) : index.openDbi(txn,edgeName.getBytes(),null,false,DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT);
            return new LocalEdgeDataStore(new LocalMetadata(scope,source,label),dbi);
        }
        if(scope==Distributable.LOCAL_SCOPE){
            String edgeName = source+"#"+label;
            Dbi<ByteBuffer> dbi = txn==null? local.openDbi(edgeName,DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT) : local.openDbi(txn,edgeName.getBytes(),null,false,DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT);
            return new LocalEdgeDataStore(new LocalMetadata(scope,source,label),dbi);
        }
        if(scope==Distributable.LOG_SCOPE){
            String edgeName = source+"#"+label;
            Dbi<ByteBuffer> dbi = txn==null? log.openDbi(edgeName,DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT) : log.openDbi(txn,edgeName.getBytes(),null,false,DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT);
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
        if(envNoSyncFlag){
            EnvFlags[] flags = new EnvFlags[]{EnvFlags.MDB_NOSYNC};
            data = Env.create().setMapSize(storeSize).setMaxDbs(maxDatabaseNumber).setMaxReaders(maxReaders).open(path(this.dataSetting.storePath).toFile(),flags);
            integration = Env.create().setMapSize(storeSize).setMaxDbs(maxDatabaseNumber).setMaxReaders(maxReaders).open(path(this.integrationSetting.storePath).toFile(),flags);
            index = Env.create().setMapSize(storeSize).setMaxDbs(maxDatabaseNumber).setMaxReaders(maxReaders).open(path(this.indexSetting.storePath).toFile(),flags);
            local = Env.create().setMapSize(storeSize).setMaxDbs(maxDatabaseNumber).setMaxReaders(maxReaders).open(this.path(localSetting.storePath).toFile(),flags);
            log = Env.create().setMapSize(storeSize).setMaxDbs(maxDatabaseNumber).setMaxReaders(maxReaders).open(this.path(logSetting.storePath).toFile(),flags);
        }
        else{
            data = Env.create().setMapSize(storeSize).setMaxDbs(maxDatabaseNumber).setMaxReaders(maxReaders).open(path(this.dataSetting.storePath).toFile());
            integration = Env.create().setMapSize(storeSize).setMaxDbs(maxDatabaseNumber).setMaxReaders(maxReaders).open(path(this.integrationSetting.storePath).toFile());
            index = Env.create().setMapSize(storeSize).setMaxDbs(maxDatabaseNumber).setMaxReaders(maxReaders).open(path(this.indexSetting.storePath).toFile());
            local = Env.create().setMapSize(storeSize).setMaxDbs(maxDatabaseNumber).setMaxReaders(maxReaders).open(this.path(localSetting.storePath).toFile());
            log = Env.create().setMapSize(storeSize).setMaxDbs(maxDatabaseNumber).setMaxReaders(maxReaders).open(this.path(logSetting.storePath).toFile());
        }
        for(int i=0;i<PENDING_BUFFER_SIZE;i++){
            pendingQueue.offer(new BufferCache(KEY_SIZE,VALUE_SIZE,pendingQueue));
        }
        data.getDbiNames().forEach(n->{
            String dname = new String(n);
            //logger.warn("DATA : "+dname);
            if(!dname.contains("#")){
                createDataStore(dname);
            }
        });
        integration.getDbiNames().forEach(n->{
            String dname = new String(n);
            //logger.warn("ACCESS : "+dname);
            if(!dname.contains("#")){
                createAccessIndexDataStore(dname);
            }
        });
        index.getDbiNames().forEach(n->{
            String dname = new String(n);
            //logger.warn("INDEX : "+dname);
            if(!dname.contains("#")){
                createKeyIndexDataStore(dname);
            }

        });
        local.getDbiNames().forEach(n->{
            String dname = new String(n);
            //logger.warn("LOCAL : "+dname);
            if(!dname.contains("#")){
                createLocalDataStore(dname);
            }
        });
        log.getDbiNames().forEach(n->{
            String dname = new String(n);
            //logger.warn("LOG : "+dname);
            if(!dname.contains("#")){
                createLogDataStore(dname);
            }
        });
        File backupLog = new File(baseDir+"/backup.json");
        if(!backupLog.exists()){
            backupLog.createNewFile();
            jsonObject = new JsonObject();
            jsonObject.addProperty("data",0);
            jsonObject.addProperty("integration",0);
            jsonObject.addProperty("index",0);
            jsonObject.addProperty("log",0);
            jsonObject.addProperty("local",0);
            saveJson();
        }
        FileInputStream in = new FileInputStream(backupLog);
        jsonObject = JsonUtil.parse(in);
        if(migration!=null && migration.migrating()){
            migration.migrate(this,storeMap);
            System.exit(0);
        }
        logger.warn("LMDB Provider started with store size ["+storeSize+"] queue side ["+pendingQueue.size()+"] store no sync mode ["+envNoSyncFlag+"]");
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
    private void closeMapStore() {
        try{
            if(keyIndexMapStoreListener!=null){
                keyIndexMapStoreListener.shutdown();
            }
            if(integrationMapStoreListener!=null){
                integrationMapStoreListener.shutdown();
            }
            if(dataMapStoreListener!=null){
                dataMapStoreListener.shutdown();
            }
        }catch (Exception ex){
            logger.error("Failed to close map stores",ex);
        }
    }

    @Override
    public void shutdown() throws Exception {
        closeMapStore();
        storeMap.forEach((k,v)->v.close());
        storeMap.clear();
        edgMap.forEach((k,v)->v.dbi.close());
        edgMap.clear();
        data.sync(true);
        data.close();
        integration.sync(true);
        integration.close();
        index.sync(true);
        index.close();
        log.sync(true);
        log.close();
        local.sync(true);
        local.close();
        logger.warn("LMDB Shutting down with pending buffer size ["+pendingQueue.size()+"]");
        pendingQueue.clear();
    }

    public void onUpdating(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value,long transactionId){
        if(metadata.scope()==Distributable.INTEGRATION_SCOPE && integrationMapStoreListener!=null){
            integrationMapStoreListener.onUpdating(metadata,key,value,transactionId);
            return;
        }
        if(metadata.scope()==Distributable.DATA_SCOPE && dataMapStoreListener!=null){
            dataMapStoreListener.onUpdating(metadata,key,value,transactionId);
        }
    }


    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer buffer){
        if(metadata.scope()==Distributable.INTEGRATION_SCOPE && integrationMapStoreListener!=null){
            return integrationMapStoreListener.onRecovering(metadata,key,buffer);
        }
        if(metadata.scope()==Distributable.DATA_SCOPE && dataMapStoreListener!=null){
            return dataMapStoreListener.onRecovering(metadata,key,buffer);
        }
        return false;
    }

    public boolean onRecovering(Metadata metadata,Recoverable.DataBuffer key,DataStore.BufferStream bufferStream){
        if(metadata.scope()==Distributable.INTEGRATION_SCOPE && integrationMapStoreListener!=null){
            return integrationMapStoreListener.onRecovering(metadata,key,bufferStream);
        }
        if(metadata.scope()==Distributable.DATA_SCOPE && dataMapStoreListener!=null){
            return dataMapStoreListener.onRecovering(metadata,key,bufferStream);

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
        }
    }

    public void assign(Recoverable.DataBuffer dataBuffer){
        this.distributionIdGenerator.assign(dataBuffer);
    }

    public File backup(int scope){
        synchronized (jsonObject){
            int ix;
            switch (scope){
                case Distributable.DATA_SCOPE:
                    ix = jsonObject.get("data").getAsInt()+1;
                    jsonObject.addProperty("data",ix);
                    break;
                case Distributable.INTEGRATION_SCOPE:
                    ix = jsonObject.get("integration").getAsInt()+1;
                    jsonObject.addProperty("integration",ix);
                    break;
                case Distributable.INDEX_SCOPE:
                    ix = jsonObject.get("index").getAsInt()+1;
                    jsonObject.addProperty("index",ix);
                    break;
                case Distributable.LOG_SCOPE:
                    ix = jsonObject.get("log").getAsInt()+1;
                    jsonObject.addProperty("log",ix);
                    break;
                case Distributable.LOCAL_SCOPE:
                default:
                    ix = jsonObject.get("local").getAsInt()+1;
                    jsonObject.addProperty("local",ix);
                    break;
            }
            saveJson();
            return new File(backup(scope,ix),"data.mdb");
        }
    }
    private void saveJson(){
        try{
            File backupLog = new File(baseDir+"/backup.json");
            FileOutputStream fileOutputStream = new FileOutputStream(backupLog);
            fileOutputStream.write(jsonObject.toString().getBytes());
            fileOutputStream.close();
        }catch (Exception exception){
            logger.error("Failed to save json ["+jsonObject.toString()+"]",exception);
        }
    }
    private void saveJsonCopyDate(File dir){
        try{
            FileOutputStream fileOutputStream = new FileOutputStream(dir.getAbsolutePath()+"/version.json");
            JsonObject version = new JsonObject();
            version.addProperty("backupDate", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            fileOutputStream.write(version.toString().getBytes());
            fileOutputStream.close();
        }catch (Exception exception){
            logger.error("Failed to save json version ["+dir+"]",exception);
        }
    }
    private File backup(int scope,int sequence){
        try{
            if(scope==Distributable.DATA_SCOPE){
                Path copyPath = path(baseDir+"/data_"+sequence);
                data.sync(true);
                data.copy(copyPath.toFile());
                saveJsonCopyDate(copyPath.toFile());
                return copyPath.toFile();
            }
            if(scope==Distributable.INTEGRATION_SCOPE){
                Path copyPath = path(baseDir+"/integration_"+sequence);
                integration.sync(true);
                integration.copy(copyPath.toFile());
                saveJsonCopyDate(copyPath.toFile());
                return copyPath.toFile();
            }
            if(scope==Distributable.INDEX_SCOPE){
                Path copyPath = path(baseDir+"/index_"+sequence);
                index.sync(true);
                index.copy(copyPath.toFile());
                saveJsonCopyDate(copyPath.toFile());
                return copyPath.toFile();
            }
            if(scope==Distributable.LOG_SCOPE){
                Path copyPath = path(baseDir+"/log_"+sequence);
                log.sync(true);
                log.copy(copyPath.toFile());
                saveJsonCopyDate(copyPath.toFile());
                return copyPath.toFile();
            }
            if(scope==Distributable.LOCAL_SCOPE){
                Path copyPath = path(baseDir+"/local_"+sequence);
                local.sync(true);
                local.copy(copyPath.toFile());
                saveJsonCopyDate(copyPath.toFile());
                return copyPath.toFile();
            }
            throw new RuntimeException("Scope ["+scope+"] not supported");

        }catch (Exception ex){
            logger.error("Failed to backup data store ["+scope+"]",ex);
            throw new RuntimeException(ex);
        }
    }
    private Path path(String path) throws Exception{
        Path _path = Paths.get(path);
        if(!Files.exists(_path)) Files.createDirectories(_path);
        return _path;
    }

    public Recoverable.DataBufferPair dataBufferPair(){
        BufferCache cache = pendingQueue.poll();
        if(cache!=null) return cache;
        return new BufferCache(KEY_SIZE,VALUE_SIZE,pendingQueue);
    }

    @Override
    public void registerMetricsListener(MetricsListener metricsListener) {
        if(metricsListener==null) return;
        this.metricsListener = metricsListener;
    }

}
