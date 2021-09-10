package com.tarantula.game.module;

import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.Item;
import com.tarantula.platform.item.ItemContext;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GameItemModule implements Module,Configurable.Listener<ConfigurableObject>{
    private ApplicationContext context;
    private GameServiceProvider gameServiceProvider;
    private ConcurrentHashMap<String,ConfigurableObject> itemList;

    @Override
    public boolean onRequest(Session session, byte[] bytes, OnUpdate onUpdate) throws Exception {
        if(session.action().equals("onList")){
            List<ConfigurableObject> _item = this.gameServiceProvider.configurationServiceProvider().list(this.context.descriptor(),session.name());
            session.write(new ItemContext(true,session.name(),_item).toString().getBytes());
        }
        else if(session.action().equals("onLoad")){

        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.itemList = new ConcurrentHashMap<>();
        this.gameServiceProvider = this.context.serviceProvider(context.descriptor().typeId());
        this.gameServiceProvider.configurationServiceProvider().registerConfigurableListener(this.context.descriptor(),this);
        this.context.log("game configuration module started", OnLog.WARN);
    }
    public void onCreated(ConfigurableObject item){
        itemList.put(item.distributionKey(),item);
        this.context.log(item.toJson().toString(),OnLog.WARN);
    }
}
