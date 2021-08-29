package com.tarantula.game.module;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.item.Item;

import java.util.concurrent.ConcurrentHashMap;

public class GameStoreModule implements Module,Configurable.Listener<Item>{
    private ApplicationContext context;
    private GameServiceProvider gameServiceProvider;
    private ConcurrentHashMap<String,Item> itemList;
    @Override
    public boolean onRequest(Session session, byte[] bytes, OnUpdate onUpdate) throws Exception {
        if(session.action().equals("onList")){
            session.write(toJson().toString().getBytes());
        }
        if(session.action().equals("onBuy")){
            Item item = itemList.get(session.name());
            session.write(item.toJson().toString().getBytes());
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.itemList = new ConcurrentHashMap<>();
        this.gameServiceProvider = this.context.serviceProvider(context.descriptor().typeId());
        this.gameServiceProvider.configurationServiceProvider().registerConfigurableListener(this.context.descriptor(),this);
        this.context.log("game store module started", OnLog.WARN);
    }
    public void onCreated(Item item){
        itemList.put(item.distributionKey(),item);
        this.context.log(item.toJson().toString(),OnLog.WARN);
    }
    private JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",true);
        JsonArray alist = new JsonArray();
        itemList.forEach((k,v)->{
            alist.add(v.toJson());
        });
        jsonObject.add("itemList",alist);
        return jsonObject;
    }

}
