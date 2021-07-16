package com.tarantula.game.module;

import com.icodesoftware.*;
import com.icodesoftware.Module;

public class TournamentModule implements Module , Tournament.Listener {
    private ApplicationContext context;
    @Override
    public boolean onRequest(Session session, byte[] bytes, OnUpdate onUpdate) throws Exception {
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.context.log("tournament module started", OnLog.WARN);
    }
    public void tournamentStarted(Tournament tournament){}
    public void tournamentClosed(Tournament tournament){}
    public void tournamentEnded(Tournament tournament){}
}
