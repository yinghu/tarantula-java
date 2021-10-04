package com.tarantula.game.module;

import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.achievement.Achievement;
import com.tarantula.platform.achievement.ItemAchievementContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class AchievementModule implements Module,Configurable.Listener<Achievement> {
    private ApplicationContext context;
    private GameServiceProvider gameServiceProvider;
    private ConcurrentHashMap<String,Achievement> itemList;
    @Override
    public boolean onRequest(Session session, byte[] bytes, OnUpdate onUpdate) throws Exception {
        session.write(new ItemAchievementContext(true,"achievement list",toList()).toJson().toString().getBytes());

        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.itemList = new ConcurrentHashMap<>();
        this.gameServiceProvider = this.context.serviceProvider(context.descriptor().typeId());
        this.gameServiceProvider.achievementServiceProvider().registerConfigurableListener(this.context.descriptor(),this);
        this.context.log("achievement module started", OnLog.WARN);
    }
    @Override
    public void clear(){

    }
    public void onCreated(Achievement item){
        itemList.put(item.distributionKey(),item);
        this.context.log(item.toJson().toString(),OnLog.WARN);
    }
    private List<Achievement> toList(){
        ArrayList<Achievement> arrayList = new ArrayList<>();
        itemList.forEach((k,v)->arrayList.add(v));
        return arrayList;
    }

}
