package com.tarantula.admin;

import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.*;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.util.SystemUtil;

import java.util.List;

public class GameItemAdminModule implements Module {
    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;
    private static String GAME_ASSET_CATEGORY_TEMPLATE = "game-asset-category-settings";
    private static String GAME_COMMODITY_CATEGORY_TEMPLATE = "game-commodity-category-settings";
    private static String GAME_ITEM_CATEGORY_TEMPLATE = "game-item-category-settings";
    private static String GAME_APPLICATION_CATEGORY_TEMPLATE = "game-application-category-settings";
    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate onUpdate) throws Exception {
        if(session.action().equals("onTemplateAssetCategory")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            ConfigurableTemplate configuration = this.deploymentServiceProvider.configuration(gameCluster,GAME_ASSET_CATEGORY_TEMPLATE);
            session.write(configuration.toJson().toString().getBytes());
        }
        else if(session.action().equals("onTemplateCommodityCategory")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            ConfigurableTemplate configuration = this.deploymentServiceProvider.configuration(gameCluster,GAME_COMMODITY_CATEGORY_TEMPLATE);
            session.write(configuration.toJson().toString().getBytes());
        }
        else if(session.action().equals("onTemplateItemCategory")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            ConfigurableTemplate configuration = this.deploymentServiceProvider.configuration(gameCluster,GAME_ITEM_CATEGORY_TEMPLATE);
            session.write(configuration.toJson().toString().getBytes());
        }
        else if(session.action().equals("onTemplateApplicationCategory")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            ConfigurableTemplate configuration = this.deploymentServiceProvider.configuration(gameCluster,GAME_APPLICATION_CATEGORY_TEMPLATE);
            session.write(configuration.toJson().toString().getBytes());
        }
        else if(session.action().equals("onTemplateAssetList")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(query[0]);
            ConfigurableTemplate template = this.deploymentServiceProvider.configuration(gameCluster,GAME_ASSET_CATEGORY_TEMPLATE);
            ConfigurableSetting conf = template.settings.get(query[1]);
            Configuration configuration = this.deploymentServiceProvider.configuration(gameCluster,conf.settingName);
            session.write(configuration.toJson().toString().getBytes());
        }
        else if(session.action().equals("onTemplateCommodityList")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(query[0]);
            ConfigurableTemplate template = this.deploymentServiceProvider.configuration(gameCluster,GAME_COMMODITY_CATEGORY_TEMPLATE);
            String conf = template.settings.get(query[1]).settingName;
            Configuration configuration = this.deploymentServiceProvider.configuration(gameCluster,conf);
            session.write(configuration.toJson().toString().getBytes());
        }
        else if(session.action().equals("onTemplateItemList")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(query[0]);
            ConfigurableTemplate template = this.deploymentServiceProvider.configuration(gameCluster,GAME_ITEM_CATEGORY_TEMPLATE);
            String conf = template.settings.get(query[1]).settingName;
            Configuration configuration = this.deploymentServiceProvider.configuration(gameCluster,conf);
            session.write(configuration.toJson().toString().getBytes());
        }
        else if(session.action().equals("onTemplateApplicationList")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(query[0]);
            ConfigurableTemplate template = this.deploymentServiceProvider.configuration(gameCluster,GAME_APPLICATION_CATEGORY_TEMPLATE);
            String conf = template.settings.get(query[1]).settingName;
            Configuration configuration = this.deploymentServiceProvider.configuration(gameCluster,conf);
            session.write(configuration.toJson().toString().getBytes());
        }
        else if (session.action().equals("onCreateAsset")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            Asset app = new Asset();
            if(app.configureAndValidate(payload)){
                ConfigurableTemplate template = this.deploymentServiceProvider.configuration(gameCluster,GAME_ASSET_CATEGORY_TEMPLATE);
                ConfigurableSetting conf = template.settings.get(app.configurationCategory());
                Descriptor desc = gameCluster.serviceWithCategory("item");
                if(SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME)).save(this.context,desc,app)){
                    Category category = app.category(desc);
                    category.list();
                    category.addItem(new CategoryItem(conf.type,conf.name));
                    Index index = app.index(desc,app.configurationName());
                    index.index(app.distributionKey());
                }
                session.write(app.toJson().toString().getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"failed to save asset").getBytes());
            }
        }
        else if (session.action().equals("onCreateCommodity")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            Commodity app = new Commodity();
            if(app.configureAndValidate(payload)){
                ConfigurableTemplate template = this.deploymentServiceProvider.configuration(gameCluster,GAME_COMMODITY_CATEGORY_TEMPLATE);
                ConfigurableSetting conf = template.settings.get(app.configurationCategory());
                Descriptor desc = gameCluster.serviceWithCategory("item");
                if(SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME)).save(this.context,desc,app)){
                    Category category = app.category(desc);
                    category.list();
                    category.addItem(new CategoryItem(conf.type,conf.name));
                }
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
                ConfigurableTemplate template = this.deploymentServiceProvider.configuration(gameCluster,GAME_ITEM_CATEGORY_TEMPLATE);
                ConfigurableSetting conf = template.settings.get(app.configurationCategory());
                Descriptor desc = gameCluster.serviceWithCategory("item");
                if(SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME)).save(this.context,desc,app)){
                    Category category = app.category(desc);
                    category.list();
                    category.addItem(new CategoryItem(conf.type,conf.name));
                }
                session.write(JsonUtil.toSimpleResponse(true,app.distributionKey()).getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"failed to save item").getBytes());
            }
        }
        else if (session.action().equals("onCreateApplication")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            Application app = new Application();
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
            Descriptor app = gameCluster.serviceWithCategory("item");
            ApplicationPreSetup preSetup = SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            Category category = new Category();
            category.distributionKey(app.distributionKey());
            preSetup.load(context,app,category);
            category.list();
            session.write(category.toJson().toString().getBytes());
        }
        else if(session.action().equals("onStock")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(query[0]);
            Descriptor app = gameCluster.serviceWithCategory("item");
            ApplicationPreSetup preSetup = SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            List<ConfigurableObject> items = preSetup.list(this.context,app,new ConfigurableObjectQuery(query[1]));
            session.write(new ItemContext(true,query[1],items).toJson().toString().getBytes());
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
