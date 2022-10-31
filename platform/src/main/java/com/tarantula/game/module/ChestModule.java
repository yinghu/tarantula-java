package com.tarantula.game.module;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Module;
import com.icodesoftware.OnLog;
import com.icodesoftware.Session;
import com.tarantula.game.service.GameServiceProvider;

import java.util.List;

public class ChestModule implements Module {

    private ApplicationContext context;
    private GameServiceProvider gameServiceProvider;

    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        if(session.action().equals("onFriendList")){
            List<String> list = this.gameServiceProvider.presenceServiceProvider().friendList(session.systemId());
            session.write(toJson(list).toString().getBytes());
        }
        else if(session.action().equals("addFriend")){
            this.gameServiceProvider.presenceServiceProvider().onFriendList(session.systemId(),session.name());
            List<String> list = this.gameServiceProvider.presenceServiceProvider().friendList(session.systemId());
            session.write(toJson(list).toString().getBytes());
        }
        else{
            throw new UnsupportedOperationException(session.action());
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.gameServiceProvider = this.context.serviceProvider(this.context.descriptor().typeId());
        this.context.log("Chest module started", OnLog.WARN);
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
