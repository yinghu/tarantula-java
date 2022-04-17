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
import com.tarantula.platform.util.SystemUtil;

import java.util.ArrayList;
import java.util.List;


public class PlatformInventoryServiceProvider implements ServiceProvider {
    private TarantulaLogger logger;

    private final String name;
    private GameCluster gameCluster;
    private ServiceContext serviceContext;
    private ApplicationPreSetup applicationPreSetup;
    public PlatformInventoryServiceProvider(GameCluster gameCluster){
        this.name = (String)gameCluster.property(GameCluster.GAME_SERVICE);
        this.gameCluster = gameCluster;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void start() throws Exception {
        logger.warn("Inventory service provider started");
    }

    @Override
    public void shutdown() throws Exception {

    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.applicationPreSetup = SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
        this.logger = serviceContext.logger(PlatformItemServiceProvider.class);
    }
    public Category category(){
        return category((ci)->ci.configurationType().equals(Configurable.COMMODITY_CONFIG_TYPE));
    }
    public List<Inventory> inventoryList(String systemId){
        List<Inventory> inventoryList = new ArrayList<>();
        category((ci)->{
            if(ci.configurationType().equals(Configurable.COMMODITY_CONFIG_TYPE)){
                Inventory inventory = inventory(systemId,ci.configurationCategory());
                if(inventory!=null) inventoryList.add(inventory);
                return true;
            }
            return false;
        });
        return inventoryList;
    }
    public Inventory inventory(String systemId,String category){
        Inventory inventory = new Inventory(category);
        inventory.distributionKey(systemId);
        GameCluster _gc = this.serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory("store");
        if(!this.applicationPreSetup.load(serviceContext,app,inventory)) return null;
        inventory.list();
        return inventory;
    }
    public boolean redeem(String systemId, Application item){
        ApplicationRedeemer redeemer = new ApplicationRedeemer(systemId,this);
        redeemer.distributionKey(item.distributionKey());
        GameCluster _gc = this.serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory(item.configurationCategory());
        if(app==null||!applicationPreSetup.load(serviceContext,app,redeemer)) return false;
        redeemer.redeem();
        return true;
    }
    public boolean redeem(String systemId, Achievement item){
        ApplicationRedeemer redeemer = new ApplicationRedeemer(systemId,this);
        redeemer.distributionKey(item.distributionKey());
        GameCluster _gc = this.serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory(item.configurationCategory());
        if(app==null||!applicationPreSetup.load(serviceContext,app,redeemer)) return false;
        redeemer.redeem();
        return true;
    }
    public boolean redeem(String systemId, DailyGiveaway item){
        ApplicationRedeemer redeemer = new ApplicationRedeemer(systemId,this);
        redeemer.distributionKey(item.distributionKey());
        GameCluster _gc = this.serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory(item.configurationCategory());
        if(app==null||!applicationPreSetup.load(serviceContext,app,redeemer)) return false;
        redeemer.redeem();
        return true;
    }
    public boolean redeem(String systemId, ShoppingItem item){
        ApplicationRedeemer redeemer = new ApplicationRedeemer(systemId,this);
        redeemer.distributionKey(item.distributionKey());
        GameCluster _gc = this.serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory(item.configurationCategory());
        if(app==null||!applicationPreSetup.load(serviceContext,app,redeemer)) return false;
        redeemer.redeem();
        return true;
    }
    public boolean redeem(String systemId, Tournament.Prize item){
        ItemRedeemer redeemer = new ItemRedeemer(systemId,this);
        redeemer.distributionKey(item.distributionKey());
        GameCluster _gc = this.serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory(item.configurationCategory());
        if(app==null||!applicationPreSetup.load(serviceContext,app,redeemer)) return false;
        redeemer.redeem();
        return true;
    }

    public Inventory newInventory(String category){
        ConfigurableCategories categories = this.configurableCategories(Configurable.COMMODITY_CONFIG_TYPE,gameCluster,applicationPreSetup);
        ConfigurableSetting conf = categories.configurableSetting(category);
        return new Inventory(conf.type,conf.name,conf.icon,conf.rechargeable);
    }

    private Category category(Category.Filter filter){
        GameCluster _gameCluster = this.serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gameCluster.serviceWithCategory("item");
        ApplicationPreSetup preSetup = SystemUtil.applicationPreSetup((String) _gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
        Category category = new Category();
        category.distributionKey(app.distributionKey());
        preSetup.load(serviceContext,app,category);
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
        if(!applicationPreSetup.load(serviceContext,gameCluster,categories)){
            ConfigurableTemplate configuration = this.categoryTemplateSetting(gameCluster,type);
            JsonArray cclasses = (JsonArray)configuration.property("itemList");
            cclasses.forEach((c)->{
                categories.addType(c.getAsJsonObject());
            });
            applicationPreSetup.save(serviceContext,gameCluster,categories);
        }
        return categories;
    }
}
