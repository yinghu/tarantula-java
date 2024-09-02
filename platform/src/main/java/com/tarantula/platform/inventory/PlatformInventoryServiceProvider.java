package com.tarantula.platform.inventory;

import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.SnowflakeKey;
import com.tarantula.game.service.PlatformGameServiceProvider;

import com.tarantula.platform.item.*;
import com.tarantula.platform.store.ShoppingItem;
import com.tarantula.platform.tournament.TournamentPrize;

import java.util.List;


public class PlatformInventoryServiceProvider extends PlatformItemServiceProvider implements Inventory.Listener {

    public static final String NAME = "inventory";


    private DataStore inventoryDataStore;

    public PlatformInventoryServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
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
        super.setup(serviceContext);
        this.inventoryDataStore = this.applicationPreSetup.dataStore(gameCluster,NAME);
        this.logger = JDKLogger.getLogger(PlatformInventoryServiceProvider.class);
    }

    public Category category(){
        return category((ci)->ci.configurationType().equals(Configurable.COMMODITY_CONFIG_TYPE));
    }
    public List<Inventory> inventoryList(long systemId){
        return this.applicationPreSetup.inventoryList(systemId);
    }

    public Inventory inventory(Session session){
        return applicationPreSetup.inventory(session.distributionId(),session.name());
    }
    public UserInventory inventory(long systemId, String category, String typeId){
        int cindex = category.indexOf(".");
        String type = cindex<0?category:category.substring(0,cindex);
        InventoryQuery query = new InventoryQuery(systemId);
        UserInventory[] inventories={null};
        inventoryDataStore.list(query,t->{
            if(t.type.equals(type)&&t.typeId.equals(typeId)){
                inventories[0]=t;
                return false;
            }
            return true;
        });
        if(inventories[0]==null) return new UserInventory(type,typeId);
        inventories[0].dataStore(inventoryDataStore);
        inventories[0].list();
        return inventories[0];
    }
    public boolean redeem(long systemId, Application item){
        boolean[] suc ={false};
        try(final Transaction t = gameCluster.transaction()){
            suc[0] = t.execute(ctx->{
                ApplicationPreSetup setup = (ApplicationPreSetup)ctx;
                Descriptor app = gameCluster.application(item.configurationTypeId());
                ApplicationRedeemer redeemer = new ApplicationRedeemer(systemId,setup);
                redeemer.distributionKey(item.distributionKey());
                if(!setup.load(app,redeemer)) return false;
                redeemer.redeem();
                return true;
            });
        }
        return suc[0];
    }
    public boolean redeem(long systemId, Item item){
        boolean[] suc ={false};
        try(final Transaction t = gameCluster.transaction()){
            suc[0] = t.execute(ctx->{
                ApplicationPreSetup setup = (ApplicationPreSetup)ctx;
                Descriptor app = gameCluster.application(item.configurationType());
                ApplicationRedeemer redeemer = new ApplicationRedeemer(systemId,setup);
                redeemer.distributionKey(item.distributionKey());
                if(!setup.load(app,redeemer)) return false;
                redeemer.redeem();
                return true;
            });
        }
        return suc[0];
    }

    public boolean redeem(long systemId, TournamentPrize item){
        boolean[] suc ={false};
        try(Transaction t = gameCluster.transaction()){
            suc[0] = t.execute(ctx->{
                ApplicationPreSetup setup =(ApplicationPreSetup)ctx;
                Descriptor app = gameCluster.application(item.configurationTypeId());
                ApplicationRedeemer redeemer = new ApplicationRedeemer(systemId,setup);
                redeemer.distributionKey(item.distributionKey());
                if(!setup.load(app,redeemer)) return false;
                redeemer.redeem();
                return true;
            });
        }
        return suc[0];
    }
    public boolean redeem(long systemId, ShoppingItem shoppingItem){
        shoppingItem.commodityList().forEach(commodity -> {
            logger.warn(commodity.application().toString());
            logger.warn(commodity.application().get("template").getAsJsonObject().get("application").toString());
            String type = commodity.configurationCategory();
            String typeId = commodity.configurationTypeId();
            UserInventory inventory = (UserInventory)applicationPreSetup.inventory(systemId,typeId);
            if(inventory!=null){
                logger.warn("inventory :"+inventory.typeId());
                inventory.redeem(shoppingItem.distributionId(),commodity);
            }
            else{
                inventory = (UserInventory) gameCluster.createInventory(applicationPreSetup,type,typeId);
                logger.warn("in :"+inventory.typeId());
                inventory.ownerKey(SnowflakeKey.from(systemId));
                inventoryDataStore.create(inventory);
                //inventoryDataStore.create(inventory);
                inventoryDataStore.createEdge(inventory,typeId);
                inventoryDataStore.createEdge(inventory,type);
                inventory.dataStore(inventoryDataStore);
                inventory.applicationPreSetup(applicationPreSetup);
                inventory.redeem(shoppingItem.distributionId(),commodity);
            }
        });
        return true;
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
    public void onInventory(ApplicationPreSetup applicationPreSetup,Inventory inventory, Inventory.Stock item) {
        logger.warn("inventory added=>"+inventory.configurationTypeId());
        this.platformGameServiceProvider.gameServiceProvider().onInventory(applicationPreSetup,inventory,item);
    }
}
