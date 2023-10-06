package com.tarantula.platform.store;

import com.icodesoftware.OnAccess;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.MetricsListener;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.service.AuthObject;

import java.util.Map;

public class DeveloperStoreProvider extends AuthObject {

    private static final TarantulaLogger logger = JDKLogger.getLogger(DeveloperStoreProvider.class);

    public DeveloperStoreProvider(PlatformGameServiceProvider gameServiceProvider, MetricsListener metricsListener){
        super(gameServiceProvider.gameCluster().typeId(),"");
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
        params.put(OnAccess.STORE_TRANSACTION_ID,serviceContext.distributionId());
        params.put(OnAccess.STORE_QUANTITY,1);
        TransactionLog transaction = new TransactionLog((String)params.get(OnAccess.SYSTEM_ID),(String)params.get(OnAccess.STORE_BUNDLE_ID),"token access");

        serviceEventLogger.log(transaction);
        return true;
    }

}
