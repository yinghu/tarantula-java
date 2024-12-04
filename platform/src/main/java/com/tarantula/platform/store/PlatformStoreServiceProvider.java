package com.tarantula.platform.store;

import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.service.ConfigurationServiceProvider;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.inventory.PlatformInventoryServiceProvider;
import com.tarantula.platform.item.DistributionItemService;
import com.tarantula.platform.item.ItemDistributionCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PlatformStoreServiceProvider implements ConfigurationServiceProvider, ItemDistributionCallback {

    public static final String NAME = "store";

    public static final String STORE_TRANSACTION_DATA_STORE = "store_transaction_log";
    private TarantulaLogger logger;
    private final String gameServiceName;
    private final GameCluster gameCluster;
    private final PlatformInventoryServiceProvider inventoryServiceProvider;
    private ServiceContext serviceContext;
    private DistributionItemService distributionItemService;
    private ApplicationPreSetup applicationPreSetup;

    private ConcurrentHashMap<String,Shop> shopIndex;
    private ConcurrentHashMap<String,ShoppingItem> shoppingItems;

    private DataStore transactionLogStore;

    public PlatformStoreServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        this.gameCluster = gameServiceProvider.gameCluster();
        this.gameServiceName = gameCluster.gameServiceName;//(String)gameCluster.property(GameCluster.GAME_SERVICE);
        this.inventoryServiceProvider = gameServiceProvider.inventoryServiceProvider();
    }
    @Override
    public String name() {
        return NAME;
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
        this.shopIndex = new ConcurrentHashMap<>();
        this.serviceContext = serviceContext;
        this.applicationPreSetup = gameCluster.applicationPreSetup();//SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
        this.transactionLogStore = applicationPreSetup.dataStore(gameCluster,STORE_TRANSACTION_DATA_STORE);
        this.logger = JDKLogger.getLogger(PlatformStoreServiceProvider.class);
        this.distributionItemService = this.serviceContext.clusterProvider().serviceProvider(DistributionItemService.NAME);
    }

    public Shop shop(String name){
        return shopIndex.containsKey(name)?shopIndex.get(name):new Shop(name);
    }
    public List<ShoppingItem> list(){
        ArrayList<ShoppingItem> _items = new ArrayList<>();
        shoppingItems.forEach((k,v)->_items.add(v));
        return _items;
    }
    public ShoppingItem shoppingItem(String itemId){
        return shoppingItems.get(itemId);
    }


    @Override
    public <T extends Configurable> void register(T t) {
        if(!t.configurationCategory().equals("Shop")) return;
        t.registered();
        distributionItemService.onRegisterItem(gameServiceName,name(),t.configurationTypeId(),t.distributionKey());
    }
    @Override
    public <T extends Configurable> void release(T t){
        t.released();
        this.distributionItemService.onReleaseItem(gameServiceName,name(),t.configurationTypeId(),t.distributionKey());
    }
    public boolean onItemRegistered(String category,String itemId){
        Shop configurableObject = new Shop();
        configurableObject.distributionKey(itemId);
        GameCluster _gc = serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionId());
        Descriptor app = _gc.serviceWithCategory(category);
        if(!applicationPreSetup.load(app,configurableObject)) return false;
        registerShop(configurableObject);
        return true;
    }
    public boolean onItemReleased(String category,String itemId){
        Shop shop = shopIndex.remove(itemId);
        if(shop==null) return false;
        shopIndex.remove(shop.name());
        shop.list().forEach(item->shoppingItems.remove(item.distributionKey()));
        return true;
    }
    @Override
    public String registerConfigurableListener(Descriptor descriptor, Configurable.Listener listener) {
        List<Shop> items = applicationPreSetup.list(descriptor,new ShoppingItemObjectQuery(descriptor.key(),"Shop"));
        items.forEach((a)-> {
            if (!a.disabled()) {
                registerShop(a);
            }
        });
        return null;
    }

    private void registerShop(Shop shop){
        shop.configurableSetting(gameCluster.configurableCategories(Configurable.APPLICATION_CONFIG_TYPE));
        Shop s = shop.setup();
        shopIndex.put(shop.configurationName(),s);
        shopIndex.put(shop.distributionKey(),s);
        shop.list().forEach(item->{
            item.setup();
            shoppingItems.put(item.distributionKey(),new ShoppingItem(item));
        });
    }

    //store IAP tracking methods
    public List<StoreTransactionLog> transactionLogList(Session session){
        return transactionLogStore.list(new StoreTransactionQuery(session.distributionId()));
    }

    public StoreTransactionLog transactionLog(String transactionId){
        StoreTransactionLog transactionLog = new StoreTransactionLog(transactionId);
        if(transactionLogStore.load(transactionLog)) return transactionLog;
        return null;
    }
    public void transactionLog(StoreTransactionLog transactionLog){
        transactionLogStore.createIfAbsent(transactionLog,false);
    }

}
