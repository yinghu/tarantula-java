package com.tarantula.platform.store;

import com.icodesoftware.*;
import com.icodesoftware.service.ConfigurationServiceProvider;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.inventory.InventoryServiceProvider;
import com.tarantula.platform.item.DistributionItemService;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.service.ClusterConfigurationCallback;
import com.tarantula.platform.util.SystemUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class StoreServiceProvider implements ConfigurationServiceProvider, ClusterConfigurationCallback {

    private TarantulaLogger logger;
    private final String name;
    private final GameCluster gameCluster;
    private final InventoryServiceProvider inventoryServiceProvider;
    private ServiceContext serviceContext;
    private DistributionItemService distributionItemService;
    private ApplicationPreSetup applicationPreSetup;

    private ConcurrentHashMap<String,ShoppingItem> shoppingItems;

    public StoreServiceProvider(GameCluster gameCluster, InventoryServiceProvider inventoryServiceProvider){
        this.name = (String)gameCluster.property(GameCluster.GAME_SERVICE);
        this.gameCluster = gameCluster;
        this.inventoryServiceProvider = inventoryServiceProvider;
    }
    @Override
    public String name() {
        return "StoreService";
    }

    @Override
    public void start() throws Exception {
        this.logger.warn("Store service provider started");
    }

    @Override
    public void shutdown() throws Exception {

    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.shoppingItems = new ConcurrentHashMap<>();
        this.serviceContext = serviceContext;
        this.applicationPreSetup = SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
        this.logger = serviceContext.logger(StoreServiceProvider.class);
        this.distributionItemService = this.serviceContext.clusterProvider(Distributable.DATA_SCOPE).serviceProvider(DistributionItemService.NAME);
    }

    public List<ShoppingItem> list(){
        ArrayList<ShoppingItem> _items = new ArrayList<>();
        shoppingItems.forEach((k,v)->_items.add(v));
        return _items;
    }
    public boolean buy(String systemId,String itemId){
        ShoppingItem shoppingItem = shoppingItems.get(itemId);
        if(shoppingItem==null){
            return false;
        }
        return this.inventoryServiceProvider.redeem(systemId,shoppingItem);
    }
    @Override
    public <T extends Configurable> void register(T t) {
        t.registered();
        distributionItemService.register(name,name(),t.configurationCategory(),t.distributionKey());
    }
    @Override
    public <T extends Configurable> void release(T t){
        t.released();
        this.distributionItemService.release(name,name(),t.configurationCategory(),t.distributionKey());
    }
    public boolean onRegister(String category,String itemId){
        ShoppingItem configurableObject = new ShoppingItem();
        configurableObject.distributionKey(itemId);
        GameCluster _gc = serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory(category);
        if(!applicationPreSetup.load(serviceContext,app,configurableObject)){
            return false;
        }
        shoppingItems.put(configurableObject.distributionKey(),configurableObject);
        return true;
    }
    public boolean onRelease(String category,String itemId){
        shoppingItems.remove(itemId);
        return true;
    }
    @Override
    public String registerConfigurableListener(Descriptor descriptor, Configurable.Listener listener) {
        List<ShoppingItem> items = applicationPreSetup.list(serviceContext,descriptor,new ShoppingItemObjectQuery("category/"+descriptor.category()));
        items.forEach((a)-> {
            if (!a.disabled()) shoppingItems.put(a.distributionKey(), a);
        });
        return null;
    }
}
