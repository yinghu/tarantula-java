package com.tarantula.game.module;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.item.ItemQuery;
import com.tarantula.platform.util.OnAccessDeserializer;


/**
 * Created by yinghu lu on 12/28/2020.
 */
public class ItemModule implements Module {
    private ApplicationContext context;
    private GameServiceProvider gameServiceProvider;
    private DataStore itemDataStore;
    private GsonBuilder builder;
    @Override
    public boolean onRequest(Session session, byte[] bytes, OnUpdate onUpdate) throws Exception {
        if(session.action().equals("onItems")){
            session.write(toItems(),label());
        }
        else if(session.action().equals("onRedeem")){
            OnAccess onAccess = builder.create().fromJson(new String(bytes),OnAccess.class);
            this.context.log(onAccess.property("itemId").toString(),OnLog.WARN);
            session.write(bytes,label());
        }
        else if(session.action().equals("onInventory")){
            session.write(toInventory(session.systemId()),label());
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.context = applicationContext;
        String itemDs = this.context.descriptor().typeId().replace("-","_")+"_item";
        itemDataStore = this.context.dataStore(itemDs);
        this.gameServiceProvider = this.context.serviceProvider(this.context.descriptor().typeId());
        this.context.log("started item module->"+this.context.descriptor().distributionKey(), OnLog.WARN);
    }

    @Override
    public String label() {
        return "item";
    }
    private byte[] toInventory(String systemId){
        JsonObject jsonObject = new JsonObject();
        JsonArray alist = new JsonArray();
        this.itemDataStore.list(new ItemQuery(systemId),(a)->{
            JsonObject jo = new JsonObject();
            jo.addProperty("name",a.name());
            jo.addProperty("category",a.category());
            jo.addProperty("id",a.id());
            alist.add(jo);
            return true;
        });
        jsonObject.add("items",alist);
        return jsonObject.toString().getBytes();
    }
    private byte[] toItems(){
        JsonObject jsonObject = new JsonObject();
        JsonArray alist = new JsonArray();
        this.gameServiceProvider.dataStore().list(new ItemQuery(this.context.descriptor().distributionKey()),(a)->{
            JsonObject jo = new JsonObject();
            jo.addProperty("name",a.name());
            jo.addProperty("category",a.category());
            jo.addProperty("id",a.id());
            alist.add(jo);
            return true;
        });
        jsonObject.add("items",alist);
        return jsonObject.toString().getBytes();
    }
}
