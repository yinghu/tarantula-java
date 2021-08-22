package com.tarantula.admin;

import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.Item;
import com.tarantula.platform.item.ItemContext;
import com.tarantula.platform.item.ItemQuery;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.util.SystemUtil;

import java.util.List;

public class GameItemAdminModule implements Module {
    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;
    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate onUpdate) throws Exception {
        if(session.action().equals("onList")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(query[0]);
            Descriptor app = gameCluster.serviceWithCategory(query[1]);
            ApplicationPreSetup preSetup = SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            List<Item> items = preSetup.list(this.context,app,new ItemQuery());
            session.write(new ItemContext(true,items).toJson().toString().getBytes());
        }
        else if (session.action().equals("onCreate")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            Item app = new Item();
            if(app.configureAndValidate(payload)){
                Descriptor desc = gameCluster.serviceWithCategory(app.configurationCategory());
                SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME)).save(this.context,desc,app);
                session.write(JsonUtil.toSimpleResponse(true,app.distributionKey()).getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"failed to save item").getBytes());
            }
        }
        else if(session.action().equals("onLoad")){

        }
        else if(session.action().equals("onUpdate")){

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
        this.context.log("game item admin module started", OnLog.WARN);
    }
}
