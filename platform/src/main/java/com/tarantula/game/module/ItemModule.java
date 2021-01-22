package com.tarantula.game.module;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.tarantula.game.service.GameServiceProvider;

/**
 * Created by yinghu lu on 12/28/2020.
 */
public class ItemModule implements Module {
    private ApplicationContext context;
    private GameServiceProvider gameServiceProvider;
    private DataStore itemDataStore;
    @Override
    public boolean onRequest(Session session, byte[] bytes, OnUpdate onUpdate) throws Exception {
        if(session.action().equals("onItems")){

        }
        else if(session.action().equals("onItemPacks")){

        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        String itemDs = this.context.descriptor().typeId().replace("-","_")+"_item";
        itemDataStore = this.context.dataStore(itemDs);
        this.gameServiceProvider = this.context.serviceProvider(this.context.descriptor().typeId());
        this.context.log("started item module->"+itemDs, OnLog.WARN);
    }

    @Override
    public String label() {
        return "item";
    }
}
