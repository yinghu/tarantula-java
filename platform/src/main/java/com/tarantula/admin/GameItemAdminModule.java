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

import java.util.ArrayList;
import java.util.List;

public class GameItemAdminModule implements Module {
    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;

    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        if(session.action().equals("onCategorySettings")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(query[0]);
            ApplicationPreSetup applicationPreSetup = SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            ConfigurableCategories categories = this.configurableCategories(query[1],gameCluster,applicationPreSetup);
            categories.configurableTypes(this.configurableTypes(query[1],gameCluster,applicationPreSetup));
            session.write(categories.toJson().toString().getBytes());
        }
        else if(session.action().equals("onTypesSettings")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(query[0]);
            ApplicationPreSetup applicationPreSetup = SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            ConfigurableTypes configurableTypes = this.configurableTypes(query[1],gameCluster,applicationPreSetup);
            session.write(configurableTypes.toJson().toString().getBytes());
        }
        else if(session.action().equals("onUpdateTypesSettings")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(query[0]);
            ApplicationPreSetup applicationPreSetup = SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            JsonObject jo = JsonUtil.parse(payload).get("type").getAsJsonObject();
            TypeIndex typeIndex = new TypeIndex(jo.get("name").getAsString(),query[1],jo.get("type").getAsString());
            if(!applicationPreSetup.load(context,gameCluster,typeIndex)){
                applicationPreSetup.save(context,gameCluster,typeIndex);
                List<String> updates = availableUpdates(query[1]);
                updates.forEach((update)-> {
                    ConfigurableTypes configurableTypes = this.configurableTypes(update, gameCluster, applicationPreSetup);
                    configurableTypes.addType(jo);
                    applicationPreSetup.save(context, gameCluster, configurableTypes);
                    if(update.equals(query[1])) session.write(configurableTypes.toJson().toString().getBytes());
                });
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,typeIndex.name()+" already existed").getBytes());
            }
        }
        else if(session.action().equals("onUpdateCategorySettings")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(query[0]);
            ApplicationPreSetup applicationPreSetup = SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            JsonObject jo = JsonUtil.parse(payload).get("category").getAsJsonObject();
            JsonObject header = jo.get("header").getAsJsonObject();
            TypeIndex typeIndex = new TypeIndex(header.get("type").getAsString(),query[1],header.get("scope").getAsString());
            if(!applicationPreSetup.load(context,gameCluster,typeIndex)){
                applicationPreSetup.save(context,gameCluster,typeIndex);
                List<String> updates = this.availableUpdates(typeIndex.label());
                updates.forEach(update->{
                    ConfigurableCategories categories = this.configurableCategories(update,gameCluster,applicationPreSetup);
                    if(categories.addCategory(jo)){
                        applicationPreSetup.save(context,gameCluster,categories);
                        ConfigurableTypes configurableTypes = this.configurableTypes(update,gameCluster,applicationPreSetup);
                        JsonObject type = new JsonObject();
                        type.addProperty("type","category");
                        type.addProperty("name",typeIndex.name());
                        configurableTypes.addType(type);
                        applicationPreSetup.save(context,gameCluster,configurableTypes);
                    }
                    if(update.equals(query[1])) session.write(categories.toJson().toString().getBytes());
                });
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,typeIndex.name()+" already existed").getBytes());
            }
        }
        else if (session.action().equals("onCreateAsset")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            ApplicationPreSetup applicationPreSetup = SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            Asset app = new Asset();
            if(app.configureAndValidate(JsonUtil.parse(payload))){
                ConfigurableCategories configurableCategories = this.configurableCategories(Configurable.ASSET_CONFIG_TYPE,gameCluster,applicationPreSetup);
                ConfigurableSetting conf = configurableCategories.configurableSetting(app.configurationCategory());
                Descriptor desc = gameCluster.serviceWithCategory("item");
                if(conf!=null&&SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME)).save(this.context,desc,app)){
                    //Category category = app.category(desc);
                    //category.list();
                    //category.addItem(new CategoryItem(Configurable.ASSET_CONFIG_TYPE,conf.type,conf.name));
                    session.write(app.toJson().toString().getBytes());
                }
                else{
                    session.write(JsonUtil.toSimpleResponse(false,"failed to save asset").getBytes());
                }
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"invalid config values").getBytes());
            }
        }
        else if (session.action().equals("onCreateComponent")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            ApplicationPreSetup applicationPreSetup = SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            Component app = new Component();
            if(app.configureAndValidate(JsonUtil.parse(payload))){
                ConfigurableCategories configurableCategories = this.configurableCategories(Configurable.COMPONENT_CONFIG_TYPE,gameCluster,applicationPreSetup);
                ConfigurableSetting conf = configurableCategories.configurableSetting(app.configurationCategory());
                Descriptor desc = gameCluster.serviceWithCategory("item");
                if(conf!=null && SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME)).save(this.context,desc,app)){
                    //Category category = app.category(desc);
                    //category.list();
                    //category.addItem(new CategoryItem(Configurable.COMPONENT_CONFIG_TYPE,conf.type,conf.name));
                    session.write(app.toJson().toString().getBytes());
                }
                else{
                    session.write(JsonUtil.toSimpleResponse(false,"failed to save component").getBytes());
                }
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"invalid config values").getBytes());
            }
        }
        else if (session.action().equals("onCreateCommodity")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            ApplicationPreSetup applicationPreSetup = SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            Commodity app = new Commodity();
            if(app.configureAndValidate(JsonUtil.parse(payload))){
                ConfigurableCategories configurableCategories = this.configurableCategories(Configurable.COMMODITY_CONFIG_TYPE,gameCluster,applicationPreSetup);
                ConfigurableSetting conf = configurableCategories.configurableSetting(app.configurationCategory());
                Descriptor desc = gameCluster.serviceWithCategory("item");
                if(conf!=null && SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME)).save(this.context,desc,app)){
                    //Category category = app.category(desc);
                    //category.list();
                    //category.addItem(new CategoryItem(Configurable.COMMODITY_CONFIG_TYPE,conf.type,conf.name));
                    session.write(app.toJson().toString().getBytes());
                }
                else{
                    session.write(JsonUtil.toSimpleResponse(false,"failed to save commodity").getBytes());
                }
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"invalid config values").getBytes());
            }
        }
        else if (session.action().equals("onCreateItem")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            ApplicationPreSetup applicationPreSetup = SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            Item app = new Item();
            if(app.configureAndValidate(JsonUtil.parse(payload))){
                ConfigurableCategories configurableCategories = this.configurableCategories(Configurable.ITEM_CONFIG_TYPE,gameCluster,applicationPreSetup);
                ConfigurableSetting conf = configurableCategories.configurableSetting(app.configurationCategory());
                Descriptor desc = gameCluster.serviceWithCategory("item");
                if(conf!=null && SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME)).save(this.context,desc,app)){
                    //Category category = app.category(desc);
                    ///category.list();
                    //category.addItem(new CategoryItem(Configurable.ITEM_CONFIG_TYPE,conf.type,conf.name));
                    session.write(app.toJson().toString().getBytes());
                }
                else{
                    session.write(JsonUtil.toSimpleResponse(false,"failed to save item").getBytes());
                }
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"invalid config values").getBytes());
            }
        }
        else if (session.action().equals("onCreateApplication")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            ApplicationPreSetup applicationPreSetup = SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            Application app = new Application();
            if(app.configureAndValidate(JsonUtil.parse(payload))){
                ConfigurableCategories configurableCategories = this.configurableCategories(Configurable.APPLICATION_CONFIG_TYPE,gameCluster,applicationPreSetup);
                ConfigurableSetting conf = configurableCategories.configurableSetting(app.configurationCategory());
                Descriptor desc = gameCluster.serviceWithCategory(app.configurationCategory());
                if(conf!=null && SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME)).save(this.context,desc,app)){
                    //Category category = app.category(desc);
                    //category.list();
                    //category.addItem(new CategoryItem(Configurable.APPLICATION_CONFIG_TYPE,conf.type,conf.name));
                    session.write(app.toJson().toString().getBytes());
                }
                else{
                    session.write(JsonUtil.toSimpleResponse(false,"failed to save application").getBytes());
                }
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"invalid config values").getBytes());
            }
        }

        else if(session.action().equals("onStock")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(query[0]);
            Descriptor app = gameCluster.serviceWithCategory("item");
            ApplicationPreSetup preSetup = SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            List<ConfigurableHeader> items = preSetup.list(this.context,app,new ConfigurableHeaderQuery(query[1]));
            session.write(new ItemHeaderContext(true,query[1],items).toJson().toString().getBytes());
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
    private ConfigurableTemplate categoryTemplateSetting(GameCluster gameCluster,String name){
        if(name.equals(Configurable.ASSET_CONFIG_TYPE))
            return this.deploymentServiceProvider.configuration(gameCluster,GameCluster.GAME_ASSET_CATEGORY_TEMPLATE);
        if(name.equals(Configurable.COMPONENT_CONFIG_TYPE))
            return this.deploymentServiceProvider.configuration(gameCluster,GameCluster.GAME_COMPONENT_CATEGORY_TEMPLATE);
        if(name.equals(Configurable.COMMODITY_CONFIG_TYPE))
            return this.deploymentServiceProvider.configuration(gameCluster,GameCluster.GAME_COMMODITY_CATEGORY_TEMPLATE);
        if(name.equals(Configurable.ITEM_CONFIG_TYPE))
            return this.deploymentServiceProvider.configuration(gameCluster,GameCluster.GAME_ITEM_CATEGORY_TEMPLATE);
        if(name.equals(Configurable.APPLICATION_CONFIG_TYPE))
            return this.deploymentServiceProvider.configuration(gameCluster,GameCluster.GAME_APPLICATION_CATEGORY_TEMPLATE);
        return null;
    }
    private ConfigurableCategories configurableCategories(String type,GameCluster gameCluster,ApplicationPreSetup applicationPreSetup){
        ConfigurableCategories categories = new ConfigurableCategories();
        categories.name(type);
        if(!applicationPreSetup.load(context,gameCluster,categories)){
            ConfigurableTemplate configuration = this.categoryTemplateSetting(gameCluster,type);
            JsonArray cclasses = (JsonArray)configuration.property("itemList");
            cclasses.forEach((c)->categories.addCategory(c.getAsJsonObject()));
            applicationPreSetup.save(context,gameCluster,categories);
        }
        return categories;
    }
    private ConfigurableTypes configurableTypes(String name,GameCluster gameCluster,ApplicationPreSetup applicationPreSetup){
        ConfigurableTypes configurableTypes = new ConfigurableTypes();
        configurableTypes.name(name);
        if(!applicationPreSetup.load(context,gameCluster,configurableTypes)){
            Configuration commonTypes = this.deploymentServiceProvider.configuration(gameCluster,GameCluster.GAME_COMMON_TYPE_TEMPLATE);
            JsonArray ctypes = (JsonArray)commonTypes.property("itemList");
            ctypes.forEach((c)-> configurableTypes.addType(c.getAsJsonObject()));
            ConfigurableTemplate template = this.categoryTemplateSetting(gameCluster,name);
            JsonArray cct = (JsonArray)template.property("itemList");
            cct.forEach((t)->{
                String type = t.getAsJsonObject().get("header").getAsJsonObject().get("type").getAsString();
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("type","category");
                jsonObject.addProperty("name",type);
                configurableTypes.addType(jsonObject);
            });
            Configuration configuration = this.deploymentServiceProvider.configuration(gameCluster,template.name);
            JsonArray items = (JsonArray) configuration.property("itemList");
            items.forEach((f)-> configurableTypes.addType(f.getAsJsonObject()));
            applicationPreSetup.save(context,gameCluster,configurableTypes);
        }
        return configurableTypes;
    }
    private List<String> availableUpdates(String type){
        ArrayList<String> updates = new ArrayList<>();
        updates.add(type);
        if(type.equals(Configurable.ITEM_CONFIG_TYPE)){
             updates.add(Configurable.APPLICATION_CONFIG_TYPE);
             return updates;
        }
        if(type.equals(Configurable.COMMODITY_CONFIG_TYPE)){
            updates.add(Configurable.ITEM_CONFIG_TYPE);
            updates.add(Configurable.APPLICATION_CONFIG_TYPE);
            return updates;
        }
        if(type.equals(Configurable.COMPONENT_CONFIG_TYPE)){
            updates.add(Configurable.COMMODITY_CONFIG_TYPE);
            updates.add(Configurable.ITEM_CONFIG_TYPE);
            updates.add(Configurable.APPLICATION_CONFIG_TYPE);
            return updates;
        }
        if(type.equals(Configurable.ASSET_CONFIG_TYPE)){
            updates.add(Configurable.COMPONENT_CONFIG_TYPE);
            updates.add(Configurable.COMMODITY_CONFIG_TYPE);
            updates.add(Configurable.ITEM_CONFIG_TYPE);
            updates.add(Configurable.APPLICATION_CONFIG_TYPE);
            return updates;
        }
        return updates;
    }
}
