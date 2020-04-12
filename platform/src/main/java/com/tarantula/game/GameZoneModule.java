package com.tarantula.game;

import com.tarantula.ApplicationContext;
import com.tarantula.Module;
import com.tarantula.OnLog;
import com.tarantula.Session;
import com.tarantula.platform.util.SystemUtil;

import java.util.concurrent.ConcurrentHashMap;

public class GameZoneModule implements Module {

    private ApplicationContext context;
    private ConcurrentHashMap<Integer,Arena> mArena = new ConcurrentHashMap<>();
    @Override
    public void onJoin(Session session,OnUpdate onUpdate) throws Exception{
        //match arena with rating rank/xp
        Rating rating = new Rating();
        rating.fromMap(SystemUtil.toMap(session.payload()));
        context.log("join on ["+this.context.descriptor().accessRank()+"/"+rating.level+"]",OnLog.WARN);
        Arena arena = mArena.get(rating.level);
        session.write(SystemUtil.toJson(arena.toMap()),label());
    }
    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        //context.log(this.context.descriptor().tag()+"/"+this.context.descriptor().accessRank(),OnLog.WARN);
        session.write(payload,label());

        return false;
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        mArena.put(1,new Arena());
        context.log(this.context.descriptor().tag()+"/"+this.context.descriptor().accessRank(),OnLog.WARN);
    }
    @Override
    public void onTimer(OnUpdate update){
        //context.log("On timer->",OnLog.WARN);
        //update.on();
    }
    @Override
    public String label() {
        return "game";
    }
}
