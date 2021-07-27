package com.tarantula.admin;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Module;
import com.icodesoftware.OnLog;
import com.icodesoftware.Session;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.Item;

public class GameStoreAdminModule implements Module {
    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;
    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate onUpdate) throws Exception {
        if (session.action().equals("onSave")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            String serviceName = (String)gameCluster.property(GameCluster.GAME_SERVICE);
            GameServiceProvider gameServiceProvider = this.context.serviceProvider(serviceName);
            Item app = new Item();
            if(app.configureAndValidate(payload)){
                app.configurationType(this.context.descriptor().category());
                gameServiceProvider.register(app);
                session.write(JsonUtil.toSimpleResponse(true,serviceName).getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,serviceName).getBytes());
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
        this.context.log("game store admin module started", OnLog.WARN);
    }
}
