package com.tarantula.game.module;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.presence.dailygiveaway.DailyLoginTrack;
import com.tarantula.platform.presence.dailygiveaway.ItemDailyGiveawayContext;
import com.tarantula.platform.presence.dailygiveaway.PlatformDailyGiveawayServiceProvider;

public class DailyGiveawayModule implements Module, Configurable.Listener {
    private ApplicationContext context;
    private PlatformDailyGiveawayServiceProvider dailyGiveawayServiceProvider;

    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        if(session.action().equals("onList")){
            session.write(new ItemDailyGiveawayContext(true, "daily giveaway list", this.dailyGiveawayServiceProvider.list()).toJson().toString().getBytes());
        }
        else if(session.action().equals("onClaim")){
            DailyLoginTrack rewarded = this.dailyGiveawayServiceProvider.claim(session);
            session.write(rewarded!=null?rewarded.toJson().toString().getBytes():JsonUtil.toSimpleResponse(false,"no reward").getBytes());
        }
        else{
            throw new UnsupportedOperationException(session.action());
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        PlatformGameServiceProvider gameServiceProvider = this.context.serviceProvider(context.descriptor().typeId());
        this.dailyGiveawayServiceProvider = gameServiceProvider.dailyGiveawayServiceProvider();
        this.dailyGiveawayServiceProvider.registerConfigurableListener(this.context.descriptor(),this);
        this.context.log("Daily Giveaway module started", OnLog.WARN);
    }

    public Descriptor descriptor(){
        return this.context.descriptor();
    }
}
