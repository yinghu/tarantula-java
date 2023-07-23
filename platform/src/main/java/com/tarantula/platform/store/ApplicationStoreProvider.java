package com.tarantula.platform.store;

import com.icodesoftware.OnAccess;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceEventLogger;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.inventory.Inventory;
import com.tarantula.platform.service.AuthObject;

import java.util.Map;
import java.util.UUID;

public class ApplicationStoreProvider extends AuthObject {

    private ServiceEventLogger serviceEventLogger;
    private PlatformGameServiceProvider platformGameServiceProvider;

    private TarantulaLogger logger;
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
    public void setup(ServiceContext serviceContext){
        super.setup(serviceContext);
        serviceEventLogger = serviceContext.serviceEventLogger(typeId+"_application_store");
        this.logger = JDKLogger.getLogger(ApplicationStoreProvider.class);
        logger.warn("application store validator->"+typeId);
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
        String systemId = (String) params.get(OnAccess.SYSTEM_ID);
        Inventory inventory = this.platformGameServiceProvider.inventoryServiceProvider().inventory(systemId,shoppingItem.purchaseType().name(),shoppingItem.virtualCurrency().name());
        if(inventory==null || !inventory.transact(shoppingItem.price()*(-1))){
            logger.warn("Not enough balance to buy");
            return false;
        }
        String tid = UUID.randomUUID().toString();
        Transaction transaction = new Transaction(systemId,"soft purchase",bundleId);
        transaction.index(tid);
        serviceEventLogger.log(transaction);
        return true;
    }

}
