package com.tarantula.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.*;
import com.tarantula.platform.service.ApplicationPreSetup;

import java.util.ArrayList;
import java.util.List;

public class GameItemAdminModule implements Module,Configurable.Listener<GameCluster> {
    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;

    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        if(session.action().equals("onCategorySettings")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(query[0]);
            ApplicationPreSetup applicationPreSetup = gameCluster.applicationPreSetup();//SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            ConfigurableCategories categories = this.configurableCategories(query[1],gameCluster,applicationPreSetup);
            categories.configurableTypes(this.configurableTypes(query[1],gameCluster,applicationPreSetup));
            session.write(categories.toJson().toString().getBytes());
        }
        else if(session.action().equals("onTypesSettings")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(query[0]);
            ApplicationPreSetup applicationPreSetup = gameCluster.applicationPreSetup();//SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            ConfigurableTypes configurableTypes = this.configurableTypes(query[1],gameCluster,applicationPreSetup);
            session.write(configurableTypes.toJson().toString().getBytes());
        }
        else if(session.action().equals("onUpdateEnumSettings")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(query[0]);
            ApplicationPreSetup applicationPreSetup = gameCluster.applicationPreSetup();//SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            JsonObject jo = JsonUtil.parse(payload).get("type").getAsJsonObject();
            TypeIndex typeIndex = new TypeIndex(jo.get("name").getAsString(),query[1],jo);
            boolean updateAllowed = true;
            if(applicationPreSetup.load(gameCluster,typeIndex)){
                updateAllowed = typeIndex.payload.get("type").getAsString().equals("enum");
            }
            if(updateAllowed && jo.get("type").getAsString().equals("enum")){
                typeIndex.payload = jo;
                applicationPreSetup.save(gameCluster,typeIndex);
                List<String> updates = availableUpdates(query[1]);
                updates.forEach((update)-> {
                    ConfigurableTypes configurableTypes = this.configurableTypes(update, gameCluster, applicationPreSetup);
                    configurableTypes.addType(jo);
                    applicationPreSetup.save(gameCluster, configurableTypes);
                    if(update.equals(query[1])) session.write(configurableTypes.toJson().toString().getBytes());
                });
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"update not allowed").getBytes());
            }
        }
        else if(session.action().equals("onUpdateTypesSettings")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(query[0]);
            ApplicationPreSetup applicationPreSetup = gameCluster.applicationPreSetup();//SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            JsonArray jtypes = JsonUtil.parse(payload).get("types").getAsJsonArray();
            int send = jtypes.size();
            for(JsonElement je : jtypes){
                JsonObject jo = je.getAsJsonObject();
                TypeIndex typeIndex = new TypeIndex(jo.get("name").getAsString(),query[1],jo);
                boolean updateAllowed = true;
                if(applicationPreSetup.load(gameCluster,typeIndex)){
                    String tpy = typeIndex.payload.get("type").getAsString();
                    updateAllowed = tpy.equals("string") || tpy.equals("number");
                }
                String ppt = jo.get("type").getAsString();
                if(updateAllowed && (ppt.equals("string")||ppt.equals("number"))){
                    send--;
                    applicationPreSetup.save(gameCluster,typeIndex);
                    List<String> updates = availableUpdates(query[1]);
                    updates.forEach((update)-> {
                        ConfigurableTypes configurableTypes = this.configurableTypes(update, gameCluster, applicationPreSetup);
                        configurableTypes.addType(jo);
                        applicationPreSetup.save(gameCluster, configurableTypes);
                    });
                }
            }
            session.write(JsonUtil.toSimpleResponse(send==0,send!=0?"one or more updates not allowed":"updated").getBytes());
        }
        else if(session.action().equals("onCreateCategorySettings")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(query[0]);
            ApplicationPreSetup applicationPreSetup = gameCluster.applicationPreSetup();//SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            JsonObject jo = JsonUtil.parse(payload).get("category").getAsJsonObject();
            JsonObject header = jo.get("header").getAsJsonObject();
            String scope = header.get("scope").getAsString();
            TypeIndex typeIndex = new TypeIndex(header.get("type").getAsString(),scope,jo);
            if(!applicationPreSetup.load(gameCluster,typeIndex)){
                applicationPreSetup.save(gameCluster,typeIndex);
                String ctype = typeIndex.index();
                int aix = ctype.indexOf('.');
                if(aix>0){
                    ctype = ctype.substring(0,aix);
                }
                List<String> updates = this.availableUpdates(ctype);
                updates.forEach(update->{
                    ConfigurableCategories categories = this.configurableCategories(update,gameCluster,applicationPreSetup);
                    if(categories.addCategory(jo)){
                        applicationPreSetup.save(gameCluster,categories);
                        ConfigurableTypes configurableTypes = this.configurableTypes(update,gameCluster,applicationPreSetup);
                        JsonObject type = new JsonObject();
                        type.addProperty("type","category");
                        type.addProperty("name",typeIndex.name());
                        configurableTypes.addType(type);
                        applicationPreSetup.save(gameCluster,configurableTypes);
                    }
                    if(update.equals(query[1])) session.write(categories.toJson().toString().getBytes());
                });
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,typeIndex.name()+" already existed").getBytes());
            }
        }
        else if(session.action().equals("onUpdateCategorySettings")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(query[0]);
            ApplicationPreSetup applicationPreSetup = gameCluster.applicationPreSetup();//SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            JsonObject jo = JsonUtil.parse(payload).get("category").getAsJsonObject();
            JsonObject header = jo.get("header").getAsJsonObject();
            String scope = header.get("scope").getAsString();
            TypeIndex typeIndex = new TypeIndex(header.get("type").getAsString());
            if(applicationPreSetup.load(gameCluster,typeIndex)){
                if(typeIndex.index().equals(scope)){
                    typeIndex.payload = jo;
                    applicationPreSetup.save(gameCluster,typeIndex);
                    String ctype = typeIndex.index();
                    int aix = ctype.indexOf('.');
                    if(aix>0){
                        ctype = ctype.substring(0,aix);
                    }
                    List<String> updates = this.availableUpdates(ctype);
                    updates.forEach(update->{
                        ConfigurableCategories categories = this.configurableCategories(update,gameCluster,applicationPreSetup);
                        if(categories.updateCategory(jo)){
                            applicationPreSetup.save(gameCluster,categories);
                        }
                        if(update.equals(query[1])) session.write(categories.toJson().toString().getBytes());
                    });
                }
                else{
                    session.write(JsonUtil.toSimpleResponse(false,"scope not matched ["+ header.get("scope").getAsString()+"<>"+typeIndex.index()+"]").getBytes());
                }
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,typeIndex.name()+" not existed").getBytes());
            }
        }
        else if (session.action().equals("onCreateAsset")||session.action().equals("onUpdateAsset")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            ApplicationPreSetup applicationPreSetup = gameCluster.applicationPreSetup();
            session.write(createAsset(new Asset(),JsonUtil.parse(payload),gameCluster,applicationPreSetup).getBytes());
        }
        else if (session.action().equals("onCreateComponent") || session.action().equals("onUpdateComponent")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            ApplicationPreSetup applicationPreSetup = gameCluster.applicationPreSetup();
            session.write(createComponent(new Component(),JsonUtil.parse(payload),gameCluster,applicationPreSetup).getBytes());
        }
        else if (session.action().equals("onCreateCommodity")||session.action().equals("onUpdateCommodity")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            ApplicationPreSetup applicationPreSetup = gameCluster.applicationPreSetup();
            session.write(createCommodity(new Commodity(),JsonUtil.parse(payload),gameCluster,applicationPreSetup).getBytes());
        }
        else if (session.action().equals("onCreateItem")||session.action().equals("onUpdateItem")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            ApplicationPreSetup applicationPreSetup = gameCluster.applicationPreSetup();
            session.write(createItem(new Item(),JsonUtil.parse(payload),gameCluster,applicationPreSetup).getBytes());
        }
        else if (session.action().equals("onCreateApplication")||session.action().equals("onUpdateApplication")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            ApplicationPreSetup applicationPreSetup = gameCluster.applicationPreSetup();
            session.write(createApplication(new Application(),JsonUtil.parse(payload),gameCluster,applicationPreSetup).getBytes());
        }
        else if(session.action().equals("onStock")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(query[0]);
            ApplicationPreSetup preSetup = gameCluster.applicationPreSetup();//SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            Descriptor app = gameCluster.serviceWithCategory("item");
            ConfigurableCategories categories = this.configurableCategories(Configurable.APPLICATION_CONFIG_TYPE,gameCluster,preSetup);
            ConfigurableSetting configurableSetting = categories.configurableSetting(query[1].split("/")[1]);
            if(configurableSetting != null && configurableSetting.scope.startsWith("application.")){
                String serviceCategory = configurableSetting.scope.substring(12);
                app = gameCluster.serviceWithCategory(serviceCategory);
            }
            List<ConfigurableObject> items = preSetup.list(app,new ConfigurableObjectQuery(query[1]));
            session.write(new ItemAdminContext(true,query[1],items).toJson().toString().getBytes());
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
        this.deploymentServiceProvider.registerConfigurableListener(GameCluster.GAME_CLUSTER_CONFIGURATION_TYPE,this);
        this.context.log("Game item admin module started", OnLog.WARN);
    }
    private String createCategory(JsonObject payload,GameCluster gameCluster,ApplicationPreSetup applicationPreSetup){
        JsonObject header = payload.get("header").getAsJsonObject();
        String scope = header.get("scope").getAsString();
        TypeIndex typeIndex = new TypeIndex(header.get("type").getAsString(),scope,payload);
        if(applicationPreSetup.load(gameCluster,typeIndex)) return JsonUtil.toSimpleResponse(false,"Category already existed");
        applicationPreSetup.save(gameCluster,typeIndex);
        String ctype = typeIndex.index();
        int aix = ctype.indexOf('.');
        if(aix>0){
            ctype = ctype.substring(0,aix);
        }
        List<String> updates = this.availableUpdates(ctype);
        updates.forEach(update->{
            ConfigurableCategories categories = this.configurableCategories(update,gameCluster,applicationPreSetup);
            if(categories.addCategory(payload)){
                applicationPreSetup.save(gameCluster,categories);
                ConfigurableTypes configurableTypes = this.configurableTypes(update,gameCluster,applicationPreSetup);
                JsonObject type = new JsonObject();
                type.addProperty("type","category");
                type.addProperty("name",typeIndex.name());
                configurableTypes.addType(type);
                applicationPreSetup.save(gameCluster,configurableTypes);
            }
        });
        return JsonUtil.toSimpleResponse(true,"Category added");
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
        if(!applicationPreSetup.load(gameCluster,categories)){
            ConfigurableTemplate configuration = this.categoryTemplateSetting(gameCluster,type);
            JsonArray cclasses = (JsonArray)configuration.property("itemList");
            cclasses.forEach((c)->categories.addCategory(c.getAsJsonObject()));
            applicationPreSetup.save(gameCluster,categories);
        }
        return categories;
    }
    private ConfigurableTypes configurableTypes(String name,GameCluster gameCluster,ApplicationPreSetup applicationPreSetup){
        ConfigurableTypes configurableTypes = new ConfigurableTypes();
        configurableTypes.name(name);
        if(!applicationPreSetup.load(gameCluster,configurableTypes)){
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
            applicationPreSetup.save(gameCluster,configurableTypes);
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
    private String createAsset(Asset app,JsonObject payload,GameCluster gameCluster,ApplicationPreSetup applicationPreSetup){
        if(!app.configureAndValidate(payload)){
            return JsonUtil.toSimpleResponse(false,"invalid payload");
        }
        ConfigurableCategories configurableCategories = this.configurableCategories(Configurable.ASSET_CONFIG_TYPE,gameCluster,applicationPreSetup);
        ConfigurableSetting conf = configurableCategories.configurableSetting(app.configurationCategory());
        if(conf==null){
            return JsonUtil.toSimpleResponse(false," no config setting ["+app.configurationCategory()+"]");
        }
        Descriptor desc = gameCluster.serviceWithCategory("item");
        app.configurableSetting(conf);
        if(!gameCluster.applicationPreSetup().save(desc,app)){
            return JsonUtil.toSimpleResponse(true,"failed to save");
        }
        return new AssetSerializer().serialize(app,Application.class,null).toString();
    }
    private String createComponent(Component app,JsonObject payload,GameCluster gameCluster,ApplicationPreSetup applicationPreSetup){
        if(!app.configureAndValidate(payload)){
            return JsonUtil.toSimpleResponse(false,"invalid payload");
        }
        ConfigurableCategories configurableCategories = this.configurableCategories(Configurable.COMPONENT_CONFIG_TYPE,gameCluster,applicationPreSetup);
        ConfigurableSetting conf = configurableCategories.configurableSetting(app.configurationCategory());
        if(conf==null){
            return JsonUtil.toSimpleResponse(false," no config setting ["+app.configurationCategory()+"]");
        }
        Descriptor desc = gameCluster.serviceWithCategory("item");
        app.configurableSetting(conf);
        if(!gameCluster.applicationPreSetup().save(desc,app)) {
            return JsonUtil.toSimpleResponse(true,"failed to save");
        }
        return new ComponentSerializer().serialize(app,Application.class,null).toString();
    }
    private String createCommodity(Commodity app,JsonObject payload,GameCluster gameCluster,ApplicationPreSetup applicationPreSetup){
        if(!app.configureAndValidate(payload)){
            return JsonUtil.toSimpleResponse(false,"invalid payload");
        }
        ConfigurableCategories configurableCategories = this.configurableCategories(Configurable.COMMODITY_CONFIG_TYPE,gameCluster,applicationPreSetup);
        ConfigurableSetting conf = configurableCategories.configurableSetting(app.configurationCategory());
        if(conf==null){
            return JsonUtil.toSimpleResponse(false," no config setting ["+app.configurationCategory()+"]");
        }
        Descriptor desc = gameCluster.serviceWithCategory("item");
        app.configurableSetting(conf);
        if(!gameCluster.applicationPreSetup().save(desc,app)) {
            return JsonUtil.toSimpleResponse(true,"failed to save");
        }
        Category category = app.category(desc);
        category.list();
        category.addItem(new CategoryItem(Configurable.COMMODITY_CONFIG_TYPE,conf.type,app.configurationTypeId()));
        return new CommoditySerializer().serialize(app,Application.class,null).toString();
    }
    private String createItem(Item app,JsonObject payload,GameCluster gameCluster,ApplicationPreSetup applicationPreSetup){
        if(!app.configureAndValidate(payload)){
            return JsonUtil.toSimpleResponse(false,"invalid payload");
        }
        ConfigurableCategories configurableCategories = this.configurableCategories(Configurable.ITEM_CONFIG_TYPE,gameCluster,applicationPreSetup);
        ConfigurableSetting conf = configurableCategories.configurableSetting(app.configurationCategory());
        if(conf==null){
            return JsonUtil.toSimpleResponse(false," no config setting ["+app.configurationCategory()+"]");
        }
        Descriptor desc = gameCluster.serviceWithCategory("item");
        app.configurableSetting(conf);
        if(!gameCluster.applicationPreSetup().save(desc,app)) {
            return JsonUtil.toSimpleResponse(true,"failed to save");
        }
        return new ItemSerializer().serialize(app,Application.class,null).toString();
    }
    public String createApplication(Application app,JsonObject payload,GameCluster gameCluster,ApplicationPreSetup applicationPreSetup){
        if(!app.configureAndValidate(payload)){
            return JsonUtil.toSimpleResponse(false,"invalid payload");
        }
        ConfigurableCategories configurableCategories = this.configurableCategories(Configurable.APPLICATION_CONFIG_TYPE,gameCluster,applicationPreSetup);
        ConfigurableSetting conf = configurableCategories.configurableSetting(app.configurationCategory());
        if(conf==null){
            return JsonUtil.toSimpleResponse(false," no config setting ["+app.configurationCategory()+"]");
        }
        Descriptor desc = gameCluster.serviceWithCategory(app.configurationTypeId());
        app.disabled(true);
        app.configurableSetting(conf);
        if(!gameCluster.applicationPreSetup().save(desc,app)){
            return JsonUtil.toSimpleResponse(true,"failed to save");
        }
        app.setup();
        return new ApplicationSerializer().serialize(app,Application.class,null).toString();
    }
    @Override
    public void onCreated(GameCluster gameCluster) {
        ApplicationPreSetup applicationPreSetup = gameCluster.applicationPreSetup();
        ConfigurableCategories assets = this.configurableCategories(Configurable.ASSET_CONFIG_TYPE, gameCluster, applicationPreSetup);
        ConfigurableCategories components = this.configurableCategories(Configurable.COMPONENT_CONFIG_TYPE, gameCluster, applicationPreSetup);
        ConfigurableCategories commodities = this.configurableCategories(Configurable.COMMODITY_CONFIG_TYPE, gameCluster, applicationPreSetup);
        ConfigurableCategories items = this.configurableCategories(Configurable.ITEM_CONFIG_TYPE, gameCluster, applicationPreSetup);
        ConfigurableCategories applications = this.configurableCategories(Configurable.APPLICATION_CONFIG_TYPE, gameCluster, applicationPreSetup);
        assets.toCategories().forEach((c -> {
            JsonObject jo = c.getAsJsonObject();
            JsonObject ho = jo.get("header").getAsJsonObject();
            TypeIndex typeIndex = new TypeIndex(ho.get("type").getAsString(), ho.get("scope").getAsString(), jo);
            applicationPreSetup.save(gameCluster, typeIndex);
            components.addCategory(jo);
            commodities.addCategory(jo);
            items.addCategory(jo);
            applications.addCategory(jo);
        }));
        components.toCategories().forEach((c -> {
            JsonObject jo = c.getAsJsonObject();
            JsonObject ho = jo.get("header").getAsJsonObject();
            TypeIndex typeIndex = new TypeIndex(ho.get("type").getAsString(), ho.get("scope").getAsString(), jo);
            applicationPreSetup.save(gameCluster, typeIndex);
            commodities.addCategory(jo);
            items.addCategory(jo);
            applications.addCategory(jo);
        }));
        commodities.toCategories().forEach((c -> {
            JsonObject jo = c.getAsJsonObject();
            JsonObject ho = jo.get("header").getAsJsonObject();
            TypeIndex typeIndex = new TypeIndex(ho.get("type").getAsString(), ho.get("scope").getAsString(), jo);
            applicationPreSetup.save(gameCluster, typeIndex);
            items.addCategory(jo);
            applications.addCategory(jo);
        }));
        items.toCategories().forEach((c -> {
            JsonObject jo = c.getAsJsonObject();
            JsonObject ho = jo.get("header").getAsJsonObject();
            TypeIndex typeIndex = new TypeIndex(ho.get("type").getAsString(), ho.get("scope").getAsString(), jo);
            applicationPreSetup.save(gameCluster, typeIndex);
            applications.addCategory(jo);
        }));
        applications.toCategories().forEach((c -> {
            JsonObject jo = c.getAsJsonObject();
            JsonObject ho = jo.get("header").getAsJsonObject();
            TypeIndex typeIndex = new TypeIndex(ho.get("type").getAsString(), ho.get("scope").getAsString(), jo);
            applicationPreSetup.save(gameCluster, typeIndex);
            //applications.addCategory(jo);
        }));
        applicationPreSetup.save(gameCluster, assets);
        applicationPreSetup.save(gameCluster, components);
        applicationPreSetup.save(gameCluster, commodities);
        applicationPreSetup.save(gameCluster, items);
        applicationPreSetup.save(gameCluster, applications);

        ConfigurableTypes assetTypes = this.configurableTypes(Configurable.ASSET_CONFIG_TYPE, gameCluster, applicationPreSetup);
        ConfigurableTypes componentTypes = this.configurableTypes(Configurable.COMPONENT_CONFIG_TYPE, gameCluster, applicationPreSetup);
        ConfigurableTypes commodityTypes = this.configurableTypes(Configurable.COMMODITY_CONFIG_TYPE, gameCluster, applicationPreSetup);
        ConfigurableTypes itemTypes = this.configurableTypes(Configurable.ITEM_CONFIG_TYPE, gameCluster, applicationPreSetup);
        ConfigurableTypes applicationTypes = this.configurableTypes(Configurable.APPLICATION_CONFIG_TYPE, gameCluster, applicationPreSetup);
        assetTypes.toTypes().forEach((t) -> {
            componentTypes.addType(t.getAsJsonObject());
            commodityTypes.addType(t.getAsJsonObject());
            itemTypes.addType(t.getAsJsonObject());
            applicationTypes.addType(t.getAsJsonObject());
        });
        componentTypes.toTypes().forEach((t) -> {
            commodityTypes.addType(t.getAsJsonObject());
            itemTypes.addType(t.getAsJsonObject());
            applicationTypes.addType(t.getAsJsonObject());
        });
        commodityTypes.toTypes().forEach((t) -> {
            itemTypes.addType(t.getAsJsonObject());
            applicationTypes.addType(t.getAsJsonObject());
        });
        itemTypes.toTypes().forEach((t) -> {
            applicationTypes.addType(t.getAsJsonObject());
        });

        applicationPreSetup.save(gameCluster, assetTypes);
        applicationPreSetup.save(gameCluster, componentTypes);
        applicationPreSetup.save(gameCluster, commodityTypes);
        applicationPreSetup.save(gameCluster, itemTypes);
        applicationPreSetup.save(gameCluster, applicationTypes);

        this.onLoaded(gameCluster);
        //pre-defined lobby configurations
        //log lobby apps
        Configuration lobbyConfiguration = this.context.configuration("lobby");
        String mode = (String)gameCluster.property(GameCluster.MODE);
        JsonObject roomPayload = ((JsonElement) lobbyConfiguration.property(mode+"Room")).getAsJsonObject();
        JsonObject zonePayload = ((JsonElement)lobbyConfiguration.property("zone")).getAsJsonObject();
        Component room = new Component();
        createComponent(room, roomPayload.getAsJsonObject(), gameCluster, applicationPreSetup);
        this.context.log(room.distributionKey(), OnLog.WARN);
        zonePayload.get("application").getAsJsonObject().get("Room").getAsJsonArray().add(room.distributionKey());
        JsonArray refs = zonePayload.get("reference").getAsJsonArray();
        refs.add(room.distributionKey());
        JsonArray arenas = ((JsonElement)lobbyConfiguration.property("arenas")).getAsJsonArray();
        JsonArray aset = zonePayload.get("application").getAsJsonObject().get("ArenaSet").getAsJsonArray();
        arenas.forEach(arenaPayload -> {
            Commodity arena = new Commodity();
            createCommodity(arena,arenaPayload.getAsJsonObject(),gameCluster,applicationPreSetup);
            this.context.log(arena.distributionKey(),OnLog.WARN);
            aset.add(arena.distributionKey());
            refs.add(arena.distributionKey());
        });
        Item zone = new Item();
        createItem(zone,zonePayload,gameCluster,applicationPreSetup);
        this.context.log(zone.distributionKey(),OnLog.WARN);
        JsonObject lobbyPayload = ((JsonElement)lobbyConfiguration.property("lobby")).getAsJsonObject();
        lobbyPayload.get("application").getAsJsonObject().get("ZoneSet").getAsJsonArray().add(zone.distributionKey());
        lobbyPayload.get("reference").getAsJsonArray().add(zone.distributionKey());
        Application lobby = new Application();
        createApplication(lobby,lobbyPayload,gameCluster,applicationPreSetup);
        this.context.log(lobby.distributionKey(),OnLog.WARN);
        //pre-defined third party validators
        Configuration configuration = this.context.configuration(((String) gameCluster.property(GameCluster.NAME)).toLowerCase());
        if(configuration!=null) {
            try{
                JsonArray validators = ((JsonElement)configuration.property("validators")).getAsJsonArray();
                validators.forEach(validator->{
                    Application app = new Application();
                    String resp = createApplication(app,validator.getAsJsonObject(),gameCluster,applicationPreSetup);
                    this.context.log(resp,OnLog.WARN);
                });
            }catch (Exception ex){
                this.context.log("expected error",ex,OnLog.ERROR);
            }
        }
    }

    @Override
    public void onLoaded(GameCluster gameCluster){
        ApplicationPreSetup applicationPreSetup = gameCluster.applicationPreSetup();
        Configuration configuration = this.deploymentServiceProvider.configuration(gameCluster,GameCluster.GAME_UPGRADE_CATEGORY_TEMPLATE);
        JsonArray cclasses = (JsonArray)configuration.property("itemList");
        cclasses.forEach((c)->{
            createCategory(c.getAsJsonObject(),gameCluster,applicationPreSetup);
        });
    }
}
