package com.icodesoftware.lmdb.ffm;

import com.icodesoftware.*;
import com.icodesoftware.lmdb.EnvSetting;
import com.icodesoftware.lmdb.LocalDistributionIdGenerator;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.service.MapStoreListener;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.util.TimeUtil;

import java.io.File;
import java.util.List;
import java.util.Map;

public class NativeDataStoreProvider implements DataStoreProvider{

    private static final TarantulaLogger logger = JDKLogger.getLogger(NativeDataStoreProvider.class);

    private final NativeEnv dataEnv = new NativeEnv(EnvSetting.setting(Distributable.DATA_SCOPE,EnvSetting.ENV_BASE_DIR,10));
    private NativeEnv integrationEnv = new NativeEnv(EnvSetting.setting(Distributable.INTEGRATION_SCOPE,EnvSetting.ENV_BASE_DIR,10));
    private NativeEnv indexEnv = new NativeEnv(EnvSetting.setting(Distributable.INDEX_SCOPE,EnvSetting.ENV_BASE_DIR,10));
    private NativeEnv logEnv = new NativeEnv(EnvSetting.setting(Distributable.LOG_SCOPE,EnvSetting.ENV_BASE_DIR,10));
    private NativeEnv localEnv = new NativeEnv(EnvSetting.setting(Distributable.LOCAL_SCOPE,EnvSetting.ENV_BASE_DIR,10));


    private DataStoreProvider.DistributionIdGenerator distributionIdGenerator = new LocalDistributionIdGenerator(1,TimeUtil.epochMillisecondsFromMidnight(2020,1,1));;

    public void configure(Map<String, Object> properties) {
        this.dataEnv.envSetting((EnvSetting)properties.getOrDefault(EnvSetting.data,this.dataEnv.envSetting()));
        this.integrationEnv.envSetting((EnvSetting)properties.getOrDefault(EnvSetting.integration,this.dataEnv.envSetting()));
        this.indexEnv.envSetting((EnvSetting)properties.getOrDefault(EnvSetting.index,this.indexEnv.envSetting()));
        this.logEnv.envSetting((EnvSetting)properties.getOrDefault(EnvSetting.log,this.logEnv.envSetting()));
        this.localEnv.envSetting((EnvSetting)properties.getOrDefault(EnvSetting.local,this.localEnv.envSetting()));
    }

    public void registerDistributionIdGenerator(DataStoreProvider.DistributionIdGenerator distributionIdGenerator){
        if(distributionIdGenerator!=null) this.distributionIdGenerator = distributionIdGenerator;
    }
    public void registerMapStoreListener(int scope, MapStoreListener mapStoreListener){

    }
    public File backup(int scope){
        return null;
    }

    public List<String> list(){
        return dataEnv.names();
    }

    public List<String> list(int scope){
        return dataEnv.names();
    }

    @Override
    public Transaction transaction(int scope) {
        return null;
    }

    public void start() throws Exception {
        if(distributionIdGenerator==null) throw new RuntimeException("Distributed id generator must be registered");
        dataEnv.start();
        integrationEnv.start();
        indexEnv.start();
        logEnv.start();
        localEnv.start();
        logger.warn("Native data store provider started!");
    }

    public DataStore createDataStore(String name){
        return new NativeDataStore(name,this,dataEnv);
    }

    public DataStore createAccessIndexDataStore(String name){
        return new NativeDataStore(name,this,integrationEnv);
    }

    //create none-partitioned local scope data store
    public DataStore createKeyIndexDataStore(String name){
        return new NativeDataStore(name,this,indexEnv);
    }


    //create partitioned data scope data store
    public DataStore createLocalDataStore(String name){
        return new NativeDataStore(name,this,localEnv);
    }

    public DataStore createLogDataStore(String name){
        return new NativeDataStore(name,this,logEnv);
    }

    public void assign(Recoverable.DataBuffer dataBuffer){
        this.distributionIdGenerator.assign(dataBuffer);
    }

    public void shutdown() throws Exception {
        dataEnv.shutdown();
        integrationEnv.shutdown();
        indexEnv.shutdown();
        logEnv.shutdown();
        localEnv.shutdown();
        logger.warn("Native data store provider shut down!");
    }

    public void onUpdating(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value, long transactionId){

    }
    //recover cluster operation
    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value){
        return false;
    }

    public boolean onRecovering(Metadata metadata,Recoverable.DataBuffer key,DataStore.BufferStream bufferStream){
        return false;
    }

    public boolean onDeleting(Metadata metadata,Recoverable.DataBuffer key, Recoverable.DataBuffer value,long transactionId){
        return false;
    }

    public void onCommit(int scope,long transactionId){

    }

    public void onAbort(int scope,long transactionId){}

    @Override
    public void onUpdated(String category, double delta) {

    }

    @Override
    public String name() {
        return EnvSetting.ENV_PROVIDER_NAME;
    }
}
