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

    private final LMDBEnv dataEnv = LMDBEnv.DATA_ENV;
    private final LMDBEnv integrationEnv = LMDBEnv.INTEGRATION_ENV;
    private final LMDBEnv indexEnv = LMDBEnv.INDEX_ENV;
    private final LMDBEnv logEnv = LMDBEnv.LOG_ENV;
    private final LMDBEnv localEnv = LMDBEnv.LOCAL_ENV;

    public static final long storeBaseMbSize = 1_048_576L; //1MB

    long storeSize = storeBaseMbSize; // 1MB = 1,048,576 (1024*1024)

    int maxDatabaseNumber = 1024;
    int maxReaders = 100;

    private final static int KEY_SIZE = 200;
    private final static int VALUE_SIZE = 2000;

    private final static int PENDING_BUFFER_SIZE = 32;

    boolean envNoSyncFlag = true;
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
        this.storeSize = storeBaseMbSize*(int)properties.get("storeSizeMb");
        this.envNoSyncFlag = (boolean)properties.get("envNoSyncFlag");
        dataEnv.envSetting = (EnvSetting) properties.get(EnvSetting.data);
        integrationEnv.envSetting = (EnvSetting) properties.get(EnvSetting.integration);
        indexEnv.envSetting = (EnvSetting) properties.get(EnvSetting.index);
        logEnv.envSetting = (EnvSetting) properties.get(EnvSetting.log);
        localEnv.envSetting = (EnvSetting) properties.get(EnvSetting.local);
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
        if(scope==Distributable.DATA_SCOPE) return dataEnv.txnWrite();
        if(scope==Distributable.INTEGRATION_SCOPE) return integrationEnv.txnWrite();
        if(scope==Distributable.INDEX_SCOPE) return indexEnv.txnWrite();
        if(scope==Distributable.LOCAL_SCOPE) return localEnv.txnWrite();
        if(scope==Distributable.LOG_SCOPE) return logEnv.txnWrite();
        throw new RuntimeException("Scope ["+scope+"] not supported");
    }

    public DataStore createDataStore(int scope,String name,Txn<ByteBuffer> txn,long transactionId){
        if(scope==Distributable.DATA_SCOPE){
            return dataEnv.createDataStore(scope,name,txn,transactionId);
        }
        if(scope==Distributable.INTEGRATION_SCOPE){
           return integrationEnv.createDataStore(scope,name,txn,transactionId);
        }
        if(scope==Distributable.INDEX_SCOPE){
            return indexEnv.createDataStore(scope,name,txn,transactionId);
        }
        if(scope==Distributable.LOCAL_SCOPE){
           return localEnv.createDataStore(scope,name,txn,transactionId);
        }
        if(scope==Distributable.LOG_SCOPE){
            return logEnv.createDataStore(scope,name,txn,transactionId);
        }
        throw new RuntimeException("Scope ["+scope+"] not supported");
    }
    public LocalEdgeDataStore localEdgeDataStore(int scope,String source,String label,Txn<ByteBuffer> txn){
        if(scope==Distributable.DATA_SCOPE){
            return dataEnv.localEdgeDataStore(scope,source,label,txn);
        }
        if(scope==Distributable.INTEGRATION_SCOPE){
            return integrationEnv.localEdgeDataStore(scope,source,label,txn);
        }
        if(scope==Distributable.INDEX_SCOPE){
            return indexEnv.localEdgeDataStore(scope,source,label,txn);
        }
        if(scope==Distributable.LOCAL_SCOPE){
            return localEnv.localEdgeDataStore(scope,source,label,txn);
        }
        if(scope==Distributable.LOG_SCOPE){
            return logEnv.localEdgeDataStore(scope,source,label,txn);
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
        dataEnv.lmdbDataStoreProvider = this;
        integrationEnv.lmdbDataStoreProvider = this;
        indexEnv.lmdbDataStoreProvider = this;
        logEnv.lmdbDataStoreProvider = this;
        localEnv.lmdbDataStoreProvider = this;
        dataEnv.start();
        integrationEnv.start();
        indexEnv.start();
        logEnv.start();
        localEnv.start();
        for(int i=0;i<PENDING_BUFFER_SIZE;i++){
            pendingQueue.offer(new BufferCache(KEY_SIZE,VALUE_SIZE,pendingQueue));
        }
        dataEnv.getDbiNames().forEach(n->{
            String dname = new String(n);
            //logger.warn("DATA : "+dname);
            if(!dname.contains("#")){
                createDataStore(dname);
            }
        });
        integrationEnv.getDbiNames().forEach(n->{
            String dname = new String(n);
            //logger.warn("ACCESS : "+dname);
            if(!dname.contains("#")){
                createAccessIndexDataStore(dname);
            }
        });
        indexEnv.getDbiNames().forEach(n->{
            String dname = new String(n);
            //logger.warn("INDEX : "+dname);
            if(!dname.contains("#")){
                createKeyIndexDataStore(dname);
            }

        });
        localEnv.getDbiNames().forEach(n->{
            String dname = new String(n);
            //logger.warn("LOCAL : "+dname);
            if(!dname.contains("#")){
                createLocalDataStore(dname);
            }
        });
        logEnv.getDbiNames().forEach(n->{
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
            jsonObject.addProperty(EnvSetting.data,0);
            jsonObject.addProperty(EnvSetting.integration,0);
            jsonObject.addProperty(EnvSetting.index,0);
            jsonObject.addProperty(EnvSetting.log,0);
            jsonObject.addProperty(EnvSetting.local,0);
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
        dataEnv.shutdown();
        integrationEnv.shutdown();
        indexEnv.shutdown();
        logEnv.shutdown();
        localEnv.shutdown();
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
                    ix = jsonObject.get(EnvSetting.data).getAsInt()+1;
                    jsonObject.addProperty(EnvSetting.data,ix);
                    break;
                case Distributable.INTEGRATION_SCOPE:
                    ix = jsonObject.get(EnvSetting.integration).getAsInt()+1;
                    jsonObject.addProperty(EnvSetting.integration,ix);
                    break;
                case Distributable.INDEX_SCOPE:
                    ix = jsonObject.get(EnvSetting.index).getAsInt()+1;
                    jsonObject.addProperty(EnvSetting.index,ix);
                    break;
                case Distributable.LOG_SCOPE:
                    ix = jsonObject.get(EnvSetting.log).getAsInt()+1;
                    jsonObject.addProperty(EnvSetting.log,ix);
                    break;
                case Distributable.LOCAL_SCOPE:
                default:
                    ix = jsonObject.get(EnvSetting.local).getAsInt()+1;
                    jsonObject.addProperty(EnvSetting.local,ix);
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
                dataEnv.copy(copyPath.toFile());
                saveJsonCopyDate(copyPath.toFile());
                return copyPath.toFile();
            }
            if(scope==Distributable.INTEGRATION_SCOPE){
                Path copyPath = path(baseDir+"/integration_"+sequence);
                integrationEnv.copy(copyPath.toFile());
                saveJsonCopyDate(copyPath.toFile());
                return copyPath.toFile();
            }
            if(scope==Distributable.INDEX_SCOPE){
                Path copyPath = path(baseDir+"/index_"+sequence);
                indexEnv.copy(copyPath.toFile());
                saveJsonCopyDate(copyPath.toFile());
                return copyPath.toFile();
            }
            if(scope==Distributable.LOG_SCOPE){
                Path copyPath = path(baseDir+"/log_"+sequence);
                logEnv.copy(copyPath.toFile());
                saveJsonCopyDate(copyPath.toFile());
                return copyPath.toFile();
            }
            if(scope==Distributable.LOCAL_SCOPE){
                Path copyPath = path(baseDir+"/local_"+sequence);
                localEnv.copy(copyPath.toFile());
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
