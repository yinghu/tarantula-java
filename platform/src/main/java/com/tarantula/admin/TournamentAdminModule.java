package com.tarantula.admin;

import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.*;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.tournament.TournamentScheduleParser;

import java.util.List;

public class TournamentAdminModule implements Module {

    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;

    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        if(session.action().equals("onList")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            Descriptor app = gameCluster.serviceWithCategory(this.context.descriptor().category());
            ApplicationPreSetup preSetup = gameCluster.applicationPreSetup();//SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            List<ConfigurableObject> items = preSetup.list(app,new ConfigurableObjectQuery("typeId/"+app.category()));
            session.write(new ItemAdminContext(true,items.size()>0?"Configure tournament item":"no items configured",items).toJson().toString().getBytes());
        }
        else if(session.action().equals("onLoad")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(query[0]);
            Descriptor desc = gameCluster.serviceWithCategory(this.context.descriptor().category());
            ApplicationPreSetup preSetup = gameCluster.applicationPreSetup();//SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            Application app = new Application();
            app.distributionKey(query[1]);
            if(preSetup.load(desc,app)){
                session.write(app.toJson().toString().getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,query[1]+" not existed").getBytes());
            }
        }
        else if(session.action().equals("onRegister")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(query[0]);
            ApplicationPreSetup applicationPreSetup = gameCluster.applicationPreSetup();//SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            TournamentScheduleParser app = new TournamentScheduleParser();
            app.distributionKey(query[1]);
            Descriptor desc = gameCluster.serviceWithCategory(this.context.descriptor().category());
            boolean loaded = applicationPreSetup.load(desc,app);
            if(loaded&&(boolean)gameCluster.property(GameCluster.TOURNAMENT_ENABLED)){
                JsonObject config = JsonUtil.parse(payload);
                app.schedule(config.getAsJsonObject("application"));
                String serviceName = (String)gameCluster.property(GameCluster.GAME_SERVICE);
                GameServiceProvider tsp = this.context.serviceProvider(serviceName);
                tsp.tournamentServiceProvider().register(app);
                session.write(JsonUtil.toSimpleResponse(true,"tournament scheduled").getBytes());
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
        this.context.log("Tournament admin module started", OnLog.WARN);
    }
}
