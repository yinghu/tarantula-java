package com.tarantula.game.module;

import com.icodesoftware.*;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.service.TournamentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.AccessControl;
import com.tarantula.platform.tournament.PlatformTournamentServiceProvider;
import com.tarantula.platform.tournament.TournamentContext;


public class TournamentModule extends ModuleHeader implements Configurable.Listener {

    private PlatformTournamentServiceProvider tournamentServiceProvider;
    private TokenValidatorProvider tokenValidatorProvider;
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
        else if(session.action().equals("onEvent")){
            TokenValidatorProvider.AuthVendor download = tokenValidatorProvider.authVendor(OnAccess.DOWNLOAD_CENTER);
            byte[] payload = download.download(gameServiceProvider.gameCluster().typeId()+"#"+session.name());
            session.write(payload);
        }
        else if(session.action().equals("onScan")){
            if(this.context.validator().role(session.distributionId()).accessControl() < AccessControl.admin.accessControl()){
                throw new RuntimeException("no permission");
            }
            Response response = tournamentServiceProvider.verifyTournamentStatusOnCluster(session.name());
            session.write(response.toBinary());
        }
        else{
            throw new UnsupportedOperationException(session.action()+" not supported");
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        super.setup(applicationContext);
        tokenValidatorProvider = applicationContext.serviceProvider(TokenValidatorProvider.NAME);
        this.tournamentServiceProvider = gameServiceProvider.tournamentServiceProvider();
        this.tournamentServiceProvider.registerConfigurableListener(this.context.descriptor(),this);
        this.context.log("Tournament module started", OnLog.WARN);
    }

}
