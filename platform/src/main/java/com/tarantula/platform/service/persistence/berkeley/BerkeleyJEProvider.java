package com.tarantula.platform.service.persistence.berkeley;

import com.google.gson.JsonElement;
import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.util.TimeUtil;
import com.sleepycat.je.*;
import com.sleepycat.je.util.DbBackup;
import com.sleepycat.je.util.LogVerificationReadableByteChannel;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.service.DataStoreProvider;
import com.tarantula.platform.service.metrics.PerformanceMetrics;
import com.tarantula.platform.service.persistence.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

public class BerkeleyJEProvider implements DataStoreProvider,MapStoreListener{

    private static JDKLogger log = JDKLogger.getLogger(BerkeleyJEProvider.class);

    private String dataPath;
    private String integrationPath;
    private String indexPath;
    private String backupPath;
    private String integrationBackupPath;
    private String indexBackupPath;
    private String database;
    private boolean trimming;
    private int partitionNumber;


    private boolean dailyBackup;


    private ClusterNode node;

    //private ClusterProvider integrationCluster;
    private ConcurrentHashMap<String,ReplicatedDataStore> dMap = new ConcurrentHashMap<>();

    //private ConcurrentLinkedDeque<String> pendingReplicationDataQueue = new ConcurrentLinkedDeque<>();
    //private ConcurrentLinkedDeque<String> pendingReplicationIntegrationQueue = new ConcurrentLinkedDeque<>();

    //private ConcurrentLinkedDeque<String> pendingBackupDataQueue = new ConcurrentLinkedDeque<>();
    //private ConcurrentLinkedDeque<String> pendingBackupIntegrationQueue = new ConcurrentLinkedDeque<>();


    //private ConcurrentHashMap<String,OnReplication> pendingReplicationIndex = new ConcurrentHashMap<>();
    //private ConcurrentHashMap<String,OnReplication> pendingBackupIndex = new ConcurrentHashMap<>();

    private Environment environment;
    private Environment integrationEnvironment;
    private Environment indexEnvironment;
    private BackupRouter iBackupProvider;
    private BackupRouter dBackupProvider;
    private List<String> dataStoreList;

    private DataStoreOperationSummary operationSummary = new DataStoreOperationSummary();

    private MetricsListener metricsListener = (k,v)->{};

    private ServiceContext serviceContext;
    private DiskSynchronizer diskSynchronizer;
    private CacheSynchronizer cacheSynchronizer;

    private int updateThreshold;
    private int cacheUpdateThreshold;

    private int replicationBatchSize;
    private int backupBatchSize;
    private int maxTimerLoop;
    private int maxBytesPerBatch;
    private int timerCount;
    private long nextReplicationInterval;
    private long nextBackupInterval;

    private int maxReplicationNumber = 3;

    private MapStoreListener integrationScopeReplicationProxy;
    private MapStoreListener dataScopeReplicationProxy;

