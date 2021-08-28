package com.tarantula.admin;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.ApplicationConfiguration;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.Item;
import com.tarantula.platform.item.ItemContext;
import com.tarantula.platform.item.ItemQuery;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.util.SystemUtil;

import java.util.List;

public class AchievementAdminModule implements Module {
    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;
    @Override
    public boolean onRequest(Session session, byte[] bytes, OnUpdate onUpdate) throws Exception {
        if(session.action().equals("onList")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            Descriptor app = gameCluster.serviceWithCategory(this.context.descriptor().category());
            ApplicationPreSetup preSetup = SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            List<Item> items = preSetup.list(this.context,app,new ItemQuery());
            session.write(new ItemContext(true,items.size()>0?"Configure achievement item":"no items configured",items).toJson().toString().getBytes());
        }
        else if (session.action().equals("onSave")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            String serviceName = (String)gameCluster.property(GameCluster.GAME_SERVICE);
            GameServiceProvider gameServiceProvider = this.context.serviceProvider(serviceName);
            ApplicationConfiguration app = new ApplicationConfiguration();
            app.configurationType(this.context.descriptor().category());
            gameServiceProvider.configurationServiceProvider().register(app);
            session.write(JsonUtil.toSimpleResponse(true,serviceName).getBytes());
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
