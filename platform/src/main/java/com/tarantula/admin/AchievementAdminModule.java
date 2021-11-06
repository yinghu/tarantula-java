package com.tarantula.admin;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.achievement.Achievement;
import com.tarantula.platform.item.*;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.util.SystemUtil;

import java.util.List;

public class AchievementAdminModule implements Module {
    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;
    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        if(session.action().equals("onList")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            Descriptor app = gameCluster.serviceWithCategory(this.context.descriptor().category());
            ApplicationPreSetup preSetup = SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            List<ConfigurableHeader> items = preSetup.list(this.context,app,new ConfigurableHeaderQuery("category/"+app.category()));
            session.write(new ItemHeaderContext(true,items.size()>0?"Configure achievement item":"no items configured",items).toJson().toString().getBytes());
        }
        else if (session.action().equals("onRegister")){
            String[] ks = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(ks[0]);
            Achievement app = new Achievement();
            app.distributionKey(ks[1]);
            Descriptor desc = gameCluster.serviceWithCategory(this.context.descriptor().category());
            if(SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME)).load(context,desc,app)){
                session.write(JsonUtil.toSimpleResponse(true,ks[1]).getBytes());
                GameServiceProvider gameServiceProvider = this.context.serviceProvider((String) gameCluster.property(GameCluster.GAME_SERVICE));
                gameServiceProvider.achievementServiceProvider().register(app.setup());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"failed to register achievement item").getBytes());
            }
        }
        else if (session.action().equals("onRelease")){
            String[] ks = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(ks[0]);
            Achievement app = new Achievement();
            app.distributionKey(ks[1]);
            Descriptor desc = gameCluster.serviceWithCategory(this.context.descriptor().category());
            if(SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME)).load(context,desc,app)){
                session.write(JsonUtil.toSimpleResponse(true,ks[1]).getBytes());
                GameServiceProvider gameServiceProvider = this.context.serviceProvider((String) gameCluster.property(GameCluster.GAME_SERVICE));
                gameServiceProvider.achievementServiceProvider().release(app.setup());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"failed to register achievement item").getBytes());
            }
        }
        else {
            throw new UnsupportedOperationException(session.action()+" not supported");
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.deploymentServiceProvider = context.serviceProvider(DeploymentServiceProvider.NAME);
        this.context.log("Achievement admin module started", OnLog.WARN);
    }
}
