package com.tarantula.game.module;

import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.service.TournamentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.util.MockUtils;
import com.tarantula.platform.tournament.TournamentContext;
import com.tarantula.platform.tournament.TournamentHistoryContext;
import com.tarantula.platform.tournament.TournamentRanking;

import java.util.ArrayList;
import java.util.List;


public class TournamentModule extends ModuleHeader implements Configurable.Listener {

    private TournamentServiceProvider tournamentServiceProvider;

    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        if(session.action().equals("onList")){
            session.write(new TournamentContext(true,"tournament list",this.tournamentServiceProvider.list()).toJson().toString().getBytes());
        }
        else if(session.action().equals("onJoin")){
            Tournament.Instance ins = tournamentServiceProvider.tournament(Long.parseLong(session.name())).register(session);
            session.write(ins.toJson().toString().getBytes());
        }
        else if(session.action().equals("onBoard")){ // TODO: Clean this up once we have tournaments working
            Tournament tournament = tournamentServiceProvider.tournament(Long.parseLong(session.name()));
            Tournament.RaceBoard board = tournament.register(session).raceBoard();
            session.write(board.toString().getBytes());
        }
        else if (session.action().equals("onLoadRankings")){

            List<TournamentRanking> allRankings = new ArrayList<>();
            JsonObject jsonObject = JsonUtil.parse(session.name());
            this.context.log(jsonObject.getAsString(), OnLog.DEBUG);
            List<Long> tournamentIdList = new ArrayList<Long>();

            long playerID = session.distributionId();

            for(long tournamentID : tournamentIdList){
                Tournament tournament = tournamentServiceProvider.tournament(tournamentID);
                Tournament.RaceBoard board = tournament.register(session).raceBoard();
                allRankings.add(new TournamentRanking(board, playerID));
            }

            session.write(allRankings.toString().getBytes());
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
