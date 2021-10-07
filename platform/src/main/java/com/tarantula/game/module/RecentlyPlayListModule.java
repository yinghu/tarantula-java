package com.tarantula.game.module;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.tarantula.game.service.GameServiceProvider;

import java.util.List;

public class RecentlyPlayListModule implements Module  {
    private ApplicationContext context;
    private GameServiceProvider gameServiceProvider;
    @Override
    public boolean onRequest(Session session, byte[] bytes, OnUpdate onUpdate) throws Exception {
        List<String> list = this.gameServiceProvider.presenceServiceProvider().recentlyPlayList();
        session.write(toJson(list).toString().getBytes());
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.gameServiceProvider = this.context.serviceProvider(this.context.descriptor().typeId());
        this.context.log("recently play list module started", OnLog.WARN);
    }
    @Override
    public void clear(){

    }
    private JsonObject toJson(List<String> list){
        JsonObject jsonObject = new JsonObject();
        JsonArray alist = new JsonArray();
        list.forEach(p->alist.add(p));
        jsonObject.add("onList",alist);
        return jsonObject;
    }
}