    private MapStoreListener indexScopeReplicationProxy;
    @Override
    public void configure(Map<String, Object> properties) {
        this.database = (String)properties.get("name");
        this.trimming =((JsonElement) properties.get("truncated")).getAsBoolean();
        String _dataPath = ((JsonElement)properties.get("dataPath")).getAsString();
        String _integrationPath = ((JsonElement)properties.get("integrationPath")).getAsString();
        String _indexPath = ((JsonElement)properties.get("indexPath")).getAsString();
        String _backupPath = ((JsonElement)properties.get("backupPath")).getAsString();
        this.replicationBatchSize = ((JsonElement)properties.get("replicationBatchSize")).getAsInt();
        this.maxTimerLoop = ((JsonElement)properties.get("maxTimerLoop")).getAsInt();
        this.maxBytesPerBatch = ((JsonElement)properties.get("maxBytesPerBatch")).getAsInt();
        this.backupBatchSize = ((JsonElement)properties.get("backupBatchSize")).getAsInt();
        this.timerCount = ((JsonElement)properties.get("timerCount")).getAsInt();
        long nextSyncInterval = 1000*((JsonElement)properties.get("diskSyncIntervalSeconds")).getAsInt();
        this.nextReplicationInterval = 1000*((JsonElement)properties.get("replicationSyncIntervalSeconds")).getAsInt();
        this.nextBackupInterval = 1000*((JsonElement)properties.get("backupSyncIntervalSeconds")).getAsInt();
        long nextEvictInterval = 1000*60*((JsonElement)properties.get("cacheSyncIntervalMinutes")).getAsInt();
        this.updateThreshold =  ((JsonElement)properties.get("syncUpdateThreshold")).getAsInt();
        this.cacheUpdateThreshold =  ((JsonElement)properties.get("cacheUpdateThreshold")).getAsInt();
        this.dataPath = properties.get("dir")+ FileSystems.getDefault().getSeparator()+_dataPath;
        this.integrationPath =properties.get("dir")+ FileSystems.getDefault().getSeparator()+_integrationPath;
        this.indexPath = properties.get("dir")+ FileSystems.getDefault().getSeparator()+_indexPath;
        this.backupPath = properties.get("dir")+ FileSystems.getDefault().getSeparator()+_backupPath+FileSystems.getDefault().getSeparator()+_dataPath;
        this.integrationBackupPath = properties.get("dir")+ FileSystems.getDefault().getSeparator()+_backupPath+FileSystems.getDefault().getSeparator()+_integrationPath;
        this.indexBackupPath = properties.get("dir")+ FileSystems.getDefault().getSeparator()+_backupPath+FileSystems.getDefault().getSeparator()+_indexPath;
        this.dailyBackup = (Boolean)properties.get("dailyBackup");
        this.partitionNumber = (Integer)properties.get("partitionNumber");
        this.maxReplicationNumber = (Integer)properties.get("maxReplicationNumber");
        this.node = (ClusterNode) properties.get("node");
        ServiceContext serviceContext = (ServiceContext) properties.get("serviceContext");
        this.iBackupProvider = new BackupRouter("integration",Distributable.INTEGRATION_SCOPE);
        this.iBackupProvider.configure((Map<String, Object>)properties.get("integrationRouter"));
        this.iBackupProvider.setup(serviceContext);
        this.dBackupProvider = new BackupRouter("data",Distributable.DATA_SCOPE);
        this.dBackupProvider.configure((Map<String, Object>)properties.get("dataRouter"));
        this.dBackupProvider.setup(serviceContext);
        this.diskSynchronizer = new DiskSynchronizer(this,nextSyncInterval);
        this.cacheSynchronizer = new CacheSynchronizer(this,nextEvictInterval);
    }

    public void registerMapStoreListener(int scope, MapStoreListener mapStoreListener){
        if(scope==Distributable.DATA_SCOPE){
            this.dataScopeReplicationProxy = mapStoreListener;
            return;
        }
        if(scope==Distributable.INTEGRATION_SCOPE){
            this.integrationScopeReplicationProxy = mapStoreListener;
            return;
        }
        if(scope==Distributable.INDEX_SCOPE){
            this.indexScopeReplicationProxy = mapStoreListener;
        }

    }

    public MapStoreListener mapStoreListener(int scope){
        if(scope==Distributable.DATA_SCOPE){
            return this.dataScopeReplicationProxy;
        }
        if(scope==Distributable.INTEGRATION_SCOPE){
            return this.integrationScopeReplicationProxy;
        }
        if(scope==Distributable.INDEX_SCOPE){
            return this.indexScopeReplicationProxy;
        }
        return null;
    }

    @Override
    public DataStore createAccessIndexDataStore(String name) {
        return this.dMap.computeIfAbsent(name,(k)->{
            Database db = this.createDatabase(name,Distributable.INTEGRATION_SCOPE);
            this.iBackupProvider.registerDataStore(Distributable.INTEGRATION_SCOPE,name);
            return  new AccessIndexDataStore(this.node,db,this);
        });
    }
    @Override
    public DataStore createKeyIndexDataStore(String name) {
        return this.dMap.computeIfAbsent(name,(k)->{
            Database db = this.createDatabase(name,Distributable.LOCAL_SCOPE);
            return  new KeyIndexDataStore(this.node,db,this);
        });
    }
    private Database createDatabase(String name,int scope){
        try{
            //log.warn("Create database ["+name+"] on scope ["+scope+"]");
            DatabaseConfig dbConfig = new DatabaseConfig();
            dbConfig.setAllowCreate(true);
            dbConfig.setDeferredWrite(true);
            if(scope==Distributable.DATA_SCOPE){
                return this.environment.openDatabase(null,name,dbConfig);
            }
            else if(scope==Distributable.INTEGRATION_SCOPE){
                return this.integrationEnvironment.openDatabase(null,name,dbConfig);
            }
            return this.indexEnvironment.openDatabase(null,name,dbConfig);
        }catch (Exception ex){
            throw new RuntimeException(name,ex);
        }
    }
    @Override
    public DataStore create(String name,int partition){
        return this.dMap.computeIfAbsent(name,(k)->{
            dataStoreList.add(name);
            Database[] shards = new Database[partition];
            for(int i=0;i<partition;i++){
                shards[i]=createDatabase(name+"_"+i,Distributable.DATA_SCOPE);
            }
            this.dBackupProvider.registerDataStore(Distributable.DATA_SCOPE,name);
            return new PartitionDataStore(partition,this.node.bucketName,this.node.nodeName,name,shards,this);
        });
    }

