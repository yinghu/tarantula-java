package com.tarantula.game.module;

import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.achievement.Achievement;
import com.tarantula.platform.achievement.PlatformAchievementServiceProvider;
import com.tarantula.platform.achievement.ItemAchievementContext;

public class AchievementModule implements Module,Configurable.Listener<Achievement> {
    private ApplicationContext context;
    private PlatformAchievementServiceProvider achievementServiceProvider;
    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        if(session.action().equals("onList")) {
            session.write(new ItemAchievementContext(true, "achievement list", this.achievementServiceProvider.list()).toJson().toString().getBytes());
        }
        else if(session.action().equals("onProgress")){
            session.write(JsonUtil.toSimpleResponse(true,"").getBytes());
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
        this.achievementServiceProvider = gameServiceProvider.achievementServiceProvider();
        this.achievementServiceProvider.registerConfigurableListener(this.context.descriptor(),this);
        gameServiceProvider.exportServiceModule(this.context.descriptor().tag(),this);
        this.context.log("Achievement module started->"+this.context.descriptor().tag(), OnLog.WARN);
    }
}
