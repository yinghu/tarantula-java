package com.tarantula.admin;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.Item;
import com.tarantula.platform.item.ItemContext;
import com.tarantula.platform.item.ConfigurableObjectQuery;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.tournament.TournamentScheduleParser;
import com.tarantula.platform.util.SystemUtil;

import java.util.List;

public class TournamentAdminModule implements Module {

    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate onUpdate) throws Exception {
        if(session.action().equals("onList")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            Descriptor app = gameCluster.serviceWithCategory(this.context.descriptor().category());
            ApplicationPreSetup preSetup = SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            List<ConfigurableObject> items = preSetup.list(this.context,app,new ConfigurableObjectQuery());
            session.write(new ItemContext(true,items.size()>0?"Configure tournament item":"no items configured",items).toJson().toString().getBytes());
        }
        else if(session.action().equals("onRegister")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(query[0]);
            ApplicationPreSetup applicationPreSetup = SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            TournamentScheduleParser app = new TournamentScheduleParser();
            app.distributionKey(query[1]);
            Descriptor desc = gameCluster.serviceWithCategory(this.context.descriptor().category());
            boolean loaded = applicationPreSetup.load(context,desc,app);
            if(loaded&&(boolean)gameCluster.property(GameCluster.TOURNAMENT_ENABLED)){
                Tournament.Schedule schedule = app.parse();
                String serviceName = (String)gameCluster.property(GameCluster.GAME_SERVICE);
                GameServiceProvider tsp = this.context.serviceProvider(serviceName);
                Tournament tournament = tsp.tournamentServiceProvider().register(schedule);
                session.write(JsonUtil.toSimpleResponse(true,tournament.distributionKey()).getBytes());
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
