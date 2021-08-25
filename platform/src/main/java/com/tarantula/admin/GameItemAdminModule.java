package com.tarantula.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.Item;
import com.tarantula.platform.util.SystemUtil;

public class GameItemAdminModule implements Module {
    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;
    private static String TEMPLATE_SUFFIX = "-game-item-settings";
    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate onUpdate) throws Exception {
        if(session.action().equals("onList")){
            String conf = session.name()+TEMPLATE_SUFFIX;
            Configuration configuration = this.deploymentServiceProvider.configuration(conf);
            session.write(toJson(configuration).toString().getBytes());
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
        else if(session.action().equals("")){

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
    private JsonObject toJson(Configuration configuration){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("description",(String)configuration.property("description"));
        jsonObject.addProperty("category",(String) configuration.property("category"));
        jsonObject.add("itemList",(JsonArray)configuration.property("template-list"));
        return jsonObject;
    }
}
