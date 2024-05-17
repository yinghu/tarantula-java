package com.tarantula.platform;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ApplicationSchema;
import com.icodesoftware.service.OnLobby;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.service.GameConfigurationSetup;
import com.tarantula.game.service.GameObjectSetup;
import com.tarantula.platform.event.PortableEventRegistry;
import com.tarantula.platform.inventory.UserInventory;
import com.tarantula.platform.item.*;
import com.icodesoftware.service.ApplicationPreSetup;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameCluster extends OnApplicationHeader implements ApplicationSchema,Portable, ApplicationPreSetup.Listener, Inventory.Listener {

    private TarantulaLogger logger = JDKLogger.getLogger(GameCluster.class);

    public final static String LABEL = "gameCluster";

    public final static String GAME_CLUSTER_CONFIGURATION_TYPE = "GameCluster";

    public Lobby gameLobby;
    public Lobby serviceLobby;
    public Lobby dataLobby;


    public final static String GAME_ASSET_CATEGORY_TEMPLATE = "assets";
    public final static String GAME_COMPONENT_CATEGORY_TEMPLATE = "components";
    public final static String GAME_COMMODITY_CATEGORY_TEMPLATE = "commodities";
    public final static String GAME_ITEM_CATEGORY_TEMPLATE = "items";
    public final static String GAME_APPLICATION_CATEGORY_TEMPLATE = "applications";
    public final static String GAME_COMMON_TYPE_TEMPLATE = "common-type-settings";



    protected ServiceContext serviceContext;

    protected ApplicationPreSetup applicationPreSetup;

    protected CopyOnWriteArrayList<Inventory.Listener> onInventory = new CopyOnWriteArrayList<>();

    protected CopyOnWriteArrayList<ApplicationPreSetup.Listener> listeners = new CopyOnWriteArrayList<>();

    public String mode;

    public String gameLobbyName;

    public String gameServiceName;

    public String gameDataName;

    public String developer;

    public String gameIcon;

    public String developerIcon;

    public String gameServiceProvider;

    public boolean dedicated;

    public boolean tournamentEnabled;

    public int maxLobbyCount;

    public int maxZoneCount;

    public int maxArenaCount;

    public int maxDataSize;

    public int upgradeVersion;
    //public long publishingId;
    public long accountId;
    public GameCluster(){
        this.onEdge = true;
    }

    @Override
    public int getClassId() {
       return PortableEventRegistry.GAME_CLUSTER_CID;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeBoolean("1",successful);
        portableWriter.writeUTF("2",message);
        if(successful){
            //portableWriter.writeUTF("3",this.bucket);
            portableWriter.writeLong("4",distributionId);
        }
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        successful = portableReader.readBoolean("1");
        message = portableReader.readUTF("2");
        if(successful){
            //bucket = portableReader.readUTF("3");
            distributionId = portableReader.readLong("4");
        }
    }
    public JsonObject toJson(){
        JsonObject jo = new JsonObject();
        jo.addProperty("successful",successful);
        jo.addProperty("message",message);
        jo.addProperty("gameClusterId",Long.toString(distributionId));
        jo.addProperty("name",name);
        jo.addProperty("mode",mode);
        jo.addProperty("typeId",typeId());
        jo.addProperty("gameLobby",gameLobbyName);
        jo.addProperty("gameService",gameServiceName);
        jo.addProperty("gameData",gameDataName);
        jo.addProperty("developer",developer);
        jo.addProperty("gameIcon",gameIcon);
        jo.addProperty("developerIcon",developerIcon);
        jo.addProperty("tournamentEnabled",tournamentEnabled);
        jo.addProperty("dedicated",dedicated);
        jo.addProperty("disabled",disabled);
        jo.addProperty("maxLobbyCount",maxLobbyCount);
        jo.addProperty("maxZoneCount",maxZoneCount);
        jo.addProperty("maxArenaCount",maxArenaCount);
        jo.addProperty("gameServiceProvider",gameServiceProvider);
        return jo;
    }

    public boolean write(DataBuffer buffer){
        buffer.writeUTF8(name);
        buffer.writeUTF8(mode);
        buffer.writeUTF8(gameServiceProvider);
        buffer.writeUTF8(gameLobbyName);
        buffer.writeUTF8(gameServiceName);
        buffer.writeUTF8(gameDataName);
        buffer.writeUTF8(developer);
        buffer.writeUTF8(developerIcon);
        buffer.writeUTF8(gameIcon);
        buffer.writeBoolean(tournamentEnabled);
        buffer.writeBoolean(dedicated);
        buffer.writeBoolean(disabled);
        buffer.writeInt(maxLobbyCount);
        buffer.writeInt(maxZoneCount);
        buffer.writeInt(maxArenaCount);
        buffer.writeLong(accountId);
        buffer.writeInt(maxDataSize);
        buffer.writeInt(upgradeVersion);
        return true;
    }
    public boolean read(DataBuffer buffer) {
        name = buffer.readUTF8();
        mode = buffer.readUTF8();
        gameServiceProvider = buffer.readUTF8();
        gameLobbyName = buffer.readUTF8();
        gameServiceName = buffer.readUTF8();
        gameDataName = buffer.readUTF8();
        developer = buffer.readUTF8();
        developerIcon = buffer.readUTF8();
        gameIcon = buffer.readUTF8();
        tournamentEnabled = buffer.readBoolean();
        dedicated = buffer.readBoolean();
        disabled = buffer.readBoolean();
        maxLobbyCount = buffer.readInt();
        maxZoneCount = buffer.readInt();
        maxArenaCount = buffer.readInt();
        accountId = buffer.readLong();
        maxDataSize = buffer.readInt();
        upgradeVersion = buffer.readInt();
        return true;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    public Descriptor serviceWithCategory(String category){
        if(serviceLobby==null){
            return null;
        }
        Descriptor loaded =null;
        for(Descriptor d : serviceLobby.entryList()){
            if(d.category().equals(category)){
                loaded = d;
                break;
            }
        }
        if(loaded!=null) return loaded;
        for(Descriptor d : dataLobby.entryList()){
            if(d.category().equals(category)){
                loaded = d;
                break;
            }
        }
        return loaded;
    }

    public void setup(ServiceContext serviceContext){
        try{
            this.serviceContext = serviceContext;
            String deployDir = serviceContext.node().deployDirectory();
            Path _config_game = Paths.get(deployDir+"/conf/"+this.name);
            if(!Files.exists(_config_game)){
                Path _config_assets = Paths.get(_config_game.toString(),"assets");
                Path _config_components = Paths.get(_config_game.toString(),"components");
                Path _config_commodities = Paths.get(_config_game.toString(),"commodities");
                Path _config_items = Paths.get(_config_game.toString(),"items");
                Path _config_applications = Paths.get(_config_game.toString(),"applications");
                Files.createDirectories(_config_assets);
                Files.createDirectories(_config_components);
                Files.createDirectories(_config_commodities);
                Files.createDirectories(_config_items);
                Files.createDirectories(_config_applications);
                URL url = Thread.currentThread().getContextClassLoader().getResource("config-template");
                copyTemplateFile(url,_config_game);

                URL srcAssets = Thread.currentThread().getContextClassLoader().getResource("config-template/assets");
                copyTemplateFile(srcAssets,_config_assets);

                URL srcComponents = Thread.currentThread().getContextClassLoader().getResource("config-template/components");
                copyTemplateFile(srcComponents,_config_components);

                URL srcCommodities = Thread.currentThread().getContextClassLoader().getResource("config-template/commodities");
                copyTemplateFile(srcCommodities,_config_commodities);

                URL srcItems = Thread.currentThread().getContextClassLoader().getResource("config-template/items");
                copyTemplateFile(srcItems,_config_items);

                URL srcApplications = Thread.currentThread().getContextClassLoader().getResource("config-template/applications");
                copyTemplateFile(srcApplications,_config_applications);
            }
            Path _web_game = Paths.get(deployDir+"/web/"+this.name);
            if(!Files.exists(_web_game)){
                Files.createDirectories(_web_game);
            }
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    private void copyTemplateFile(URL src,Path dest) throws Exception{
        String[] fs = new File(src.getFile()).list((m, n)-> n.endsWith(".json"));
        String _path = src.getFile();
        int wp = _path.indexOf(":");
        if(wp>0){
            _path = _path.substring(wp+1);
        }
        for(String f : fs){
            Path _item = Paths.get(_path+"/"+f);
            Path _game = Paths.get(dest+"/"+f);
            Files.copy(_item,_game, StandardCopyOption.COPY_ATTRIBUTES);
        }
    }

    public ApplicationPreSetup applicationPreSetup(){
        if(applicationPreSetup!=null) return applicationPreSetup;
        applicationPreSetup = new GameObjectSetup(this);
        applicationPreSetup.setup(serviceContext);
        return applicationPreSetup;
    }

    public Transaction transaction(){
        Transaction transaction = serviceContext.transaction(Distributable.DATA_SCOPE);
        ApplicationPreSetup preSetup = new GameConfigurationSetup(this);
        preSetup.setup(serviceContext);
        transaction.register(preSetup,this);
        return transaction;
    }

    public Descriptor application(String category){
        return serviceWithCategory(category);
    }
    @Override
    public String typeId(){
        return serviceType().replace("-service","");
    }


    public String playMode(){
        return this.mode;
    }

    public String lobbyType(){
        return gameLobbyName;
    }

    public String serviceType(){
        return gameServiceName;
    }

    public String dataType(){
        return gameDataName;
    }

    public boolean tournamentEnabled(){
        return tournamentEnabled;
    }

    public boolean dedicated(){
        return dedicated;
    }


    public int maxLobbyCount(){
       return maxLobbyCount;
    }

    public int maxZoneCount(){
        return maxZoneCount;
    }

    public int maxArenaCount(){
        return maxArenaCount;
    }

    public int maxDataSizeCount(){
        return maxDataSize;
    }

    public int upgradeVersion(){
        return upgradeVersion;
    }

    public long accountId(){
        return accountId;
    }
    @Override
    public <T extends Configurable> void onUpdated(Descriptor application,T t) {
        listeners.forEach(l->l.onUpdated(application,t));
    }
    @Override
    public <T extends Configurable> void onCreated(Descriptor application,T t) {
        //reset(t,true);
        listeners.forEach(l->l.onCreated(application,t));
    }
    @Override
    public <T extends Configurable> void onDeleted(Descriptor application,T t) {
        listeners.forEach(l->l.onDeleted(application,t));
    }
    @Override
    public <T extends Configurable> void onUpdated(ApplicationSchema application,T t){
        listeners.forEach(l->l.onUpdated(application,t));
    }
    @Override
    public <T extends Configurable> void onCreated(ApplicationSchema application,T t){
        listeners.forEach(l->l.onCreated(application,t));
    }

    public <T extends Configurable> void onDeleted(ApplicationSchema application,T t){
        listeners.forEach(l->l.onDeleted(application,t));
    }
    @Override
    public void onUpdated(OnLobby onLobby) {
        if(onLobby.gameClusterId()!=(this.distributionId())) return;
        if(onLobby.typeId().equals(gameLobbyName)){
            this.gameLobby = this.serviceContext.deploymentServiceProvider().lobby(gameLobbyName);
            return;
        }
        if(onLobby.typeId().equals(gameDataName)){
            this.dataLobby = this.serviceContext.deploymentServiceProvider().lobby(gameDataName);
            return;
        }
        if(onLobby.typeId().equals(gameServiceName)){
            this.serviceLobby = this.serviceContext.deploymentServiceProvider().lobby(gameServiceName);
        }
    }

    public void addListener(ApplicationPreSetup.Listener listener){
        this.listeners.add(listener);
    }
    public void addListener(Inventory.Listener listener){
        this.onInventory.add(listener);
    }

    public ConfigurableCategories configurableCategories(String type){
        ApplicationPreSetup preSetup = applicationPreSetup();
        return configurableCategories(preSetup,type);
    }

    public ConfigurableCategories configurableCategories(ApplicationPreSetup preSetup,String type){
        ConfigurableCategories categories = new ConfigurableCategories();
        categories.name(type);
        ConfigurableCategoryQuery query = ConfigurableCategoryQuery.query(type,"category");
        ConfigurableTypeQuery typeQuery = ConfigurableTypeQuery.query(type,"type");
        preSetup.list(this, query).forEach(c->{
            categories.addCategory(c);
        });
        ConfigurableTemplate configuration = this.categoryTemplateSetting(type);
        JsonArray cdata = (JsonArray)configuration.property("itemList");
        cdata.forEach((c)->{
            ConfigurableCategory category = new ConfigurableCategory(c.getAsJsonObject());
            if(categories.addCategory(category)){
                category.onEdge(true);
                category.label("category");
                category.ownerKey(query.key());
                preSetup.save(this,category);
                ConfigurableType ty = category.configurableType();
                ty.onEdge(true);
                ty.label("type");
                ty.ownerKey(typeQuery.key());
                preSetup.save(this,ty);
                logger.warn("category added->"+ty.toJson());
            }
        });
        return categories;
    }
    public ConfigurableTypes configurableTypes(String name){
        ConfigurableTypes configurableTypes = new ConfigurableTypes();
        configurableTypes.name(name);
        ConfigurableTypeQuery query = ConfigurableTypeQuery.query(name,"type");
        applicationPreSetup().list(this,query).forEach(t->{
            configurableTypes.addType(t);
        });
        Configuration commonTypes = this.serviceContext.deploymentServiceProvider().configuration(this,GameCluster.GAME_COMMON_TYPE_TEMPLATE);
        JsonArray ctypes = (JsonArray)commonTypes.property("itemList");
        ctypes.forEach((c)-> {
            ConfigurableType t  = new ConfigurableType(c.getAsJsonObject());
            if(configurableTypes.addType(t)){
                t.onEdge(true);
                t.label("type");
                t.ownerKey(query.key());
                applicationPreSetup().save(this,t);
            }
        });
        return configurableTypes;
    }

    private ConfigurableTemplate categoryTemplateSetting(String name){
        if(name.equals(Configurable.ASSET_CONFIG_TYPE))
            return this.serviceContext.deploymentServiceProvider().configuration(this,GameCluster.GAME_ASSET_CATEGORY_TEMPLATE);
        if(name.equals(Configurable.COMPONENT_CONFIG_TYPE))
            return this.serviceContext.deploymentServiceProvider().configuration(this,GameCluster.GAME_COMPONENT_CATEGORY_TEMPLATE);
        if(name.equals(Configurable.COMMODITY_CONFIG_TYPE))
            return this.serviceContext.deploymentServiceProvider().configuration(this,GameCluster.GAME_COMMODITY_CATEGORY_TEMPLATE);
        if(name.equals(Configurable.ITEM_CONFIG_TYPE))
            return this.serviceContext.deploymentServiceProvider().configuration(this,GameCluster.GAME_ITEM_CATEGORY_TEMPLATE);
        if(name.equals(Configurable.APPLICATION_CONFIG_TYPE))
            return this.serviceContext.deploymentServiceProvider().configuration(this,GameCluster.GAME_APPLICATION_CATEGORY_TEMPLATE);
        return null;
    }

    @Override
    public void afterCommit(long transactionId) {

    }

    @Override
    public void afterAbort(long transactionId,Exception exception) {
        if(exception!=null) exception.printStackTrace();
    }

    public Inventory createInventory(ApplicationPreSetup applicationPreSetup,String category,String typeId){
        ConfigurableCategories categories = this.configurableCategories(applicationPreSetup,Configurable.COMMODITY_CONFIG_TYPE);
        ConfigurableCategory conf = categories.configurableSetting(category);
        conf.parse();
        Inventory inventory = new UserInventory(conf.name(),typeId,conf.rechargeable,conf.constrained,this);
        return inventory;
    }

    @Override
    public void onInventory(ApplicationPreSetup applicationPreSetup,Inventory inventory, Inventory.Stock inventoryItem) {
        onInventory.forEach(listener -> listener.onInventory(applicationPreSetup,inventory,inventoryItem));
    }

    public String gameServiceProvider(){
        return gameServiceProvider;
    }
}
