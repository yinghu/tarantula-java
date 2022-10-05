package com.tarantula.platform.inventory;

import com.google.gson.JsonArray;
import com.icodesoftware.*;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.achievement.Achievement;
import com.tarantula.platform.item.*;
import com.tarantula.platform.presence.DailyGiveaway;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.store.ShoppingItem;
import com.tarantula.platform.tournament.TournamentPrize;

import java.util.ArrayList;
import java.util.List;


public class PlatformInventoryServiceProvider implements ServiceProvider {
    private TarantulaLogger logger;

    private final String gameServiceName;
    private GameCluster gameCluster;
    private ServiceContext serviceContext;
    private ApplicationPreSetup applicationPreSetup;
    private DataStore inventoryDataStore;
    public PlatformInventoryServiceProvider(GameCluster gameCluster){
        this.gameCluster = gameCluster;
        this.gameServiceName = (String)gameCluster.property(GameCluster.GAME_SERVICE);
    }

    @Override
    public String name() {
        return "inventory";
    }

    @Override
    public void start() throws Exception {
        logger.warn("Inventory service provider started->"+gameServiceName);
    }

    @Override
    public void shutdown() throws Exception {

    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.applicationPreSetup = gameCluster.applicationPreSetup();//SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
        this.inventoryDataStore = this.applicationPreSetup.dataStore(gameCluster,name());
        this.logger = serviceContext.logger(PlatformItemServiceProvider.class);
    }
    public DataStore inventoryDataStore(){
        return this.inventoryDataStore;
    }
    public Category category(){
        return category((ci)->ci.configurationType().equals(Configurable.COMMODITY_CONFIG_TYPE));
    }
    public List<Inventory> inventoryList(String systemId){
        List<Inventory> inventoryList = new ArrayList<>();
        category((ci)->{
            if(ci.configurationType().equals(Configurable.COMMODITY_CONFIG_TYPE)){
                Inventory inventory = inventory(systemId,ci.configurationCategory(),ci.configurationTypeId());
                if(inventory!=null) inventoryList.add(inventory);
                return true;
            }
            return false;
        });
        return inventoryList;
    }

    public Inventory inventory(String systemId,String category,String typeId){
        Inventory inventory = new Inventory(category,typeId);
        inventory.distributionKey(systemId);
        if(!inventoryDataStore.load(inventory)) return null;
        inventory.dataStore(inventoryDataStore);
        inventory.list();
        return inventory;
    }
    public boolean redeem(String systemId, Application item){
        ApplicationRedeemer redeemer = new ApplicationRedeemer(systemId,this);
        redeemer.distributionKey(item.distributionKey());
        GameCluster _gc = this.serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory(item.configurationTypeId());
        if(app==null||!applicationPreSetup.load(app,redeemer)) return false;
        redeemer.redeem();
        return true;
    }
    public boolean redeem(String systemId, Achievement item){
        ApplicationRedeemer redeemer = new ApplicationRedeemer(systemId,this);
        redeemer.distributionKey(item.distributionKey());
        GameCluster _gc = this.serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory(item.configurationTypeId());
        if(app==null||!applicationPreSetup.load(app,redeemer)) return false;
        redeemer.redeem();
        return true;
    }
    public boolean redeem(String systemId, DailyGiveaway item){
        ApplicationRedeemer redeemer = new ApplicationRedeemer(systemId,this);
        redeemer.distributionKey(item.distributionKey());
        GameCluster _gc = this.serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory(item.configurationTypeId());
        if(app==null||!applicationPreSetup.load(app,redeemer)) return false;
        redeemer.redeem();
        return true;
    }
    public boolean redeem(String systemId, ShoppingItem item){
        ApplicationRedeemer redeemer = new ApplicationRedeemer(systemId,this);
        redeemer.distributionKey(item.distributionKey());
        GameCluster _gc = this.serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory(item.configurationTypeId());
        if(app==null||!applicationPreSetup.load(app,redeemer)) return false;
        redeemer.redeem();
        return true;
    }
    public boolean redeem(String systemId, TournamentPrize item){
        ApplicationRedeemer redeemer = new ApplicationRedeemer(systemId,this);
        redeemer.distributionKey(item.distributionKey());
        GameCluster _gc = this.serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory(item.configurationTypeId());
        if(app==null||!applicationPreSetup.load(app,redeemer)) return false;
        Descriptor itemApp = _gc.serviceWithCategory("item");
        redeemer.dataStore(applicationPreSetup.dataStore(itemApp));
        redeemer.redeem();
        return true;
    }

    public Inventory newInventory(String category,String typeId){
        ConfigurableCategories categories = this.configurableCategories(Configurable.COMMODITY_CONFIG_TYPE,gameCluster,applicationPreSetup);
        ConfigurableSetting conf = categories.configurableSetting(category);
        return new Inventory(conf.type,typeId,conf.rechargeable);
    }

    private Category category(Category.Filter filter){
        GameCluster _gameCluster = this.serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gameCluster.serviceWithCategory("item");
        ApplicationPreSetup preSetup = _gameCluster.applicationPreSetup();//SystemUtil.applicationPreSetup((String) _gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
        Category category = new Category();
        category.distributionKey(app.distributionKey());
        preSetup.load(app,category);
        category.list((ci)-> filter.onFilter(ci));
        return category;
    }
    private ConfigurableTemplate categoryTemplateSetting(GameCluster gameCluster,String name){
        if(name.equals(Configurable.ASSET_CONFIG_TYPE))
            return this.serviceContext.deploymentServiceProvider().configuration(gameCluster,GameCluster.GAME_ASSET_CATEGORY_TEMPLATE);
        if(name.equals(Configurable.COMPONENT_CONFIG_TYPE))
            return this.serviceContext.deploymentServiceProvider().configuration(gameCluster,GameCluster.GAME_COMPONENT_CATEGORY_TEMPLATE);
        if(name.equals(Configurable.COMMODITY_CONFIG_TYPE))
            return this.serviceContext.deploymentServiceProvider().configuration(gameCluster,GameCluster.GAME_COMMODITY_CATEGORY_TEMPLATE);
        if(name.equals(Configurable.ITEM_CONFIG_TYPE))
            return this.serviceContext.deploymentServiceProvider().configuration(gameCluster,GameCluster.GAME_ITEM_CATEGORY_TEMPLATE);
        if(name.equals(Configurable.APPLICATION_CONFIG_TYPE))
            return this.serviceContext.deploymentServiceProvider().configuration(gameCluster,GameCluster.GAME_APPLICATION_CATEGORY_TEMPLATE);
        return null;
    }
    private ConfigurableCategories configurableCategories(String type,GameCluster gameCluster,ApplicationPreSetup applicationPreSetup){
        ConfigurableCategories categories = new ConfigurableCategories();
        categories.name(type);
        if(!applicationPreSetup.load(gameCluster,categories)){
            ConfigurableTemplate configuration = this.categoryTemplateSetting(gameCluster,type);
            JsonArray cclasses = (JsonArray)configuration.property("itemList");
            cclasses.forEach((c)->{
                categories.addCategory(c.getAsJsonObject());
            });
            applicationPreSetup.save(gameCluster,categories);
        }
        return categories;
    }
}
