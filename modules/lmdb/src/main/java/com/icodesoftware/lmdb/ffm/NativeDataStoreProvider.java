package com.icodesoftware.lmdb.ffm;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.lmdb.EnvSetting;
import com.icodesoftware.lmdb.LocalDistributionIdGenerator;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.util.TimeUtil;

import java.util.Map;

public class NativeDataStoreProvider{

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
}
