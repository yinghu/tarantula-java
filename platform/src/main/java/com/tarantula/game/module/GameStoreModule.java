package com.tarantula.game.module;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ItemApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GameStoreModule implements Module,Configurable.Listener<Application>{
    private ApplicationContext context;
    private GameServiceProvider gameServiceProvider;
    private ConcurrentHashMap<String,Application> itemList;
    @Override
    public boolean onRequest(Session session, byte[] bytes, OnUpdate onUpdate) throws Exception {
        if(session.action().equals("onList")){
            session.write(new ItemApplicationContext(true,"shop list",toList()).toJson().toString().getBytes());
        }
        if(session.action().equals("onBuy")){
            Application item = itemList.get(session.name());
            if(item!=null&&this.gameServiceProvider.inventoryServiceProvider().redeem(session.systemId(),item)){
                session.write(JsonUtil.toSimpleResponse(true,"inventory redeemed").getBytes());
            }else{
                session.write(JsonUtil.toSimpleResponse(false,"item not existed->"+session.name()).getBytes());
            }
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
    public void onCreated(Application item){
        itemList.put(item.distributionKey(),item);
        this.context.log(item.toJson().toString(),OnLog.WARN);
    }
    private List<Application> toList(){
        ArrayList<Application> arrayList = new ArrayList<>();
        itemList.forEach((k,v)->arrayList.add(v));
        return arrayList;
    }

}
