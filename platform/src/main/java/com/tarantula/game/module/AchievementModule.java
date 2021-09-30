package com.tarantula.game.module;

import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.item.Application;

public class AchievementModule implements Module,Configurable.Listener<Application> {
    private ApplicationContext context;
    private GameServiceProvider gameServiceProvider;
    @Override
    public boolean onRequest(Session session, byte[] bytes, OnUpdate onUpdate) throws Exception {
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.gameServiceProvider = this.context.serviceProvider(context.descriptor().typeId());
        this.gameServiceProvider.configurationServiceProvider().registerConfigurableListener(this.context.descriptor(),this);
        this.context.log("achievement module started", OnLog.WARN);
    }
    @Override
    public void clear(){

    }
    public void onCreated(Application item){
        //itemList.put(item.distributionKey(),item);
        this.context.log(item.toJson().toString(),OnLog.WARN);
    }

}
