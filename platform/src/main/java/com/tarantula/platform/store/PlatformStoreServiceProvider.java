package com.tarantula.platform.store;

import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.inventory.PlatformInventoryServiceProvider;
import com.tarantula.platform.item.Commodity;
import com.tarantula.platform.item.PlatformItemServiceProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PlatformStoreServiceProvider extends PlatformItemServiceProvider {

    public static final String NAME = "store";


    private final PlatformInventoryServiceProvider inventoryServiceProvider;

    private ConcurrentHashMap<String,Shop> shopIndex;
    private ConcurrentHashMap<String,ShoppingItem> shoppingItems;

    public PlatformStoreServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
        this.inventoryServiceProvider = gameServiceProvider.inventoryServiceProvider();
    }
    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void start() throws Exception {
        if(!serviceContext.node().homingAgent().enabled()) return;
        String resp = serviceContext.node().homingAgent().onConfiguration(gameCluster.distributionId(),"Shop");
        JsonObject configs = JsonUtil.parse(resp);
        configs.get("list").getAsJsonArray().forEach(e-> registerShop(new Shop(e.getAsJsonObject())));
        this.logger.warn("Store service provider started with homing agent enabled");
    }


    @Override
    public void setup(ServiceContext serviceContext) {
        super.setup(serviceContext);
        this.shoppingItems = new ConcurrentHashMap<>();
        this.shopIndex = new ConcurrentHashMap<>();
        this.logger = JDKLogger.getLogger(PlatformStoreServiceProvider.class);
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
        super.register(t);
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
        if(serviceContext.node().homingAgent().enabled()) return null;
        List<Shop> items = applicationPreSetup.list(descriptor,new ShoppingItemObjectQuery(descriptor.key(),"Shop"));
        items.forEach((a)-> {
            if (!a.disabled()) {
                registerShop(a);
            }
        });
        return null;
    }

    private void registerShop(Shop shop){
        if(shop.itemList()!=null){
            shop.itemList().forEach(shoppingItem -> {
                List<Commodity> commodities = shoppingItem.commodityList();
                commodities.forEach(commodity -> {
                    gameCluster.registerConfigurableCategory(commodity.configurableCategory());
                });
                shoppingItems.put(shoppingItem.distributionKey(),shoppingItem);
            });
            shopIndex.put(shop.configurationName(),shop);
            return;
        }
        shop.configurableSetting(gameCluster.configurableCategories(Configurable.APPLICATION_CONFIG_TYPE));
        Shop s = shop.setup();
        shopIndex.put(shop.configurationName(),s);
        shopIndex.put(shop.distributionKey(),s);
        shop.list().forEach(item->{
            item.setup();
            shoppingItems.put(item.distributionKey(),new ShoppingItem(item));
        });
    }



    public boolean onItemRegistered(int publishId){
        logger.warn("register shop with publish id : "+publishId);
        String config = serviceContext.node().homingAgent().onConfigurationRegistered(publishId);
        registerShop(new Shop(JsonUtil.parse(config)));
        return true;
    }
    public boolean onItemReleased(int publishId){
        logger.warn("release local shop with ["+publishId+"]");
        Shop removed = shopIndex.remove(Integer.toString(publishId));
        if(removed==null) return false;
        //remove items
        removed.itemList().forEach(item->{
            shoppingItems.remove(item.distributionKey());
        });
        return true;
    }

}
