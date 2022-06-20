package com.tarantula.game.module;

import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.game.ItemContext;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GameItemModule implements Module,Configurable.Listener<ConfigurableObject>{
    private ApplicationContext context;
    private GameServiceProvider gameServiceProvider;
    private ConcurrentHashMap<String,ConfigurableObject> itemList;

    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        if(session.action().equals("onList")){
            List<ConfigurableObject> _item = this.gameServiceProvider.itemServiceProvider().list(this.context.descriptor(),session.name());
            session.write(new ItemContext(true,session.name(),_item).toString().getBytes());
        }
        else{
            throw new UnsupportedOperationException(session.action()+" not support");
        }

        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.itemList = new ConcurrentHashMap<>();
        this.gameServiceProvider = this.context.serviceProvider(context.descriptor().typeId());
        this.gameServiceProvider.itemServiceProvider().registerConfigurableListener(this.context.descriptor(),this);
        this.context.log("Game item module started", OnLog.WARN);
    }
    public void onCreated(ConfigurableObject item){
        itemList.put(item.distributionKey(),item);
        this.context.log(item.toJson().toString(),OnLog.WARN);
    }
}
