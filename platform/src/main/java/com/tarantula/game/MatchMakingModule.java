package com.tarantula.game;

import com.tarantula.ApplicationContext;
import com.tarantula.Module;
import com.tarantula.OnLog;
import com.tarantula.Session;

public class MatchMakingModule implements Module {

    private ApplicationContext context;
    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        //check Rating to match the game zone to join 
        //context.presence(session.systemId()).onPlay(session,)
        return false;
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        context.log("Started match making module", OnLog.WARN);
    }

    @Override
    public String label() {
        return "matchmaking";
    }
}
