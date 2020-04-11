package com.tarantula.game;

import com.tarantula.ApplicationContext;
import com.tarantula.Module;
import com.tarantula.OnLog;
import com.tarantula.Session;

public class GameZoneModule implements Module {

    private ApplicationContext context;

    @Override
    public void onJoin(Session session,OnUpdate onUpdate) throws Exception{
        //match arena with rating rank/xp
        session.write("{}".getBytes(),label());
    }
    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        context.log(this.context.descriptor().typeId(),OnLog.WARN);
        session.write(payload,label());

        return false;
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        context.log("Started game module", OnLog.WARN);
    }

    @Override
    public String label() {
        return "game";
    }
}
