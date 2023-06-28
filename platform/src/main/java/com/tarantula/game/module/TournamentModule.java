package com.tarantula.game.module;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.TournamentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.tournament.TournamentContext;
import com.tarantula.platform.tournament.TournamentHistoryContext;

public class TournamentModule implements Module , Tournament.Listener,Configurable.Listener {
    private ApplicationContext context;
    private TournamentServiceProvider tournamentServiceProvider;
    private PlatformGameServiceProvider gameServiceProvider;
    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        if(session.action().equals("onList")){
            session.write(new TournamentContext(true,"tournament list",this.tournamentServiceProvider.list()).toJson().toString().getBytes());
        }
        else if(session.action().equals("onPlayerHistory")){
            session.write(new TournamentHistoryContext(true,"player tournament list",this.tournamentServiceProvider.playerHistory(session.systemId())).toString().getBytes());
        }
        else if(session.action().equals("onTournamentHistory")){
            Tournament.Instance ht = this.tournamentServiceProvider.tournamentHistory(session.name());
            if(ht!=null){
                session.write(ht.toJson().toString().getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"tournament not existed->"+session.name()).getBytes());
            }
        }
        else{
            throw new UnsupportedOperationException(session.action()+" not supported");
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        gameServiceProvider = context.serviceProvider(context.descriptor().typeId());
        this.tournamentServiceProvider = gameServiceProvider.tournamentServiceProvider();
        this.tournamentServiceProvider.registerTournamentListener(this);
        this.tournamentServiceProvider.registerConfigurableListener(this.context.descriptor(),this);
        //this.gameServiceProvider.exportServiceModule(this.context.descriptor().tag(),this);
        this.context.log("Tournament module started", OnLog.WARN);
    }

    @Override
    public void tournamentStarted(Tournament tournament) {
        this.context.log(tournament.distributionKey()+" STARTED",OnLog.WARN);
    }

    @Override
    public void tournamentClosed(Tournament tournament) {
        this.context.log(tournament.distributionKey()+" CLOSED",OnLog.WARN);
    }

    @Override
    public void tournamentEnded(Tournament tournament) {
        this.context.log(tournament.distributionKey()+" ENDED",OnLog.WARN);
    }

    public Descriptor descriptor(){
        return this.context.descriptor();
    }

}
