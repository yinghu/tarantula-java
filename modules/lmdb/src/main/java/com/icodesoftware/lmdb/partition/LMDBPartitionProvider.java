package com.icodesoftware.lmdb.partition;

import com.icodesoftware.*;
import com.icodesoftware.lmdb.*;

import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.MapStoreListener;
import com.icodesoftware.service.Metadata;
import org.lmdbjava.Dbi;
import org.lmdbjava.DbiFlags;
import org.lmdbjava.Txn;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class LMDBPartitionProvider implements LocalLMDBProvider {

    private static final TarantulaLogger logger = JDKLogger.getLogger(LMDBPartitionProvider.class);

    private String name = EnvSetting.ENV_PROVIDER_NAME;
    private static int PENDING_BUFFER_SIZE = EnvSetting.MAX_PENDING_BUFFER_NUMBER;
    private static int KEY_SIZE = EnvSetting.KEY_SIZE;
    private static int VALUE_SIZE = EnvSetting.VALUE_SIZE;

    private final int maxPartitionNumber = 3;
    private int storeMbSize = 1000; //1G
    private String basePath ="target/lmdb/partition";

    private final ConcurrentHashMap<Integer, LMDBEnv> partitionMap = new ConcurrentHashMap<>();

    private DistributionIdGenerator distributionIdGenerator;
    private MapStoreListener integrationMapStoreListener;
    private MapStoreListener dataMapStoreListener;

    final ArrayBlockingQueue<BufferCache> pendingQueue = new ArrayBlockingQueue<>(PENDING_BUFFER_SIZE);;

    public LMDBPartitionProvider(){
    }
    @Override
    public void start() throws Exception {
        if(distributionIdGenerator==null) throw new RuntimeException("DistributionIdGenerator Not Registered");
        for(int i=0;i<maxPartitionNumber;i++){
            String path =basePath +"/"+i;
            Files.createDirectories(Paths.get(path));
            Files.createDirectories(Paths.get(path+"/back"));
            LMDBEnv env = new LMDBEnv(new EnvSetting(EnvSetting.data,path,storeMbSize,i,true));
            env.lmdbDataStoreProvider = this;
            env.start();
            partitionMap.put(i,env);
        }
        logger.warn("LMDB partition provider started with partitions ["+maxPartitionNumber+"]");
    }

    @Override
    public void shutdown() throws Exception {
        partitionMap.forEach((k,p)->{
            try {
                p.shutdown();
            }catch (Exception ex){
                //ignore
            }
        });
    }


    public void assign(Recoverable.DataBuffer key){
        distributionIdGenerator.assign(key);
    }

    @Override
    public void configure(Map<String, Object> properties) {

    }

    @Override
    public void registerDistributionIdGenerator(DistributionIdGenerator distributionIdGenerator) {
        this.distributionIdGenerator = distributionIdGenerator;
    }

    @Override
    public void registerMapStoreListener(int scope, MapStoreListener mapStoreListener) {
        if(scope== Distributable.DATA_SCOPE){
            this.dataMapStoreListener = mapStoreListener;
            return;
        }
        if(scope== Distributable.INTEGRATION_SCOPE){
            this.integrationMapStoreListener = mapStoreListener;
        }

    }

    @Override
    public File backup(int scope) {
        return null;
    }

    @Override
    public DataStore createAccessIndexDataStore(String name) {
        LMDBPartitionDataStore partitionDataStore = new LMDBPartitionDataStore(Distributable.INTEGRATION_SCOPE,name,this);
        return partitionDataStore;
    }

    @Override
    public DataStore createKeyIndexDataStore(String name) {
        LMDBPartitionDataStore partitionDataStore = new LMDBPartitionDataStore(Distributable.INDEX_SCOPE,name,this);
        return partitionDataStore;
    }

    @Override
    public DataStore createDataStore(String name) {
        LMDBPartitionDataStore partitionDataStore = new LMDBPartitionDataStore(Distributable.DATA_SCOPE,name,this);
        return partitionDataStore;
    }

    @Override
    public DataStore createLocalDataStore(String name) {
        LMDBPartitionDataStore partitionDataStore = new LMDBPartitionDataStore(Distributable.LOCAL_SCOPE,name,this);
        return partitionDataStore;
    }

    @Override
    public DataStore createLogDataStore(String name) {
        LMDBPartitionDataStore partitionDataStore = new LMDBPartitionDataStore(Distributable.LOG_SCOPE,name,this);
        return partitionDataStore;
    }

    @Override
    public List<String> list() {
        return List.of();
    }

    @Override
    public List<String> list(int scope) {
        return List.of();
    }

    @Override
    public DataStore lookup(String name) {
        return null;
    }

    @Override
    public Transaction transaction(int scope) {
        return null;
    }

    @Override
    public Recoverable.DataBufferPair dataBufferPair(){
        BufferCache cache = pendingQueue.poll();
        if(cache!=null) return cache;
        return new BufferCache(KEY_SIZE,VALUE_SIZE,pendingQueue);
    }

    @Override
    public String name() {
        return name;
    }

    public long storeSize(){
        return EnvSetting.toBytesFromMb(storeMbSize);
    }
    public int maxReaderNumber(){
        return EnvSetting.MAX_READER_NUMBER;
    }
    public int maxDatabaseNumber(){
        return EnvSetting.MAX_STORE_NUMBER;
    }
    public boolean diskSyncOnCommit(){
        return !EnvSetting.ENV_NO_SYNC_Flag;
    }

    @Override
    public void onUpdating(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value, long transactionId) {

    }

    @Override
    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value) {
        return false;
    }

    @Override
    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, DataStore.BufferStream bufferStream) {
        return false;
    }

    @Override
    public boolean onDeleting(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value, long transactionId) {
        return false;
    }

    @Override
    public void onCommit(int scope, long transactionId) {

    }

    @Override
    public void onAbort(int scope, long transactionId) {

    }

    public void onUpdated(String category,double delta){

    }

    public LocalEdgeDataStore createEdgeDB(int scope, String source, String label){
        final String edgeName = source+"#"+label;
        return null;
        //return edgMap.computeIfAbsent(edgeName,k->localEdgeDataStore(scope,source,label,null));
    }

    public  LocalEdgeDataStore localEdgeDataStore(int scope, String source, String label, Txn<ByteBuffer> txn){
        return null;
    }

    public LocalDataStore partition(int scope,String name,Recoverable.DataBuffer key){
        LMDBEnv lmdb = partitionMap.get(partition(key));
        Dbi<ByteBuffer> dbi = lmdb.env.openDbi(store(scope,name), DbiFlags.MDB_CREATE);
        LocalMetadata metadata = new LocalMetadata(scope,name);
        return new LocalDataStore(metadata,dbi,lmdb);
    }

    private int partition(Recoverable.DataBuffer key){
        return Math.abs(Arrays.hashCode(key.array())) % maxPartitionNumber;
    }

    private String store(int scope,String name){
        if(name.contains("@") || name.contains("#") || name.contains("-")) throw new RuntimeException("store cannot have @ , #, or -");
        switch (scope){
            case Distributable.DATA_SCOPE -> {
                return EnvSetting.data+"@"+name;
            }
            case Distributable.INTEGRATION_SCOPE -> {
                return EnvSetting.integration+"@"+name;
            }
            case Distributable.INDEX_SCOPE -> {
                return EnvSetting.index+"@"+name;
            }
            case Distributable.LOG_SCOPE -> {
                return EnvSetting.log+"@"+name;
            }
            case Distributable.LOCAL_SCOPE -> {
                return EnvSetting.local+"@"+name;
            }
            default -> throw new RuntimeException("Scope ["+scope+"] not supported");
        }
    }

}
