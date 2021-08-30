package com.tarantula.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.*;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.util.SystemUtil;

import java.util.List;
import java.util.Set;

public class GameItemAdminModule implements Module {
    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;
    private static String GAME_ITEM_CATEGORY_TEMPLATE = "game-item-category-settings";
    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate onUpdate) throws Exception {
        if(session.action().equals("onTemplateCategory")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            ConfigurableTemplate configuration = this.deploymentServiceProvider.configuration(gameCluster,GAME_ITEM_CATEGORY_TEMPLATE);
            session.write(configuration.toJson().toString().getBytes());
        }
        else if(session.action().equals("onTemplateList")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(query[0]);
            ConfigurableTemplate template = this.deploymentServiceProvider.configuration(gameCluster,GAME_ITEM_CATEGORY_TEMPLATE);
            String conf = template.settings.get(query[1]).settingName;
            Configuration configuration = this.deploymentServiceProvider.configuration(gameCluster,conf);
            session.write(configuration.toJson().toString().getBytes());
        }
        else if (session.action().equals("onCreateCommodity")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            Item app = new Item();
            if(app.configureAndValidate(payload)){
                Descriptor desc = gameCluster.serviceWithCategory("system");
                SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME)).save(this.context,desc,app);
                session.write(JsonUtil.toSimpleResponse(true,app.distributionKey()).getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"failed to save commodity").getBytes());
            }
        }
        else if (session.action().equals("onCreateItem")){
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
        else if(session.action().equals("onCategory")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            Descriptor app = gameCluster.serviceWithCategory("system");
            ApplicationPreSetup preSetup = SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            Set<String> refs = preSetup.list(this.context,app);
            session.write(toJson(refs).toString().getBytes());
        }
        else if(session.action().equals("onStock")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(query[0]);
            Descriptor app = gameCluster.serviceWithCategory("system");
            ApplicationPreSetup preSetup = SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            List<Item> items = preSetup.list(this.context,app,new ItemQuery(query[1]));
            session.write(new ItemContext(true,"",items).toJson().toString().getBytes());
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

    private JsonObject toJson(Set<String> refs){
        JsonObject jsonObject = new JsonObject();
        JsonArray alist = new JsonArray();
        refs.forEach((ref)->alist.add(ref));
        jsonObject.add("onList",alist);
        return jsonObject;
    }
}
