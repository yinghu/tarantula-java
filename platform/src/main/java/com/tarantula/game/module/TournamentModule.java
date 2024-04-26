package com.tarantula.game.module;

import com.icodesoftware.*;
import com.icodesoftware.service.TournamentServiceProvider;
import com.tarantula.platform.tournament.TournamentMockUtils;
import com.tarantula.platform.tournament.TournamentContext;


public class TournamentModule extends ModuleHeader implements Configurable.Listener {

    private TournamentServiceProvider tournamentServiceProvider;

    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        if(session.action().equals("onList")){
            session.write(new TournamentContext(true,"tournament list",this.tournamentServiceProvider.list()).toJson().toString().getBytes());
        }

        else if(session.action().equals("onBoard")){
            Tournament tournament = tournamentServiceProvider.tournament(Long.parseLong(session.name()));
            Tournament.RaceBoard board = tournament.register(session).raceBoard();
            session.write(board.toJson().toString().getBytes());
        }
        else if (session.action().equals("onLoadRankings")){
            session.write(TournamentMockUtils.GetMockTournamentRankings(Long.parseLong(session.systemId())).toString().getBytes());
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
