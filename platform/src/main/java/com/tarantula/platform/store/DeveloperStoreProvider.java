package com.tarantula.platform.store;

import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.service.MetricsListener;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.inventory.ApplicationRedeemer;
import com.tarantula.platform.service.AuthObject;

import java.util.Map;

public class DeveloperStoreProvider extends AuthObject {

    private static final TarantulaLogger logger = JDKLogger.getLogger(DeveloperStoreProvider.class);
    private PlatformGameServiceProvider gameServiceProvider;
    public DeveloperStoreProvider(PlatformGameServiceProvider gameServiceProvider, MetricsListener metricsListener){
        super(gameServiceProvider.gameCluster().typeId(),"");
        this.gameServiceProvider = gameServiceProvider;
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
        String bundleId = (String)params.get(OnAccess.STORE_BUNDLE_ID);
        String systemId = (String)params.get(OnAccess.SYSTEM_ID);
        ShoppingItem shoppingItem = gameServiceProvider.storeServiceProvider().shoppingItem(bundleId);
        if(shoppingItem==null){
            logger.warn("Shopping Item not existed  : "+bundleId);
            return false;
        }
        Transaction t = gameCluster.transaction();
        boolean suc = t.execute(ctx->{
            ApplicationPreSetup setup =(ApplicationPreSetup)ctx;
            Descriptor app = gameCluster.application(shoppingItem.configurationTypeId());
            ApplicationRedeemer redeemer = new ApplicationRedeemer(systemId,setup);
            redeemer.distributionKey(bundleId);
            if(!setup.load(app,redeemer)) return false;
            redeemer.redeem();
            return true;
        });
        if(!suc) return false;
        params.put(OnAccess.STORE_TRANSACTION_ID,serviceContext.distributionId());
        params.put(OnAccess.STORE_QUANTITY,1);
        TransactionLog transaction = new TransactionLog((String)params.get(OnAccess.SYSTEM_ID),(String)params.get(OnAccess.STORE_BUNDLE_ID),"token access");
        serviceEventLogger.log(transaction);
        return true;
    }

}
