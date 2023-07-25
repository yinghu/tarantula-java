package com.tarantula.platform.service;

import com.icodesoftware.OnAccess;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.MetricsListener;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.store.Transaction;

import java.util.Map;
import java.util.UUID;

public class DeveloperStoreProvider extends AuthObject{

    private static final TarantulaLogger logger = JDKLogger.getLogger(DeveloperStoreProvider.class);

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
    public boolean validate(Map<String,Object> params){
        GameCluster gameCluster = this.tokenValidatorProvider.validateGameClusterAccessKey((String)params.get(OnAccess.STORE_RECEIPT));
        if(gameCluster==null|| !gameCluster.typeId().equals(this.typeId)){
            logger.warn("Illegal access from ["+params.get(OnAccess.STORE_RECEIPT)+"]");
            return false;
        }
        String tid = UUID.randomUUID().toString();
        params.put(OnAccess.STORE_TRANSACTION_ID,tid);
        params.put(OnAccess.STORE_QUANTITY,1);
        Transaction transaction = new Transaction((String)params.get(OnAccess.SYSTEM_ID),(String)params.get(OnAccess.STORE_BUNDLE_ID),"token access");
        transaction.index(tid);
        serviceEventLogger.log(transaction);
        return true;
    }

}
