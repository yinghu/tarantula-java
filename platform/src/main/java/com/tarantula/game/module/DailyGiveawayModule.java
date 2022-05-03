package com.tarantula.game.module;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.presence.ItemDailyGiveawayContext;
import com.tarantula.platform.presence.PlatformPresenceServiceProvider;

public class DailyGiveawayModule implements Module, Configurable.Listener {
    private ApplicationContext context;
    private PlatformPresenceServiceProvider presenceServiceProvider;

    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        if(session.action().equals("onList")){
            session.write(new ItemDailyGiveawayContext(true, "daily giveaway list", this.presenceServiceProvider.list()).toJson().toString().getBytes());
        }
        else if(session.action().equals("onRedeem")){
            boolean rewarded = this.presenceServiceProvider.redeem(session.systemId(),session.name());
            session.write(JsonUtil.toSimpleResponse(rewarded,session.name()).getBytes());
        }
        else{
            throw new UnsupportedOperationException(session.action());
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        GameServiceProvider gameServiceProvider = this.context.serviceProvider(context.descriptor().typeId());
        this.presenceServiceProvider = gameServiceProvider.presenceServiceProvider();
        this.presenceServiceProvider.registerConfigurableListener(this.context.descriptor(),this);
        this.context.log("Daily Giveaway module started", OnLog.WARN);
    }
}
