package com.tarantula.platform.service;

import com.icodesoftware.DataStore;
import com.icodesoftware.OnAccess;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TokenValidatorProvider;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.store.Transaction;

import java.util.Map;
import java.util.UUID;

public class DeveloperStoreProvider extends AuthObject{

    private DataStore dataStore;
    private TokenValidatorProvider tokenValidatorProvider;

    private TarantulaLogger logger;
    public DeveloperStoreProvider(PlatformGameServiceProvider gameServiceProvider, MetricsListener metricsListener){
        super(gameServiceProvider.gameCluster().typeId(),"");
        //this.platformGameServiceProvider = platformGameServiceProvider;
        this.applicationMetricsListener = metricsListener;
    }

    @Override
    public String name(){
        return OnAccess.DEVELOPER_STORE;
    }


    @Override
    public void setup(ServiceContext serviceContext){
        super.setup(serviceContext);
        String ds = typeId.replaceAll("-","_")+"_developer_store_transaction";
        dataStore = serviceContext.dataStore(ds,serviceContext.node().partitionNumber());
        this.tokenValidatorProvider = (TokenValidatorProvider) serviceContext.serviceProvider(TokenValidatorProvider.NAME);
        this.logger = serviceContext.logger(DeveloperStoreProvider.class);
        this.logger.warn("Developer Store Registered on->"+typeId);
    }
    @Override
    public boolean validate(Map<String,Object> params){
        GameCluster gameCluster = this.tokenValidatorProvider.validateGameClusterAccessKey((String)params.get(OnAccess.STORE_RECEIPT));
        if(gameCluster==null|| !gameCluster.typeId().equals(this.typeId)){
            logger.warn("Illegal access from ["+params.get(OnAccess.STORE_RECEIPT)+"]");
            return false;
        }
        String tid = UUID.randomUUID().toString();
        params.put(OnAccess.STORE_TRANSACTION_ID,tid);
        params.put(OnAccess.STORE_QUANTITY,1);
        Transaction transaction = new Transaction();
        transaction.index(tid);
        transaction.owner((String)params.get(OnAccess.SYSTEM_ID));
        dataStore.create(transaction);
        return true;
    }

}
