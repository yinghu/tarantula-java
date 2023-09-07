package com.tarantula.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.OidKey;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.*;
import com.tarantula.platform.service.ApplicationPreSetup;

import java.util.ArrayList;
import java.util.List;

public class GameItemAdminRoleModule implements Module,Configurable.Listener<GameCluster> {
    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;

    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        if(session.action().equals("onCategorySettings")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(Long.parseLong(query[0]));
            ApplicationPreSetup applicationPreSetup = gameCluster.applicationPreSetup();
            ConfigurableCategories categories = this.configurableCategories(query[1],gameCluster,applicationPreSetup);
            categories.configurableTypes(this.configurableTypes(query[1],gameCluster,applicationPreSetup));
            session.write(categories.toJson().toString().getBytes());
        }
        else if(session.action().equals("onTypesSettings")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(Long.parseLong(query[0]));
            ApplicationPreSetup applicationPreSetup = gameCluster.applicationPreSetup();
            ConfigurableTypes configurableTypes = this.configurableTypes(query[1],gameCluster,applicationPreSetup);
            session.write(configurableTypes.toJson().toString().getBytes());
        }
        else if(session.action().equals("onUpdateEnumSettings")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(Long.parseLong(query[0]));
            ApplicationPreSetup applicationPreSetup = gameCluster.applicationPreSetup();
            JsonObject jo = JsonUtil.parse(payload).get("type").getAsJsonObject();
            TypeIndex typeIndex = new TypeIndex(jo.get("name").getAsString(),TypeIndex.Typed.Enum,query[1],jo);
            boolean updateAllowed = query[2].equals("save");
            boolean deleted = query[2].equals("delete");
            if(applicationPreSetup.load(gameCluster,typeIndex)){
                ReferenceIndex instanceIndex = new ReferenceIndex(typeIndex.name());
                applicationPreSetup.load(gameCluster,instanceIndex);
                updateAllowed = deleted ?(typeIndex.payload().get("type").getAsString().equals("enum") && instanceIndex.keySet().isEmpty()) : typeIndex.payload().get("type").getAsString().equals("enum");
            }
            if(updateAllowed){
                typeIndex.upgrade(jo);
                if(deleted){
                    applicationPreSetup.delete(gameCluster,typeIndex);
                }else{
                    applicationPreSetup.save(gameCluster,typeIndex);
                }
                List<String> updates = availableUpdates(deleted?Configurable.ASSET_CONFIG_TYPE:query[1]);
                updates.forEach((update)-> {
                    ConfigurableTypes configurableTypes = this.configurableTypes(update, gameCluster, applicationPreSetup);
                    if(deleted){
                        configurableTypes.removeType(jo);
                    }else{
                        configurableTypes.addType(jo);
                    }
                    applicationPreSetup.save(gameCluster, configurableTypes);
                    if(update.equals(query[1])) session.write(configurableTypes.toJson().toString().getBytes());
                });
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,query[2]+" not allowed").getBytes());
            }
        }
        else if(session.action().equals("onUpdateTypesSettings")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(Long.parseLong(query[0]));
            ApplicationPreSetup applicationPreSetup = gameCluster.applicationPreSetup();
            JsonArray jtypes = JsonUtil.parse(payload).get("types").getAsJsonArray();
            int send = jtypes.size();
            for(JsonElement je : jtypes){
                JsonObject jo = je.getAsJsonObject();
                TypeIndex typeIndex = new TypeIndex(jo.get("name").getAsString(),TypeIndex.Typed.Category,query[1],jo);
                boolean updateAllowed = true;
                if(applicationPreSetup.load(gameCluster,typeIndex)){
                    String tpy = typeIndex.payload().get("type").getAsString();
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
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(Long.parseLong(query[0]));
            ApplicationPreSetup applicationPreSetup = gameCluster.applicationPreSetup();
            JsonObject jo = JsonUtil.parse(payload).get("category").getAsJsonObject();
            JsonObject header = jo.get("header").getAsJsonObject();
            String ctype = header.get("scope").getAsString();
            int aix = ctype.indexOf('.');
            if(aix>0){
                ctype = ctype.substring(0,aix);
            }
            ConfigurableCategory category = new ConfigurableCategory(jo);
            if(gameCluster.configurableCategories(Configurable.APPLICATION_CONFIG_TYPE).addCategory(category)){
                category.ownerKey(ConfigurableCategoryQuery.query(ctype,"category").key());
                category.onEdge(true);
                if(applicationPreSetup.save(gameCluster,category)){
                    ConfigurableType type = category.configurableType();
                    type.ownerKey(ConfigurableTypeQuery.query(ctype,"type").key());
                    type.onEdge(true);
                    applicationPreSetup.save(gameCluster,type);
                    this.availableUpdates(ctype).forEach(c->{
                        gameCluster.configurableCategories(c).addCategory(category);
                        category.ownerKey(ConfigurableCategoryQuery.query(c,"category").key());
                        applicationPreSetup.edge(gameCluster,category,"category");
                        gameCluster.configurableTypes(c).addType(type);
                        type.ownerKey(ConfigurableTypeQuery.query(c,"type").key());
                        applicationPreSetup.edge(gameCluster,type,"type");
                    });
                    ConfigurableCategories categories = this.configurableCategories(ctype,gameCluster,applicationPreSetup);
                    categories.configurableTypes(this.configurableTypes(ctype,gameCluster,applicationPreSetup));
                    session.write(categories.toJson().toString().getBytes());
                }
                else{
                    session.write(JsonUtil.toSimpleResponse(false,category.name()+" failed to save").getBytes());
                }
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,category.name()+" already existed").getBytes());
            }
        }
        else if(session.action().equals("onUpdateCategorySettings")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(Long.parseLong(query[0]));
            ApplicationPreSetup applicationPreSetup = gameCluster.applicationPreSetup();
            JsonObject jo = JsonUtil.parse(payload).get("category").getAsJsonObject();
            JsonObject header = jo.get("header").getAsJsonObject();
            String ctype = header.get("scope").getAsString();
            int aix = ctype.indexOf('.');
            if(aix>0){
                ctype = ctype.substring(0,aix);
            }
            boolean updated = query[2].equals("save");
            ConfigurableCategories _categories = gameCluster.configurableCategories(ctype);
            ConfigurableCategory category = new ConfigurableCategory(jo);
            if((category = _categories.configurableSetting(category.name()))!=null){
                category.parse();
                if(category.scope.equals(ctype)){
                    if(updated){//do update
                        category.reset(jo);
                        applicationPreSetup.save(gameCluster,category);
                        session.write(JsonUtil.toSimpleResponse(false,"["+ category.name()+"] updated in scope["+ctype+"]").getBytes());
                    }
                    else{//do delete
                        session.write(JsonUtil.toSimpleResponse(false,"["+ category.name()+"] deleted in scope["+ctype+"]").getBytes());
                    }
                }
                else{
                    session.write(JsonUtil.toSimpleResponse(false,"["+category.scope+"] not matched in ["+ctype+"]").getBytes());
                }
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"["+ category.name()+"] not existed in scope["+ctype+"]").getBytes());
            }

        }
        else if (session.action().equals("onCreateAsset")||session.action().equals("onUpdateAsset")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(Long.parseLong(session.name()));
            ApplicationPreSetup applicationPreSetup = gameCluster.applicationPreSetup();
            session.write(createAsset(new Asset(),JsonUtil.parse(payload),gameCluster,applicationPreSetup).getBytes());
        }
        else if (session.action().equals("onCreateComponent") || session.action().equals("onUpdateComponent")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(Long.parseLong(session.name()));
            ApplicationPreSetup applicationPreSetup = gameCluster.applicationPreSetup();
            session.write(createComponent(new Component(),JsonUtil.parse(payload),gameCluster,applicationPreSetup).getBytes());
        }
        else if (session.action().equals("onCreateCommodity")||session.action().equals("onUpdateCommodity")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(Long.parseLong(session.name()));
            ApplicationPreSetup applicationPreSetup = gameCluster.applicationPreSetup();
            session.write(createCommodity(new Commodity(),JsonUtil.parse(payload),gameCluster,applicationPreSetup).getBytes());
        }
        else if (session.action().equals("onCreateItem")||session.action().equals("onUpdateItem")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(Long.parseLong(session.name()));
            ApplicationPreSetup applicationPreSetup = gameCluster.applicationPreSetup();
            session.write(createItem(new Item(),JsonUtil.parse(payload),gameCluster,applicationPreSetup).getBytes());
        }
        else if (session.action().equals("onCreateApplication")||session.action().equals("onUpdateApplication")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(Long.parseLong(session.name()));
            ApplicationPreSetup applicationPreSetup = gameCluster.applicationPreSetup();
            session.write(createApplication(new Application(),JsonUtil.parse(payload),gameCluster,applicationPreSetup).getBytes());
        }
        else if(session.action().equals("onStock")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(Long.parseLong(query[0]));
            ApplicationPreSetup preSetup = gameCluster.applicationPreSetup();
            Descriptor app = gameCluster.serviceWithCategory("item");
            //ConfigurableCategories categories = this.configurableCategories(Configurable.APPLICATION_CONFIG_TYPE,gameCluster,preSetup);
            String category = query[1].split("/")[1];
            //ConfigurableCategory configurableSetting = categories.configurableSetting(category);
            //if(configurableSetting != null && configurableSetting.scope.startsWith("application.")){
                //String serviceCategory = configurableSetting.scope.substring(12);
                //app = gameCluster.serviceWithCategory(serviceCategory);
            //}
            List<ConfigurableObject> items = preSetup.list(app,new ConfigurableObjectQuery(app.key(),category));
            session.write(new ItemAdminContext(true,query[1],items).toJson().toString().getBytes());
        }
        else if(session.action().equals("onVersionedStock")){
            this.context.log(session.name(),OnLog.WARN);
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(Long.parseLong(query[0]));
            ApplicationPreSetup preSetup = gameCluster.applicationPreSetup();
            Descriptor app = gameCluster.serviceWithCategory("item");
            List<ConfigurableObject> items = preSetup.list(app,new VersionedConfigurableObjectQuery(Long.parseLong(query[1])));//,"version"));
            session.write(new ItemAdminContext(true,query[1],items).toJson().toString().getBytes());
        }
        else if(session.action().equals("onDelete")){
            String[] query = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(Long.parseLong(query[0]));
            ApplicationPreSetup preSetup = gameCluster.applicationPreSetup();
            Descriptor app = gameCluster.serviceWithCategory("item");
            ConfigurableObject configurableObject = new ConfigurableObject();
            configurableObject.distributionKey(query[1]);
            if(preSetup.load(app,configurableObject)){
                session.write(JsonUtil.toSimpleResponse(preSetup.delete(app,configurableObject),"item deleted ["+query[1]+"]").getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"item not existed ["+query[1]+"]").getBytes());
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
        this.deploymentServiceProvider.registerConfigurableListener(GameCluster.GAME_CLUSTER_CONFIGURATION_TYPE,this);
        this.context.log("Game item admin module started", OnLog.WARN);
    }

    private ConfigurableCategories configurableCategories(String type,GameCluster gameCluster,ApplicationPreSetup applicationPreSetup){
        return gameCluster.configurableCategories(type);
    }
    private ConfigurableTypes configurableTypes(String name,GameCluster gameCluster,ApplicationPreSetup applicationPreSetup){
        return gameCluster.configurableTypes(name);
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
        ConfigurableCategory conf = configurableCategories.configurableSetting(app.configurationCategory());
        if(conf==null){
            return JsonUtil.toSimpleResponse(false," no config setting ["+app.configurationCategory()+"]");
        }
        Descriptor desc = gameCluster.serviceWithCategory("item");
        app.configurableSetting(conf);
        if(!applicationPreSetup.save(desc,app)){
            return JsonUtil.toSimpleResponse(true,"failed to save");
        }
        return new AssetSerializer().serialize(app,Application.class,null).toString();
    }
    private String createComponent(Component app,JsonObject payload,GameCluster gameCluster,ApplicationPreSetup applicationPreSetup){
        if(!app.configureAndValidate(payload)){
            return JsonUtil.toSimpleResponse(false,"invalid payload");
        }
        ConfigurableCategories configurableCategories = this.configurableCategories(Configurable.COMPONENT_CONFIG_TYPE,gameCluster,applicationPreSetup);
        ConfigurableCategory conf = configurableCategories.configurableSetting(app.configurationCategory());
        if(conf==null){
            return JsonUtil.toSimpleResponse(false," no config setting ["+app.configurationCategory()+"]");
        }
        Descriptor desc = gameCluster.serviceWithCategory("item");
        app.configurableSetting(conf);
        if(!applicationPreSetup.save(desc,app)) {
            return JsonUtil.toSimpleResponse(true,"failed to save");
        }
        return new ComponentSerializer().serialize(app,Application.class,null).toString();
    }
    private String createCommodity(Commodity app,JsonObject payload,GameCluster gameCluster,ApplicationPreSetup applicationPreSetup){
        if(!app.configureAndValidate(payload)){
            return JsonUtil.toSimpleResponse(false,"invalid payload");
        }
        ConfigurableCategories configurableCategories = this.configurableCategories(Configurable.COMMODITY_CONFIG_TYPE,gameCluster,applicationPreSetup);
        ConfigurableCategory conf = configurableCategories.configurableSetting(app.configurationCategory());
        if(conf==null){
            return JsonUtil.toSimpleResponse(false," no config setting ["+app.configurationCategory()+"]");
        }
        Descriptor desc = gameCluster.serviceWithCategory("item");
        app.configurableSetting(conf);
        if(!applicationPreSetup.save(desc,app)) {
            return JsonUtil.toSimpleResponse(true,"failed to save");
        }
        Category category = app.category(gameCluster.serviceWithCategory("inventory"));
        category.list();
        category.addItem(new CategoryItem(Configurable.COMMODITY_CONFIG_TYPE,conf.name(),app.configurationTypeId()));
        return new CommoditySerializer().serialize(app,Application.class,null).toString();
    }
    private String createItem(Item app,JsonObject payload,GameCluster gameCluster,ApplicationPreSetup applicationPreSetup){
        if(!app.configureAndValidate(payload)){
            return JsonUtil.toSimpleResponse(false,"invalid payload");
        }
        ConfigurableCategories configurableCategories = this.configurableCategories(Configurable.ITEM_CONFIG_TYPE,gameCluster,applicationPreSetup);
        ConfigurableCategory conf = configurableCategories.configurableSetting(app.configurationCategory());
        if(conf==null){
            return JsonUtil.toSimpleResponse(false," no config setting ["+app.configurationCategory()+"]");
        }
        Descriptor desc = gameCluster.serviceWithCategory("item");
        app.configurableSetting(conf);
        if(!applicationPreSetup.save(desc,app)) {
            return JsonUtil.toSimpleResponse(true,"failed to save");
        }
        return new ItemSerializer().serialize(app,Application.class,null).toString();
    }
    public String createApplication(Application app,JsonObject payload,GameCluster gameCluster,ApplicationPreSetup applicationPreSetup){
        if(!app.configureAndValidate(payload)){
            return JsonUtil.toSimpleResponse(false,"invalid payload");
        }
        ConfigurableCategories configurableCategories = this.configurableCategories(Configurable.APPLICATION_CONFIG_TYPE,gameCluster,applicationPreSetup);
        ConfigurableCategory conf = configurableCategories.configurableSetting(app.configurationCategory());
        if(conf==null){
            return JsonUtil.toSimpleResponse(false," no config setting ["+app.configurationCategory()+"]");
        }
        Descriptor desc = gameCluster.serviceWithCategory(app.configurationTypeId());
        app.disabled(true);
        app.configurableSetting(conf);
        if(!applicationPreSetup.save(desc,app)){
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
        this.configurableCategories(Configurable.APPLICATION_CONFIG_TYPE, gameCluster, applicationPreSetup);
        assets.toCategories().forEach((c -> {
            c.ownerKey(ConfigurableCategoryQuery.ComponentKey);
            applicationPreSetup.edge(gameCluster,c,"category");
            c.ownerKey(ConfigurableCategoryQuery.CommodityKey);
            applicationPreSetup.edge(gameCluster,c,"category");
            c.ownerKey(ConfigurableCategoryQuery.ItemKey);
            applicationPreSetup.edge(gameCluster,c,"category");
            c.ownerKey(ConfigurableCategoryQuery.ApplicationKey);
            applicationPreSetup.edge(gameCluster,c,"category");
        }));
        components.toCategories().forEach((c -> {
            c.ownerKey(ConfigurableCategoryQuery.CommodityKey);
            applicationPreSetup.edge(gameCluster,c,"category");
            c.ownerKey(ConfigurableCategoryQuery.ItemKey);
            applicationPreSetup.edge(gameCluster,c,"category");
            c.ownerKey(ConfigurableCategoryQuery.ApplicationKey);
            applicationPreSetup.edge(gameCluster,c,"category");
        }));
        commodities.toCategories().forEach((c -> {
            c.ownerKey(ConfigurableCategoryQuery.ItemKey);
            applicationPreSetup.edge(gameCluster,c,"category");
            c.ownerKey(ConfigurableCategoryQuery.ApplicationKey);
            applicationPreSetup.edge(gameCluster,c,"category");
        }));
        items.toCategories().forEach((c -> {
            c.ownerKey(ConfigurableCategoryQuery.ApplicationKey);
            applicationPreSetup.edge(gameCluster,c,"category");
        }));
        //this.configurableCategories(Configurable.APPLICATION_CONFIG_TYPE, gameCluster, applicationPreSetup);

        ConfigurableTypes assetTypes = this.configurableTypes(Configurable.ASSET_CONFIG_TYPE, gameCluster, applicationPreSetup);
        ConfigurableTypes componentTypes = this.configurableTypes(Configurable.COMPONENT_CONFIG_TYPE, gameCluster, applicationPreSetup);
        ConfigurableTypes commodityTypes = this.configurableTypes(Configurable.COMMODITY_CONFIG_TYPE, gameCluster, applicationPreSetup);
        ConfigurableTypes itemTypes = this.configurableTypes(Configurable.ITEM_CONFIG_TYPE, gameCluster, applicationPreSetup);
        this.configurableTypes(Configurable.APPLICATION_CONFIG_TYPE, gameCluster, applicationPreSetup);
        assetTypes.toTypes().forEach((t) -> {
            t.ownerKey(ConfigurableTypeQuery.ComponentKey);
            applicationPreSetup.edge(gameCluster,t,"type");
            t.ownerKey(ConfigurableTypeQuery.CommodityKey);
            applicationPreSetup.edge(gameCluster,t,"type");
            t.ownerKey(ConfigurableTypeQuery.ItemKey);
            applicationPreSetup.edge(gameCluster,t,"type");
            t.ownerKey(ConfigurableTypeQuery.ApplicationKey);
            applicationPreSetup.edge(gameCluster,t,"type");
        });
        componentTypes.toTypes().forEach((t) -> {
            t.ownerKey(ConfigurableTypeQuery.CommodityKey);
            applicationPreSetup.edge(gameCluster,t,"type");
            t.ownerKey(ConfigurableTypeQuery.ItemKey);
            applicationPreSetup.edge(gameCluster,t,"type");
            t.ownerKey(ConfigurableTypeQuery.ApplicationKey);
            applicationPreSetup.edge(gameCluster,t,"type");
        });
        commodityTypes.toTypes().forEach((t) -> {
            t.ownerKey(ConfigurableTypeQuery.ItemKey);
            applicationPreSetup.edge(gameCluster,t,"type");
            t.ownerKey(ConfigurableTypeQuery.ApplicationKey);
            applicationPreSetup.edge(gameCluster,t,"type");
        });
        itemTypes.toTypes().forEach((t) -> {
            t.ownerKey(ConfigurableTypeQuery.ApplicationKey);
            applicationPreSetup.edge(gameCluster,t,"type");
        });

        //this.onLoaded(gameCluster);
        //pre-defined lobby configurations
        //log lobby apps
        Configuration lobbyConfiguration = this.context.configuration("lobby");
        String mode = gameCluster.mode;//(String)gameCluster.property(GameCluster.MODE);
        JsonObject roomPayload = ((JsonElement) lobbyConfiguration.property(mode+"Room")).getAsJsonObject();
        JsonObject zonePayload = ((JsonElement)lobbyConfiguration.property(mode+"Zone")).getAsJsonObject();
        Component room = new Component();
        createComponent(room, roomPayload.getAsJsonObject(), gameCluster, applicationPreSetup);

        zonePayload.get("application").getAsJsonObject().get("Room").getAsJsonArray().add(room.distributionId());
        JsonArray refs = zonePayload.get("reference").getAsJsonArray();
        refs.add(room.distributionId());
        JsonArray arenas = ((JsonElement)lobbyConfiguration.property("arenas")).getAsJsonArray();
        JsonArray aset = zonePayload.get("application").getAsJsonObject().get("ArenaSet").getAsJsonArray();
        arenas.forEach(arenaPayload -> {
            Commodity arena = new Commodity();
            createCommodity(arena,arenaPayload.getAsJsonObject(),gameCluster,applicationPreSetup);
            aset.add(arena.distributionId());
            refs.add(arena.distributionId());
        });
        Item zone = new Item();
        createItem(zone,zonePayload,gameCluster,applicationPreSetup);
        JsonObject lobbyPayload = ((JsonElement)lobbyConfiguration.property("lobby")).getAsJsonObject();
        lobbyPayload.get("application").getAsJsonObject().get("ZoneSet").getAsJsonArray().add(zone.distributionId());
        lobbyPayload.get("reference").getAsJsonArray().add(zone.distributionId());
        Application lobby = new Application();
        this.context.log(createApplication(lobby,lobbyPayload,gameCluster,applicationPreSetup),OnLog.WARN);
        //pre-defined third party validators
        Configuration configuration = this.context.configuration((gameCluster.name()).toLowerCase());
        if(configuration!=null) {
            try{
                JsonArray validators = ((JsonElement)configuration.property("validators")).getAsJsonArray();
                validators.forEach(validator->{
                    Application app = new Application();
                    createApplication(app,validator.getAsJsonObject(),gameCluster,applicationPreSetup);
                });
            }catch (Exception ex){
                this.context.log("unexpected error",ex,OnLog.ERROR);
            }
        }

        Configuration assetConfiguration = this.context.configuration("asset");
        if(assetConfiguration!=null){
            try{
                JsonArray configs = ((JsonElement)assetConfiguration.property("list")).getAsJsonArray();
                configs.forEach(config->{
                    Asset app = new Asset();
                    createAsset(app,config.getAsJsonObject(),gameCluster,applicationPreSetup);
                });
            }catch (Exception ex){
                this.context.log("unexpected error",ex,OnLog.ERROR);
            }
        }

        Configuration componentConfiguration = this.context.configuration("component");
        if(componentConfiguration!=null){
            try{
                JsonArray configs = ((JsonElement)componentConfiguration.property("list")).getAsJsonArray();
                configs.forEach(config->{
                    Component app = new Component();
                    createComponent(app,config.getAsJsonObject(),gameCluster,applicationPreSetup);
                });
            }catch (Exception ex){
                this.context.log("unexpected error",ex,OnLog.ERROR);
            }
        }

        Configuration commodityConfiguration = this.context.configuration("commodity");
        if(commodityConfiguration!=null){
            try{
                JsonArray configs = ((JsonElement)commodityConfiguration.property("list")).getAsJsonArray();
                configs.forEach(config->{
                    Commodity app = new Commodity();
                    createCommodity(app,config.getAsJsonObject(),gameCluster,applicationPreSetup);
                });
            }catch (Exception ex){
                this.context.log("unexpected error",ex,OnLog.ERROR);
            }
        }
    }

    @Override
    public void onLoaded(GameCluster gameCluster){
        gameCluster.configurableCategories(Configurable.ASSET_CONFIG_TYPE);
        gameCluster.configurableCategories(Configurable.COMPONENT_CONFIG_TYPE);
        gameCluster.configurableCategories(Configurable.COMMODITY_CONFIG_TYPE);
        gameCluster.configurableCategories(Configurable.ITEM_CONFIG_TYPE);
        gameCluster.configurableCategories(Configurable.APPLICATION_CONFIG_TYPE);
        gameCluster.configurableTypes(Configurable.ASSET_CONFIG_TYPE);
        gameCluster.configurableTypes(Configurable.COMPONENT_CONFIG_TYPE);
        gameCluster.configurableTypes(Configurable.COMMODITY_CONFIG_TYPE);
        gameCluster.configurableTypes(Configurable.ITEM_CONFIG_TYPE);
        gameCluster.configurableTypes(Configurable.APPLICATION_CONFIG_TYPE);
    }
}
