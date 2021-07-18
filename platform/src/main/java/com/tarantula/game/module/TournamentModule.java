package com.tarantula.game.module;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.TournamentServiceProvider;

public class TournamentModule implements Module , Tournament.Listener {
    private ApplicationContext context;
    private TournamentServiceProvider gameServiceProvider;
    private String regKey;
    @Override
    public boolean onRequest(Session session, byte[] bytes, OnUpdate onUpdate) throws Exception {
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.gameServiceProvider = context.serviceProvider(context.descriptor().typeId());
        regKey = this.gameServiceProvider.registerTournamentListener(this);
        this.context.log("tournament module started", OnLog.WARN);
    }
    @Override
    public void clear(){
        this.gameServiceProvider.unregisterTournamentListener(regKey);
    }
    public void tournamentStarted(Tournament tournament){
        this.context.log("tournament started->"+tournament.type(),OnLog.WARN);
    }
    public void tournamentClosed(Tournament tournament){}
    public void tournamentEnded(Tournament tournament){}
}
