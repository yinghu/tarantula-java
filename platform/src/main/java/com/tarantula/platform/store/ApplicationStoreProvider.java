package com.tarantula.platform.store;

import com.icodesoftware.DataStore;
import com.icodesoftware.OnAccess;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.inventory.Inventory;
import com.tarantula.platform.service.AuthObject;

import java.util.Map;
import java.util.UUID;

public class ApplicationStoreProvider extends AuthObject {

    private DataStore dataStore;
    private PlatformGameServiceProvider platformGameServiceProvider;

    private TarantulaLogger logger;
    public ApplicationStoreProvider(String typeId,PlatformGameServiceProvider platformGameServiceProvider){
        super(typeId,"");
        this.platformGameServiceProvider = platformGameServiceProvider;
    }

    @Override
    public String name(){
        return OnAccess.APPLICATION_STORE;
    }


    @Override
    public void setup(ServiceContext serviceContext){
        super.setup(serviceContext);
        String ds = typeId.replaceAll("-","_")+"_application_store_transaction";
        dataStore = serviceContext.dataStore(ds,serviceContext.node().partitionNumber());
        this.logger = serviceContext.logger(ApplicationStoreProvider.class);
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
        Transaction transaction = new Transaction();
        transaction.index(tid);
        transaction.owner(systemId);
        dataStore.create(transaction);
        return true;
    }

}
