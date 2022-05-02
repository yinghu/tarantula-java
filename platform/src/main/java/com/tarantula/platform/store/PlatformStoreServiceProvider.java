package com.tarantula.platform.store;

import com.icodesoftware.*;
import com.icodesoftware.service.ConfigurationServiceProvider;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.inventory.PlatformInventoryServiceProvider;
import com.tarantula.platform.item.DistributionItemService;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.service.ClusterConfigurationCallback;
import com.tarantula.platform.util.SystemUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PlatformStoreServiceProvider implements ConfigurationServiceProvider, ClusterConfigurationCallback {

    private TarantulaLogger logger;
    private final String gameServiceName;
    private final GameCluster gameCluster;
    private final PlatformInventoryServiceProvider inventoryServiceProvider;
    private ServiceContext serviceContext;
    private DistributionItemService distributionItemService;
    private ApplicationPreSetup applicationPreSetup;

    private ConcurrentHashMap<String,ShoppingItem> shoppingItems;

    public PlatformStoreServiceProvider(GameCluster gameCluster, PlatformInventoryServiceProvider inventoryServiceProvider){
        this.gameServiceName = (String)gameCluster.property(GameCluster.GAME_SERVICE);
        this.gameCluster = gameCluster;
        this.inventoryServiceProvider = inventoryServiceProvider;
    }
    @Override
    public String name() {
        return "store";
    }

    @Override
    public void start() throws Exception {
        this.logger.warn("Store service provider started on->"+gameServiceName);
    }

    @Override
    public void shutdown() throws Exception {

    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.shoppingItems = new ConcurrentHashMap<>();
        this.serviceContext = serviceContext;
        this.applicationPreSetup = SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
        this.logger = serviceContext.logger(PlatformStoreServiceProvider.class);
        this.distributionItemService = this.serviceContext.clusterProvider(Distributable.DATA_SCOPE).serviceProvider(DistributionItemService.NAME);
    }

    public List<ShoppingItem> list(){
        ArrayList<ShoppingItem> _items = new ArrayList<>();
        shoppingItems.forEach((k,v)->_items.add(v));
        return _items;
    }
    public ShoppingItem shoppingItem(String itemId){
        return shoppingItems.get(itemId);
    }
    public boolean grant(String systemId,String itemId){
        ShoppingItem shoppingItem = shoppingItems.get(itemId);
        if(shoppingItem==null){
            return false;
        }
        return this.inventoryServiceProvider.redeem(systemId,shoppingItem);
    }
    @Override
    public <T extends Configurable> void register(T t) {
        t.registered();
        distributionItemService.register(gameServiceName,name(),t.configurationTypeId(),t.distributionKey());
    }
    @Override
    public <T extends Configurable> void release(T t){
        t.released();
        this.distributionItemService.release(gameServiceName,name(),t.configurationTypeId(),t.distributionKey());
    }
    public boolean onRegister(String category,String itemId){
        ShoppingItem configurableObject = new ShoppingItem();
        configurableObject.distributionKey(itemId);
        GameCluster _gc = serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory(category);
        if(!applicationPreSetup.load(serviceContext,app,configurableObject)) return false;
        shoppingItems.put(configurableObject.distributionKey(),configurableObject);
        return true;
    }
    public boolean onRelease(String category,String itemId){
        shoppingItems.remove(itemId);
        return true;
    }
    @Override
    public String registerConfigurableListener(Descriptor descriptor, Configurable.Listener listener) {
        List<ShoppingItem> items = applicationPreSetup.list(serviceContext,descriptor,new ShoppingItemObjectQuery("typeId/"+descriptor.category()));
        items.forEach((a)-> {
            if (!a.disabled()) {
                a._setup();
                shoppingItems.put(a.distributionKey(), a);
            }
        });
        return null;
    }
}
