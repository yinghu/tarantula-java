package com.tarantula.game.module;

import com.icodesoftware.*;
import com.icodesoftware.service.TournamentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.tournament.TournamentContext;


public class TournamentModule extends ModuleHeader implements Configurable.Listener {

    private TournamentServiceProvider tournamentServiceProvider;

    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        if(session.action().equals("onList")){
            session.write(new TournamentContext(true,"tournament list",this.tournamentServiceProvider.list(session.name())).toJson().toString().getBytes());
        }
        else if(session.action().equals("onBoard")){
            Tournament tournament = tournamentServiceProvider.tournament(Long.parseLong(session.name()));
            if(tournament.status() == Tournament.Status.STARTING || tournament.status() == Tournament.Status.PENDING){
                session.write(JsonUtil.toSimpleResponse(false,"tournament not started").getBytes());
            }
            else{
                Tournament.Instance instance = tournament.register(session);
                Tournament.RaceBoard board = instance.raceBoard();
                Tournament.RaceBoard myBoard = instance.myRaceBoard();
                session.write(new TournamentContext(board,myBoard).toJson().toString().getBytes());
            }
        }
        else{
            throw new UnsupportedOperationException(session.action()+" not supported");
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        super.setup(applicationContext);
        this.tournamentServiceProvider = gameServiceProvider.tournamentServiceProvider();
        this.tournamentServiceProvider.registerConfigurableListener(this.context.descriptor(),this);
        this.context.log("Tournament module started", OnLog.WARN);
    }

}
