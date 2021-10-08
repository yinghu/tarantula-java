package com.tarantula.game.module;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.GameServiceProvider;

public class DailyGiveawayModule implements Module, Configurable.Listener {
    private ApplicationContext context;
    private GameServiceProvider gameServiceProvider;

    @Override
    public boolean onRequest(Session session, byte[] bytes, OnUpdate onUpdate) throws Exception {
        if(session.action().equals("onRedeem")){
            this.gameServiceProvider.presenceServiceProvider().redeem(session.systemId());
            session.write(JsonUtil.toSimpleResponse(true,session.name()).getBytes());
        }
        else{
            throw new UnsupportedOperationException(session.action());
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.gameServiceProvider = this.context.serviceProvider(context.descriptor().typeId());
        this.gameServiceProvider.presenceServiceProvider().registerConfigurableListener(this.context.descriptor(),this);
        this.context.log("Daily Giveaway module started", OnLog.WARN);
    }
    @Override
    public void clear(){

    }

}