    public List<String> list(){
       ArrayList<String>  alist = new ArrayList<>();
       alist.addAll(dataStoreList);
       return alist;
    }
    public DataStore lookup(String name){
        return dMap.get(name);
    }
    @Override
    public String name() {
        return this.database;//provider name and default data store name
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        //this.integrationCluster = serviceContext.clusterProvider();
        this.serviceContext.schedule(this.diskSynchronizer);
        this.serviceContext.schedule(this.cacheSynchronizer);
        //for(int i=0;i<timerCount;i++){
            //this.serviceContext.schedule(new ReplicationSynchronizer(this,nextReplicationInterval,Distributable.DATA_SCOPE));
            //this.serviceContext.schedule(new ReplicationSynchronizer(this,nextReplicationInterval+10,Distributable.INTEGRATION_SCOPE));
            //this.serviceContext.schedule(new BackupSynchronizer(this,nextBackupInterval,Distributable.DATA_SCOPE));
            //this.serviceContext.schedule(new BackupSynchronizer(this,nextBackupInterval+10,Distributable.INTEGRATION_SCOPE));
        //}
        this.integrationScopeReplicationProxy = new IntegrationScopeReplicationProxy(this);
        this.integrationScopeReplicationProxy.setup(serviceContext);
        this.dataScopeReplicationProxy = new DataScopeReplicationProxy(this);
        this.dataScopeReplicationProxy.setup(serviceContext);
        this.indexScopeReplicationProxy = new IndexScopeReplicationProxy(this);
        this.indexScopeReplicationProxy.setup(serviceContext);
    }
    @Override
    public void waitForData() {
        this.indexScopeReplicationProxy.waitForData();
        this.integrationScopeReplicationProxy.waitForData();
        this.dataScopeReplicationProxy.waitForData();
    }
    @Override
    public void start() throws Exception {
        Path _path = Paths.get(this.dataPath);
        if(!Files.exists(_path)){
            Files.createDirectories(_path);
        }
        Path _ipath = Paths.get(this.integrationPath);
        if(!Files.exists(_ipath)){
            Files.createDirectories(_ipath);
        }
        Path _xpath = Paths.get(this.indexPath);
        if(!Files.exists(_xpath)){
            Files.createDirectories(_xpath);
        }
        Path _pback = Paths.get(this.backupPath);
        if(!Files.exists(_pback)){
            Files.createDirectories(_pback);
        }
        Path _piback = Paths.get(this.integrationBackupPath);
        if(!Files.exists(_piback)){
            Files.createDirectories(_piback);
        }
        Path _pxback = Paths.get(this.indexBackupPath);
        if(!Files.exists(_pxback)){
            Files.createDirectories(_pxback);
        }
        File f = new File(dataPath+FileSystems.getDefault().getSeparator()+"last.dat");
        if(!f.exists()){
            DataStoreUtil.createLastDataFile(f);
        }
        File fi = new File(integrationPath+FileSystems.getDefault().getSeparator()+"last.dat");
        if(!fi.exists()){
            DataStoreUtil.createLastDataFile(fi);
        }
        File fix = new File(indexPath+FileSystems.getDefault().getSeparator()+"last.dat");
        if(!fix.exists()){
            DataStoreUtil.createLastDataFile(fix);
        }
        this.dataStoreList = new CopyOnWriteArrayList<>();
        Properties props = new Properties();
        props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("je.properties"));
        EnvironmentConfig envConfig = new EnvironmentConfig(props);
        envConfig.setAllowCreate(true);
        this.environment = new Environment(new File(dataPath),envConfig);
        if(trimming){
            log.warn("Database ["+this.database+"] configured as trimming mode");
            for(int i=0;i<this.partitionNumber;i++){
                long ret = this.environment.truncateDatabase(null,this.database+"-"+i,true);
                log.warn("Records ["+ret+"] truncated from ["+this.database+"-"+i+"]");
            }
        }
        this.integrationEnvironment = new Environment(new File(integrationPath),envConfig);
        this.indexEnvironment = new Environment(new File(indexPath),envConfig);
        //log.info("Waiting for loading data on first member from data scope store");
        HashSet<String> ln = new HashSet<>();
        for(String dn : this.environment.getDatabaseNames()){
            int ix = dn.lastIndexOf("_");
            ln.add(dn.substring(0,ix));
        }
        ln.forEach((n)->{
            DataStore ds = this.create(n,this.partitionNumber);
            ds.count();
        });
        //log.info("Waiting for loading data on first member from integration scope store");
        for(String dn : this.integrationEnvironment.getDatabaseNames()){
            DataStore ds = this.createAccessIndexDataStore(dn);
            ds.count();
        }
        for(String dn : this.indexEnvironment.getDatabaseNames()){
            DataStore ds = this.createKeyIndexDataStore(dn);
            ds.count();
        }
        this.create(this.database,this.partitionNumber);
        log.info("Tarantula data store started on ["+node.toString()+"]");
    }
    public void _sync(){
        try {
            long updates = operationSummary.dailyTotalDataUpdates.get();
            long delta = updates-operationSummary.lastSyncUpdates.get();
            if(delta>updateThreshold){
                operationSummary.lastSyncUpdates.set(updates);
                environment.sync();
                environment.cleanLog();
                integrationEnvironment.sync();
                integrationEnvironment.cleanLog();
            }
        }catch (Exception ex){
            log.error("_sync",ex);
        }
        this.serviceContext.schedule(this.diskSynchronizer);
    }
    public void _evict(){
        try{
            long updates = operationSummary.dailyTotalDataUpdates.get();
            long delta = updates-operationSummary.lastEvictUpdates.get();
            if(delta>cacheUpdateThreshold){
                operationSummary.lastEvictUpdates.set(updates);
                environment.evictMemory();
                integrationEnvironment.evictMemory();
            }
        }catch (Exception ex){
            log.error("_evict",ex);
        }
        this.serviceContext.schedule(this.cacheSynchronizer);
    }
    /**
    public void _replicateOnIntegrationScope(ReplicationSynchronizer caller){
        try{
            int integrationSize = 0;
            long totalBytes = operationSummary.dailyTotalIntegrationBytesUpdated.get();
            long totalUpdates = operationSummary.dailyTotalIntegrationUpdates.get();
            long averageBytes = totalBytes>0?(totalBytes/totalUpdates):0;
            int bsize = averageBytes>0?(int)(maxBytesPerBatch/averageBytes):replicationBatchSize;
            operationSummary.lastIntegrationBatchSize.set(bsize);
            OnReplication[] integration = new OnReplication[bsize];
            for(int loop =0;loop<maxTimerLoop;loop++) {
                for (int i = 0; i < bsize; i++) {
                    String integrationId = pendingReplicationIntegrationQueue.poll();
                    if (integrationId == null) break;
                    OnReplication onReplication = pendingReplicationIndex.remove(integrationId);
                    integration[i] = onReplication;
                    integrationSize++;
                }
                if (integrationSize > 0) {
                    this.serviceContext.clusterProvider().accessIndexService().onReplicate(integration, integrationSize,maxReplicationNumber);
                    operationSummary.pendingUpdates.addAndGet((-1)*integrationSize);
                }
                integrationSize = 0;
            }
        } catch (Exception ex){
            log.error("_replicationOnIntegrationScope",ex);
        }
        this.serviceContext.schedule(caller);
    }
    public void _replicateOnDataScope(ReplicationSynchronizer caller){
        try{
            int dataSize = 0;
            long totalBytes = operationSummary.dailyTotalDataBytesUpdated.get();
            long totalUpdates = operationSummary.dailyTotalDataUpdates.get();
            long averageBytes = totalBytes>0?(totalBytes/totalUpdates):0;
            int bsize = averageBytes>0?(int)(maxBytesPerBatch/averageBytes):replicationBatchSize;
            operationSummary.lastDataBatchSize.set(bsize);
            OnReplication[] data = new OnReplication[bsize];
            for(int loop =0;loop<maxTimerLoop;loop++) {
                for (int i = 0; i < bsize; i++) {
                    String dataId = pendingReplicationDataQueue.poll();
                    if(dataId==null) break;
                    OnReplication onReplication = pendingReplicationIndex.remove(dataId);
                    data[i] = onReplication;
                    dataSize++;
                }
                if (dataSize > 0) {
                    this.serviceContext.clusterProvider().recoverService().onReplicate(data, dataSize,maxReplicationNumber);
                    operationSummary.pendingUpdates.addAndGet((-1)*dataSize);
                }
                dataSize = 0;
            }
        }catch (Exception ex){
            log.error("_replicationOnDataScope",ex);
        }
        this.serviceContext.schedule(caller);
    }
    public void _backupOnDataScope(BackupSynchronizer caller){
        try{
            long totalBytes = operationSummary.dailyTotalDataBytesUpdated.get();
            long totalUpdates = operationSummary.dailyTotalDataUpdates.get();
            long averageBytes = totalBytes>0?(totalBytes/totalUpdates):0;
            int bsize = averageBytes>0?(int)(maxBytesPerBatch/averageBytes):backupBatchSize;
            int dataSize = 0;
            OnReplication[] data = new OnReplication[bsize];
            for(int loop =0;loop<maxTimerLoop;loop++) {
                for (int i = 0; i < bsize; i++) {
                    String dataId = pendingBackupDataQueue.poll();
                    if(dataId==null) break;
                    OnReplication onReplication = pendingBackupIndex.remove(dataId);
                    data[i] = onReplication;
                    dataSize++;
                }
                if (dataSize > 0) {
                    this.dBackupProvider.batch(data,dataSize);
                    operationSummary.pendingBackups.addAndGet((-1)*dataSize);
                }
                dataSize = 0;
            }
        }catch (Exception ex){
            log.error("_backupOnDataScope",ex);
        }
        this.serviceContext.schedule(caller);
    }

    public void _backupOnIntegrationScope(BackupSynchronizer caller){
        long totalBytes = operationSummary.dailyTotalIntegrationBytesUpdated.get();
        long totalUpdates = operationSummary.dailyTotalIntegrationUpdates.get();
        long averageBytes = totalBytes>0?(totalBytes/totalUpdates):0;
        int bsize = averageBytes>0?(int)(maxBytesPerBatch/averageBytes):backupBatchSize;
        //log.warn("Backup sync 2->"+bsize);
        OnReplication[] integration = new OnReplication[bsize];
        int integrationSize = 0;
        for(int loop =0;loop<maxTimerLoop;loop++) {
            for (int i = 0; i < bsize; i++) {
                String integrationId = pendingBackupIntegrationQueue.poll();
                if (integrationId == null) break;
                OnReplication onReplication = pendingBackupIndex.remove(integrationId);
                integration[i] = onReplication;
                integrationSize++;
            }
            if (integrationSize > 0) {
                this.iBackupProvider.batch(integration,integrationSize);
                operationSummary.pendingBackups.addAndGet((-1)*integrationSize);
            }
            integrationSize = 0;
        }
        this.serviceContext.schedule(caller);
    }
    **/
    @Override
    public void shutdown() throws Exception {
        //running = false;
        iBackupProvider.shutdown();
        dBackupProvider.shutdown();
        dMap.forEach((k,v)->{
            try{
                v.close();
            }catch (Exception ex){
                log.warn(k+" store error on close>"+ex.getMessage());
            }
        });
        this.environment.cleanLog();
        this.integrationEnvironment.cleanLog();
        this.indexEnvironment.cleanLog();
        this.environment.close();
        this.integrationEnvironment.close();
        this.indexEnvironment.close();
        log.info("Berkeley JE data store shut down on ["+node.toString()+"]");
    }

    // map store listener methods

    public <T extends Recoverable> void onBackingUp(Metadata metadata,String key,T t){
        if(metadata.scope()==Distributable.DATA_SCOPE){
            /**
            String pendingId = metadata.source()+"#"+key;
            pendingBackupIndex.compute(pendingId,(k,v)->{
                if(v==null){
                    pendingBackupDataQueue.offer(pendingId);
                    operationSummary.pendingBackups.incrementAndGet();
                }
                return new ReplicationData(metadata.source(),key,t);
            });**/
            dataScopeReplicationProxy.onBackingUp(metadata,key,t);
            return;
        }
        if(metadata.scope()==Distributable.INTEGRATION_SCOPE){
            /**
            String pendingId = metadata.source()+"#"+key;
            pendingBackupIndex.compute(pendingId,(k,v)->{
                if(v==null){
                    pendingBackupIntegrationQueue.offer(pendingId);
                    operationSummary.pendingBackups.incrementAndGet();
                }
                return new ReplicationData(metadata.source(),key,t);
            });**/
            integrationScopeReplicationProxy.onBackingUp(metadata,key,t);
        }
    }

    @Override
    public void onDistributing(Metadata metadata, String stringKey,byte[] key, byte[] value) {
        int keySize = key.length;
        int valueSize = value.length;
        if(metadata.scope()==Distributable.DATA_SCOPE && maxReplicationNumber>0){
            //String pendingId = metadata.source()+"#"+stringKey;
            operationSummary.dailyTotalDataUpdates.incrementAndGet();
            operationSummary.dailyTotalDataBytesUpdated.addAndGet(keySize+valueSize);
            dataScopeReplicationProxy.onDistributing(metadata,stringKey,key,value);
            /**
            pendingReplicationIndex.compute(pendingId,(k,v)->{
                if(v==null){
                    pendingReplicationDataQueue.offer(pendingId);
                    operationSummary.pendingUpdates.incrementAndGet();
                }
                return new ReplicationData(metadata.source(),key,value);
            });**/
        }
        else if(metadata.scope()==Distributable.INTEGRATION_SCOPE && maxReplicationNumber>0){
            //String pendingId = metadata.source()+"#"+stringKey;
            operationSummary.dailyTotalIntegrationUpdates.incrementAndGet();
            operationSummary.dailyTotalIntegrationBytesUpdated.addAndGet(keySize+valueSize);
            integrationScopeReplicationProxy.onDistributing(metadata,stringKey,key,value);
            /**
            pendingReplicationIndex.compute(pendingId,(k,v)->{
                if(v==null){
                    pendingReplicationIntegrationQueue.offer(pendingId);
                    operationSummary.pendingUpdates.incrementAndGet();
                }
                return new ReplicationData(metadata.partition(),key,value);
            });**/
        }
        onMetrics();
    }

    @Override
    public byte[] onRecovering(Metadata metadata,String stringKey,byte[] key){
        if(metadata.scope()==Distributable.DATA_SCOPE){
            return this.dataScopeReplicationProxy.onRecovering(metadata,stringKey,key);
        }
        if(metadata.scope()==Distributable.INTEGRATION_SCOPE){
            return this.integrationScopeReplicationProxy.onRecovering(metadata,stringKey,key);
        }
        if(metadata.scope()==Distributable.INDEX_SCOPE){
            return this.indexScopeReplicationProxy.onRecovering(metadata,stringKey,key);
        }
        return null;
    }

    @Override
    public void onDeleting(Metadata metadata,byte[] key){
        if(metadata.scope()==Distributable.DATA_SCOPE){
            dataScopeReplicationProxy.onDeleting(metadata,key);
            return;
        }
        if(metadata.scope()==Distributable.INTEGRATION_SCOPE){
            integrationScopeReplicationProxy.onDeleting(metadata,key);
            return;
        }
        if(metadata.scope()==Distributable.INDEX_SCOPE){
            indexScopeReplicationProxy.onDeleting(metadata,key);
        }
    }

    //end of map store listener

    public void backup(int scope){
        if(scope==Distributable.DATA_SCOPE){
            this.environment.sync();
            this._backup(this.environment,dataPath,backupPath);
        }
        else if(scope==Distributable.INTEGRATION_SCOPE){
            this.integrationEnvironment.sync();
            this._backup(integrationEnvironment,integrationPath,integrationBackupPath);
        }
    }
    private void _backup(Environment bEnvironment,String dPath,String bPath){
        DbBackup backup =null;
        try{
            DataInputStream fi = new DataInputStream(new FileInputStream(dPath+FileSystems.getDefault().getSeparator()+"last.dat"));
            long last = fi.readLong();
            long lastDir = fi.readLong();
            String lastVersion = fi.readUTF();
            fi.close();
            Path tp = Paths.get(bPath+FileSystems.getDefault().getSeparator()+lastDir);
            if(!Files.exists(tp)){
                Files.createDirectories(tp);
            }
            LocalDateTime st = LocalDateTime.now();
            log.info("JE Berkeley Incremental last index file/dir ["+last+"/"+lastDir+"] at ["+lastVersion+"] from ["+dPath+"] to ["+bPath+"]");
            backup = last>0?new DbBackup(bEnvironment,last):new DbBackup(bEnvironment);
            bEnvironment.checkpoint(new CheckpointConfig().setForce(true));
            backup.startBackup();
            String[] bset = backup.getLogFilesInBackupSet();
            for(String b : bset){
                FileChannel fc = new FileOutputStream(tp.toFile().getPath()+FileSystems.getDefault().getSeparator()+b).getChannel();
                FileInputStream fis = new FileInputStream(Paths.get(dPath+FileSystems.getDefault().getSeparator()+b).toFile());
                LogVerificationReadableByteChannel vc = new LogVerificationReadableByteChannel(bEnvironment,fis.getChannel(),b);
                ByteBuffer buffer = ByteBuffer.allocate(4096);
                while (true) {
                    final int len = vc.read(buffer);
                    if (len < 0) {
                        break;
                    }
                    buffer.flip();
                    fc.write(buffer);
                    buffer.clear();
                }
                fc.close();
                vc.close();
                backup.removeFileProtection(b);
            }
            LocalDateTime ed = LocalDateTime.now();
            log.info("JE Berkeley finished incremental backup files/duration ["+bset.length+"/"+ TimeUtil.durationUTCMilliseconds(st,ed)+"] at ["+ed.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)+"]");
            DataOutputStream fo = new DataOutputStream(new FileOutputStream(dPath+FileSystems.getDefault().getSeparator()+"last.dat"));
            fo.writeLong(backup.getLastFileInBackupSet());
            fo.writeLong(lastDir+1);
            fo.writeUTF(ed.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            fo.close();

        }catch (Exception ex){
            log.error("incremental backup clashed",ex);
        }
        finally {
            backup.endBackup();
        }
    }
    public void backup(int scope,OnBackup onBackup){
        if(scope==Distributable.DATA_SCOPE){
            this.environment.sync();
            this._backup(onBackup,dataPath,this.environment);
        }
        else if(scope==Distributable.INTEGRATION_SCOPE){
            this.integrationEnvironment.sync();
            this._backup(onBackup,integrationPath,this.integrationEnvironment);
        }
    }
    private void _backup(OnBackup onBackup,String dPath,Environment bEnvironment){
        DbBackup backup =null;
        try{
            LocalDateTime st = LocalDateTime.now();
            log.info("JE Berkeley full backup starting at ["+st.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)+"] from ["+dPath+"]");
            backup = new DbBackup(bEnvironment);
            bEnvironment.checkpoint(new CheckpointConfig().setForce(true));
            backup.startBackup();
            String[] bset = backup.getLogFilesInBackupSet();
            for(String b : bset){
                FileInputStream fis = new FileInputStream(Paths.get(dPath+FileSystems.getDefault().getSeparator()+b).toFile());
                LogVerificationReadableByteChannel vc = new LogVerificationReadableByteChannel(bEnvironment,fis.getChannel(),b);
                onBackup.on(b,bset.length,vc);
                vc.close();
                backup.removeFileProtection(b);
            }
            LocalDateTime ed = LocalDateTime.now();
            log.info("JE Berkeley full backup files/duration ["+bset.length+"/"+TimeUtil.durationUTCMilliseconds(st,ed)+"] finished at ["+ed.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)+"]");
        }catch (Exception ex){
            log.error("full backup clashed",ex);
            throw new RuntimeException("backup crashed",ex);
        }
        finally {
            backup.endBackup();
        }
    }
    public void recover(int scope,OnBackup backup){
        if(scope==Distributable.DATA_SCOPE){
            _recover(backup,dataPath,backupPath,this.environment);
        }
        else if(scope==Distributable.INTEGRATION_SCOPE){
            _recover(backup,integrationPath,integrationBackupPath,this.integrationEnvironment);
        }
    }
    private void _recover(OnBackup backup,String dPath,String bPath,Environment bEnvironment){
        try{
            DataInputStream fi = new DataInputStream(new FileInputStream(dPath+FileSystems.getDefault().getSeparator()+"last.dat"));
            long last = fi.readLong(); //last file
            long lastDir = fi.readLong()-1; //last dir
            String lastVersion = fi.readUTF();//last updated
            fi.close();
            LocalDateTime st = LocalDateTime.now();
            log.info("JE Berkeley is recovering from index["+last+"/"+lastDir+"] from ["+lastVersion+"] from ["+bPath+"]");
            ArrayList<File> rlist = new ArrayList<>();
            do {
                Path tp = Paths.get(bPath + FileSystems.getDefault().getSeparator() + lastDir);
                for(String fn : tp.toFile().list()){
                    rlist.add(Paths.get(tp.toFile().getPath()+FileSystems.getDefault().getSeparator()+fn).toFile());
                }
                lastDir--;
            }while (lastDir>=0);
            for(File r : rlist){
                FileInputStream fis = new FileInputStream(r);
                LogVerificationReadableByteChannel vc = new LogVerificationReadableByteChannel(bEnvironment,fis.getChannel(),r.getName());
                backup.on(r.getName(),rlist.size(),vc);
                vc.close();
            }
            LocalDateTime ed = LocalDateTime.now();
            log.info("JE Berkeley finished total recovered files /duration ["+rlist.size()+"/"+TimeUtil.durationUTCMilliseconds(st,ed)+"] at ["+ed.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)+"]");

        }catch (Exception ex){
            log.error("recover clashed",ex);
            throw new RuntimeException("recover crashed",ex);
        }
    }
    @Override
    public void atMidnight() {
        if(dailyBackup){
            backup(Distributable.DATA_SCOPE);//daily incremental backup
            backup(Distributable.INTEGRATION_SCOPE);
        }
        operationSummary.dailyTotalDataBytesUpdated.set(0);
        operationSummary.dailyTotalDataUpdates.set(0);
        operationSummary.dailyTotalIntegrationBytesUpdated.set(0);
        operationSummary.dailyTotalIntegrationUpdates.set(0);
    }
    private void onMetrics(){
        this.metricsListener.onUpdated(PerformanceMetrics.PERFORMANCE_DATA_STORE_COUNT,1);
    }
    public void registerMetricsListener(MetricsListener metricsListener){
        if(metricsListener==null) return;
        this.metricsListener = metricsListener;
    }
    @Override
    public void registerSummary(Summary summary){
        summary.registerCategory(DataStoreOperationSummary.PENDING_UPDATE_SIZE);
        summary.registerCategory(DataStoreOperationSummary.PENDING_BACKUP_SIZE);
        summary.registerCategory(DataStoreOperationSummary.DAILY_TOTAL_UPDATES);
        summary.registerCategory(DataStoreOperationSummary.CACHE_MISS_NUMBER);
        summary.registerCategory(DataStoreOperationSummary.AVERAGE_BYTES_PER_UPDATE);
        summary.registerCategory(DataStoreOperationSummary.DAILY_TOTAL_BYTES_UPDATED);
        summary.registerCategory(DataStoreOperationSummary.LAST_DATA_BATCH_SIZE);
        summary.registerCategory(DataStoreOperationSummary.LAST_INTEGRATION_BATCH_SIZE);
    }
    @Override
    public void updateSummary(Summary summary){
        summary.update(DataStoreOperationSummary.PENDING_UPDATE_SIZE,operationSummary.pendingUpdates.get());
        summary.update(DataStoreOperationSummary.PENDING_BACKUP_SIZE,operationSummary.pendingBackups.get());
        summary.update(DataStoreOperationSummary.REPLICATION_NODE_NUMBER,maxReplicationNumber);
        long totalBytes = operationSummary.dailyTotalDataBytesUpdated.get()+operationSummary.dailyTotalIntegrationBytesUpdated.get();
        long totalUpdates = operationSummary.dailyTotalDataUpdates.get()+operationSummary.dailyTotalIntegrationUpdates.get();
        summary.update(DataStoreOperationSummary.DAILY_TOTAL_UPDATES,totalUpdates);
        summary.update(DataStoreOperationSummary.DAILY_TOTAL_BYTES_UPDATED,totalBytes);
        summary.update(DataStoreOperationSummary.AVERAGE_BYTES_PER_UPDATE,totalBytes/totalUpdates);
        long totalMissed = environment.getStats(null).getNCacheMiss()+integrationEnvironment.getStats(null).getNCacheMiss();
        summary.update(DataStoreOperationSummary.CACHE_MISS_NUMBER,totalMissed);
        summary.update(DataStoreOperationSummary.LAST_DATA_BATCH_SIZE,operationSummary.lastDataBatchSize.get());
        summary.update(DataStoreOperationSummary.LAST_INTEGRATION_BATCH_SIZE,operationSummary.lastIntegrationBatchSize.get());
    }

}
