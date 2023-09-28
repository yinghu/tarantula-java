package com.tarantula.platform.inventory;

import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.*;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.store.ShoppingItem;
import com.tarantula.platform.tournament.TournamentPrize;

import java.util.ArrayList;
import java.util.List;


public class PlatformInventoryServiceProvider implements ServiceProvider,InventoryListener {

    public static final String NAME = "inventory";

    private TarantulaLogger logger;

    private final String gameServiceName;
    private GameCluster gameCluster;
    private ServiceContext serviceContext;
    private ApplicationPreSetup applicationPreSetup;
    private DataStore inventoryDataStore;

    public PlatformInventoryServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        this.gameCluster = gameServiceProvider.gameCluster();
        this.gameServiceName = gameCluster.serviceType();
    }

    @Override
    public String name() {
        return NAME;
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
        this.applicationPreSetup = gameCluster.applicationPreSetup();
        this.inventoryDataStore = this.applicationPreSetup.dataStore(gameCluster,NAME);
        this.logger = JDKLogger.getLogger(PlatformInventoryServiceProvider.class);
    }
    public DataStore inventoryDataStore(){
        return this.inventoryDataStore;
    }
    public Category category(){
        return category((ci)->ci.configurationType().equals(Configurable.COMMODITY_CONFIG_TYPE));
    }
    public List<Inventory> inventoryList(String systemId){
        InventoryQuery query = new InventoryQuery(Long.parseLong(systemId));
        List<Inventory> inventoryList = new ArrayList<>();
        inventoryDataStore.list(query).forEach(t->{
            t.dataStore(inventoryDataStore);
            t.list();
            inventoryList.add(t);
        });
        return inventoryList;
    }

    public Inventory inventory(String systemId,String category,String typeId){
        int cindex = category.indexOf(".");
        String type = cindex<0?category:category.substring(0,cindex);
        InventoryQuery query = new InventoryQuery(Long.parseLong(systemId));
        Inventory[] inventories={null};
        inventoryDataStore.list(query,t->{
            if(t.type.equals(type)&&t.typeId.equals(typeId)){
                inventories[0]=t;
                return false;
            }
            return true;
        });
        if(inventories[0]==null) return new Inventory(type,typeId);
        inventories[0].dataStore(inventoryDataStore);
        inventories[0].list();
        return inventories[0];
    }
    public boolean redeem(String systemId, Application item){
        ApplicationRedeemer redeemer = new ApplicationRedeemer(systemId,this);
        redeemer.distributionKey(item.distributionKey());
        Descriptor app = gameCluster.serviceWithCategory(item.configurationTypeId());
        if(app==null||!applicationPreSetup.load(app,redeemer)) return false;
        Descriptor itemApp = gameCluster.serviceWithCategory("item");
        redeemer.dataStore(applicationPreSetup.dataStore(itemApp));
        redeemer.redeem();
        return true;
    }
    public boolean redeem(String systemId, Item item){
        ItemRedeemer redeemer = new ItemRedeemer(systemId,this);
        redeemer.distributionKey(item.distributionKey());
        Descriptor app = gameCluster.serviceWithCategory(item.configurationType());
        if(app==null||!applicationPreSetup.load(app,redeemer)) return false;
        Descriptor itemApp = gameCluster.serviceWithCategory("item");
        redeemer.dataStore(applicationPreSetup.dataStore(itemApp));
        redeemer.redeem();
        return true;
    }

    public boolean redeem(String systemId, ShoppingItem item){
        ApplicationRedeemer redeemer = new ApplicationRedeemer(systemId,this);
        redeemer.distributionKey(item.distributionKey());
        Descriptor app = gameCluster.serviceWithCategory(item.configurationTypeId());
        if(app==null||!applicationPreSetup.load(app,redeemer)) return false;
        Descriptor itemApp = gameCluster.serviceWithCategory("item");
        redeemer.dataStore(applicationPreSetup.dataStore(itemApp));
        redeemer.redeem();
        return true;
    }
    public boolean redeem(String systemId, TournamentPrize item){
        ApplicationRedeemer redeemer = new ApplicationRedeemer(systemId,this);
        redeemer.distributionKey(item.distributionKey());
        Descriptor app = gameCluster.serviceWithCategory(item.configurationTypeId());
        if(app==null||!applicationPreSetup.load(app,redeemer)) return false;
        Descriptor itemApp = gameCluster.serviceWithCategory("item");
        redeemer.dataStore(applicationPreSetup.dataStore(itemApp));
        redeemer.redeem();
        return true;
    }

    public Inventory newInventory(String category,String typeId){
        ConfigurableCategories categories = this.gameCluster.configurableCategories(Configurable.COMMODITY_CONFIG_TYPE);
        ConfigurableCategory conf = categories.configurableSetting(category);
        conf.parse();
        return new Inventory(conf.name(),typeId,conf.rechargeable);
    }

    private Category category(Category.Filter filter){
        Descriptor app = gameCluster.serviceWithCategory("inventory");
        ApplicationPreSetup preSetup = gameCluster.applicationPreSetup();
        Category category = new Category(app);
        category.dataStore(preSetup.dataStore(app));
        category.list((ci)-> filter.onFilter(ci));
        return category;
    }


    @Override
    public void onInventory(Inventory inventory,InventoryItem item) {
        //callback on adding inventory
    }
}
