package com.tarantula.platform.store;

import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.service.MetricsListener;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.inventory.ApplicationRedeemer;
import com.tarantula.platform.inventory.UserInventory;
import com.tarantula.platform.service.AuthObject;

import java.util.Map;

public class ApplicationStoreProvider extends AuthObject {

    private static final TarantulaLogger logger = JDKLogger.getLogger(ApplicationStoreProvider.class);
    private PlatformGameServiceProvider platformGameServiceProvider;


    public ApplicationStoreProvider(PlatformGameServiceProvider platformGameServiceProvider, MetricsListener metricsListener){
        super(platformGameServiceProvider.gameCluster().typeId(),"");
        this.platformGameServiceProvider = platformGameServiceProvider;
        this.applicationMetricsListener = metricsListener;
    }

    @Override
    public String name(){
        return OnAccess.APPLICATION_STORE;
    }



    @Override
    public boolean validate(Map<String,Object> params){
        String bundleId = (String)params.get(OnAccess.STORE_BUNDLE_ID);
        ShoppingItem shoppingItem = this.platformGameServiceProvider.storeServiceProvider().shoppingItem(bundleId);
        if(shoppingItem == null){
            logger.warn("Shopping Item not existed");
            return false;
        }
        if(shoppingItem.purchaseType() == ShoppingItem.PurchaseType.IAP){
            logger.warn("IAP shopping item cannot be here");
            return false;
        }
        long systemId = (long) params.get(OnAccess.SYSTEM_ID);
        GameCluster gameCluster = platformGameServiceProvider.gameCluster();
        Transaction t = gameCluster.transaction();
        boolean suc = t.execute(ctx->{
            ApplicationPreSetup setup =(ApplicationPreSetup)ctx;
            Inventory inventory = setup.inventory(systemId, shoppingItem.virtualCurrency().name());
            if(inventory==null || !inventory.transact(shoppingItem.price()*(-1))){
                logger.warn("Not enough balance to buy");
                return false;
            }
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
        return true;
    }

}
