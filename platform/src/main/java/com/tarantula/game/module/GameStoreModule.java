package com.tarantula.game.module;

import com.google.gson.GsonBuilder;
import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.AccessControl;
import com.tarantula.platform.inventory.PlatformInventoryServiceProvider;
import com.tarantula.platform.store.ShoppingItem;
import com.tarantula.platform.store.PlatformStoreServiceProvider;
import com.tarantula.platform.store.StorePurchase;
import com.tarantula.platform.util.OnAccessDeserializer;

import java.util.HashMap;
import java.util.Map;

public class GameStoreModule implements Module,Configurable.Listener<ShoppingItem>{
    private ApplicationContext context;
    private PlatformStoreServiceProvider storeServiceProvider;
    private PlatformInventoryServiceProvider inventoryServiceProvider;
    private PlatformGameServiceProvider gameServiceProvider;
    private GsonBuilder builder;
    private String serviceTypeId;
    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        if(session.action().equals("onList")){
            Map<String,Object> params = new HashMap<>();
            params.put(OnAccess.SYSTEM_ID,session.systemId());
            params.put(OnAccess.TYPE_ID,serviceTypeId);
            params.put(OnAccess.PROVIDER,OnAccess.APPLICATION_STORE);
            params.put(OnAccess.STORE_BUNDLE_ID,"BUNDLE_ID");
            this.context.validator().validateToken(params);
            session.write(this.storeServiceProvider.shop(session.name()).toJson().toString().getBytes());
        }
        else if(session.action().equals("onValidate")){
            OnAccess acc = builder.create().fromJson(new String(session.payload()).trim(),OnAccess.class);
            Map<String,Object> params = acc.toMap();
            String bundleId = (String) params.get(OnAccess.STORE_BUNDLE_ID);
            ShoppingItem shoppingItem = this.storeServiceProvider.shoppingItem(bundleId);
            if(shoppingItem==null) throw new RuntimeException("shopping item not existed");
            params.put(OnAccess.SYSTEM_ID,session.systemId());
            params.put(OnAccess.TYPE_ID,serviceTypeId);
            params.put(OnAccess.STORE_PRODUCT_ID,shoppingItem.skuName());
            if(this.context.validator().validateToken(params)){
                String sku = (String) params.get(OnAccess.STORE_PRODUCT_ID);
                if(shoppingItem.skuName().equals(sku)){
                    this.storeServiceProvider.grant(session.systemId(),shoppingItem.distributionKey());
                    StorePurchase storePurchase = new StorePurchase();
                    storePurchase.transactionId = (String) params.get(OnAccess.STORE_TRANSACTION_ID);
                    storePurchase.inventoryList = inventoryServiceProvider.inventoryList(session.systemId());
                    session.write(storePurchase.toJson().toString().getBytes());
                }
                else{
                    session.write(JsonUtil.toSimpleResponse(false,"Bundle store name not matched").getBytes());
                }
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,(String)params.get(OnAccess.STORE_MESSAGE)).getBytes());
            }
        }
        else if(session.action().equals("onTestWithAdmin")){
            if(this.context.validator().role(session.systemId()).accessControl()< AccessControl.admin.accessControl()){
                throw new RuntimeException("no permission");
            }
            ShoppingItem shoppingItem = this.storeServiceProvider.shoppingItem(session.name());
            if(shoppingItem==null) throw new RuntimeException("shopping item not existed");
            this.storeServiceProvider.grant(session.systemId(),shoppingItem.distributionKey());
            StorePurchase storePurchase = new StorePurchase();
            storePurchase.transactionId = session.name();
            storePurchase.inventoryList = inventoryServiceProvider.inventoryList(session.systemId());
            session.write(storePurchase.toJson().toString().getBytes());
        }
        else{
            throw new UnsupportedOperationException(session.action()+" not support");
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.gameServiceProvider = this.context.serviceProvider(this.context.descriptor().typeId());
        this.storeServiceProvider = gameServiceProvider.storeServiceProvider();
        this.storeServiceProvider.registerConfigurableListener(this.context.descriptor(),this);
        this.inventoryServiceProvider = gameServiceProvider.inventoryServiceProvider();
        this.serviceTypeId = this.context.descriptor().typeId().replace("-service","");
        this.gameServiceProvider.exportServiceModule(this.context.descriptor().tag(),this);
        this.context.log("Game store module started with ["+serviceTypeId+"]", OnLog.WARN);
    }
}
