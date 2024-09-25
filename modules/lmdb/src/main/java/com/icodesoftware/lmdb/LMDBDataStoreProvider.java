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

    private String name = EnvSetting.ENV_PROVIDER_NAME;

    private String baseDir = EnvSetting.ENV_BASE_DIR;

    private final LMDBEnv dataEnv = new LMDBEnv(EnvSetting.DataSetting);
    private final LMDBEnv integrationEnv = new LMDBEnv(EnvSetting.IntegrationSetting);
    private final LMDBEnv indexEnv = new LMDBEnv(EnvSetting.IndexSetting);
    private final LMDBEnv logEnv = new LMDBEnv(EnvSetting.LogSetting);
    private final LMDBEnv localEnv = new LMDBEnv(EnvSetting.LocalSetting);


    long storeSize = EnvSetting.toBytesFromMb(1); // 1MB = 1,048,576 (1024*1024)

    int maxDatabaseNumber = EnvSetting.MAX_STORE_NUMBER;
    int maxReaders = EnvSetting.MAX_READER_NUMBER;

    static int KEY_SIZE = EnvSetting.KEY_SIZE;
    private static int VALUE_SIZE = EnvSetting.VALUE_SIZE;

    private static int PENDING_BUFFER_SIZE = EnvSetting.MAX_PENDING_BUFFER_NUMBER;

    boolean envNoSyncFlag = EnvSetting.ENV_NO_SYNC_Flag;
    boolean storeReindexing;
    private final ConcurrentHashMap<String,DataStore> storeMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String,LocalEdgeDataStore> edgMap = new ConcurrentHashMap<>();

    final ArrayBlockingQueue<BufferCache> pendingQueue = new ArrayBlockingQueue<>(PENDING_BUFFER_SIZE);;


    private MapStoreListener integrationMapStoreListener;
    private MapStoreListener keyIndexMapStoreListener;
    private MapStoreListener dataMapStoreListener;
    private DistributionIdGenerator distributionIdGenerator;

    MetricsListener metricsListener = (k,v)->{};
    private JsonObject jsonObject;
    private LocalDataMigration migration;
    @Override
    public void configure(Map<String, Object> properties) {
        this.name = (String)properties.getOrDefault(EnvSetting.ENV_CONFIG_NAME,EnvSetting.ENV_PROVIDER_NAME);
        this.storeSize = EnvSetting.toBytesFromMb((int)properties.getOrDefault(EnvSetting.ENV_CONFIG_STORE_SIZE_MB,1));
        this.envNoSyncFlag = (boolean)properties.getOrDefault(EnvSetting.ENV_CONFIG_NO_SYNC_FLAG,EnvSetting.ENV_NO_SYNC_Flag);
        this.storeReindexing = (boolean)properties.getOrDefault(EnvSetting.ENV_CONFIG_STORE_REINDEXING,false);
        boolean externalKeyValueBufferUsed = (boolean)properties.getOrDefault(EnvSetting.ENV_CONFIG_EXTERNAL_KEY_VALUE_BUFFER_USED,false);
        if(externalKeyValueBufferUsed){
            logger.warn("External key size, value size and pending buffer size used");
            int keySizeUsed = (int)properties.getOrDefault(EnvSetting.ENV_CONFIG_STORE_KEY_SIZE,EnvSetting.KEY_SIZE);
            int valueSizeUsed = (int)properties.getOrDefault(EnvSetting.ENV_CONFIG_STORE_VALUE_SIZE,EnvSetting.VALUE_SIZE);
            int bufferSizeUsed = (int)properties.getOrDefault(EnvSetting.ENV_CONFIG_STORE_PENDING_BUFFER_SIZE,EnvSetting.MAX_PENDING_BUFFER_NUMBER);
            if(keySizeUsed>0 && keySizeUsed <= EnvSetting.MAX_LMDB_KEY_SIZE) KEY_SIZE = keySizeUsed;
            if(valueSizeUsed>0 && valueSizeUsed <= 2032 ) VALUE_SIZE = valueSizeUsed;
            if(bufferSizeUsed > EnvSetting.MAX_PENDING_BUFFER_NUMBER) PENDING_BUFFER_SIZE = bufferSizeUsed;
        }
        dataEnv.envSetting = (EnvSetting) properties.getOrDefault(EnvSetting.data,EnvSetting.DataSetting);
        integrationEnv.envSetting = (EnvSetting) properties.getOrDefault(EnvSetting.integration,EnvSetting.IntegrationSetting);
        indexEnv.envSetting = (EnvSetting) properties.getOrDefault(EnvSetting.index,EnvSetting.IndexSetting);
        logEnv.envSetting = (EnvSetting) properties.getOrDefault(EnvSetting.log,EnvSetting.LogSetting);
        localEnv.envSetting = (EnvSetting) properties.getOrDefault(EnvSetting.local,EnvSetting.LocalSetting);
        this.baseDir = (String)properties.getOrDefault(EnvSetting.ENV_CONFIG_BASE_DIR,EnvSetting.ENV_BASE_DIR);
        this.migration = new LocalDataMigration((JsonObject)properties.get(EnvSetting.ENV_CONFIG_MIGRATION));
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

    public LMDBEnv env(int scope){
        if(scope==Distributable.DATA_SCOPE) return dataEnv;
        if(scope==Distributable.INTEGRATION_SCOPE) return integrationEnv;
        if(scope==Distributable.INDEX_SCOPE) return indexEnv;
        if(scope==Distributable.LOCAL_SCOPE) return localEnv;
        if(scope==Distributable.LOG_SCOPE) return logEnv;
        throw new RuntimeException("Scope ["+scope+"] not supported");
    }

    public DataStore createDataStore(int scope,String name,Txn<ByteBuffer> txn,long transactionId){
        if(scope==Distributable.DATA_SCOPE){
            return dataEnv.createDataStore(name,txn,transactionId);
        }
        if(scope==Distributable.INTEGRATION_SCOPE){
           return integrationEnv.createDataStore(name,txn,transactionId);
        }
        if(scope==Distributable.INDEX_SCOPE){
            return indexEnv.createDataStore(name,txn,transactionId);
        }
        if(scope==Distributable.LOCAL_SCOPE){
           return localEnv.createDataStore(name,txn,transactionId);
        }
        if(scope==Distributable.LOG_SCOPE){
            return logEnv.createDataStore(name,txn,transactionId);
        }
        throw new RuntimeException("Scope ["+scope+"] not supported");
    }
    public LocalEdgeDataStore localEdgeDataStore(int scope,String source,String label,Txn<ByteBuffer> txn){
        if(scope==Distributable.DATA_SCOPE){
            return dataEnv.localEdgeDataStore(source,label,txn);
        }
        if(scope==Distributable.INTEGRATION_SCOPE){
            return integrationEnv.localEdgeDataStore(source,label,txn);
        }
        if(scope==Distributable.INDEX_SCOPE){
            return indexEnv.localEdgeDataStore(source,label,txn);
        }
        if(scope==Distributable.LOCAL_SCOPE){
            return localEnv.localEdgeDataStore(source,label,txn);
        }
        if(scope==Distributable.LOG_SCOPE){
            return logEnv.localEdgeDataStore(source,label,txn);
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
        if(storeReindexing){
            logger.warn("Starting store reindex from index store");
        }
        if(migration!=null && migration.migrating()){
            migration.migrate(this,storeMap);
            System.exit(0);
        }
        logger.warn("LMDB Provider ["+name+"] started with store size ["+storeSize+"] queue side ["+pendingQueue.size()+"] store no sync mode ["+envNoSyncFlag+"] reindexing ["+storeReindexing+"]");
        logger.warn("LMDB Provider using key/value size ["+KEY_SIZE+"/"+VALUE_SIZE+"]");
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
        edgMap.forEach((k,v)->v.close());
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
    public String baseDir(){
        return baseDir;
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
