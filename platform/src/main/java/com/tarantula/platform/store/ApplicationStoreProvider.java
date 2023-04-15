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
        logger.warn("Passing->"+typeId+">>"+params.get(OnAccess.STORE_BUNDLE_ID));
        ShoppingItem shoppingItem = this.platformGameServiceProvider.storeServiceProvider().shoppingItem("");
        if(shoppingItem!=null){
            shoppingItem.itemType();
            shoppingItem.purchaseType();
            //logger.warn("no item found");
        }

        String systemId = (String) params.get(OnAccess.SYSTEM_ID);
        Inventory inventory = this.platformGameServiceProvider.inventoryServiceProvider().inventory(systemId,"GameCurrency","Gold");
        if(inventory!=null) logger.warn(">>>>"+inventory.balance()+inventory.name());
        //platformGameServiceProvider.inventoryServiceProvider().inventory("s")
        String tid = UUID.randomUUID().toString();
        //params.put(OnAccess.STORE_TRANSACTION_ID,tid);
        //params.put(OnAccess.STORE_PRODUCT_ID,params.get(OnAccess.STORE_RECEIPT));
        //params.put(OnAccess.STORE_QUANTITY,1);
        //Transaction transaction = new Transaction();
        //transaction.index(tid);
        //transaction.owner((String)params.get(OnAccess.SYSTEM_ID));
        //dataStore.create(transaction);
        return true;
    }

}
