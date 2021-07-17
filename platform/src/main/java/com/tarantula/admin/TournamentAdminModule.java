package com.tarantula.admin;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.TournamentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.tournament.TournamentScheduleParser;

public class TournamentAdminModule implements Module {

    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate onUpdate) throws Exception {
        if(session.action().equals("onList")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            session.write(JsonUtil.toSimpleResponse(true,(String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME)).getBytes());
        }
        else if(session.action().equals("onSchedule")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            if((boolean)gameCluster.property(GameCluster.TOURNAMENT_ENABLED)){
                Tournament.Schedule schedule = TournamentScheduleParser.parse(payload);
                String serviceName = (String)gameCluster.property(GameCluster.GAME_SERVICE);
                TournamentServiceProvider tsp = this.context.serviceProvider(serviceName);
                tsp.register(schedule);
                session.write(JsonUtil.toSimpleResponse(true,serviceName).getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"tournament not supported").getBytes());
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
        this.deploymentServiceProvider = context.serviceProvider(DeploymentServiceProvider.NAME);
        this.context.log("tournament admin module started", OnLog.WARN);
    }
}
