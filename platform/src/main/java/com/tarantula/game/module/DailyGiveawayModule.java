package com.tarantula.game.module;

import com.icodesoftware.*;

import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.presence.dailygiveaway.DailyLoginTrack;
import com.tarantula.platform.presence.dailygiveaway.ItemDailyGiveawayContext;
import com.tarantula.platform.presence.dailygiveaway.PlatformDailyGiveawayServiceProvider;

public class DailyGiveawayModule extends ModuleHeader implements Configurable.Listener {

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
        super.setup(applicationContext);
        this.dailyGiveawayServiceProvider = gameServiceProvider.dailyGiveawayServiceProvider();
        this.dailyGiveawayServiceProvider.registerConfigurableListener(this.context.descriptor(),this);
        this.context.log("Daily Giveaway module started", OnLog.WARN);
    }

}
