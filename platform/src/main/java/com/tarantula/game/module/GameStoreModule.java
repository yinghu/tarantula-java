package com.tarantula.game.module;

import com.google.gson.GsonBuilder;
import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.inventory.PlatformInventoryServiceProvider;
import com.tarantula.platform.store.ShoppingItem;
import com.tarantula.platform.store.ShoppingItemContext;
import com.tarantula.platform.store.PlatformStoreServiceProvider;
import com.tarantula.platform.store.StorePurchase;
import com.tarantula.platform.util.OnAccessDeserializer;

import java.util.List;
import java.util.Map;

public class GameStoreModule implements Module,Configurable.Listener<ShoppingItem>{
    private ApplicationContext context;
    private PlatformStoreServiceProvider storeServiceProvider;
    private PlatformInventoryServiceProvider inventoryServiceProvider;
    private GsonBuilder builder;
    private String serviceTypeId;
    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        if(session.action().equals("onList")){
            session.write(new ShoppingItemContext(true,"shop list",this.storeServiceProvider.list()).toJson().toString().getBytes());
        }
        else if(session.action().equals("onValidate")){
            OnAccess acc = builder.create().fromJson(new String(session.payload()).trim(),OnAccess.class);
            Map<String,Object> params = acc.toMap();
            params.put("systemId",session.systemId());
            params.put("serviceTypeId",serviceTypeId);
            if(this.context.validator().validateToken(params)){
                List<ShoppingItem> shoppingItemList = this.storeServiceProvider.list();
                String[] itemId ={""};
                String sku = (String) params.get(OnAccess.STORE_PRODUCT_ID);
                shoppingItemList.forEach((si)->{
                    if(si.configurationName().equals(sku)) itemId[0] = si.distributionKey();
                });
                this.storeServiceProvider.grant(session.systemId(),itemId[0]);
                StorePurchase storePurchase = new StorePurchase();
                storePurchase.transactionId = (String) params.get(OnAccess.STORE_TRANSACTION_ID);
                storePurchase.inventoryList = inventoryServiceProvider.inventoryList(session.systemId());
                session.write(storePurchase.toJson().toString().getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,(String)params.get(OnAccess.STORE_MESSAGE)).getBytes());
            }
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        GameServiceProvider gameServiceProvider = this.context.serviceProvider(this.context.descriptor().typeId());
        this.storeServiceProvider = gameServiceProvider.storeServiceProvider();
        this.storeServiceProvider.registerConfigurableListener(this.context.descriptor(),this);
        this.inventoryServiceProvider = gameServiceProvider.inventoryServiceProvider();
        this.serviceTypeId = this.context.descriptor().typeId();
        this.context.log("Game store module started with ["+serviceTypeId+"]", OnLog.WARN);
    }
}
