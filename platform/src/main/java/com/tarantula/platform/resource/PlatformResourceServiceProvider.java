package com.tarantula.platform.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.Configuration;
import com.icodesoftware.Descriptor;

import com.icodesoftware.logging.JDKLogger;

import com.icodesoftware.service.ServiceContext;

import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.inventory.PlatformInventoryServiceProvider;
import com.tarantula.platform.item.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PlatformResourceServiceProvider extends PlatformItemServiceProvider{

    public static final String NAME = "resource";

    private final PlatformInventoryServiceProvider inventoryServiceProvider;

    private ConcurrentHashMap<String, GameResource> gameResourceIndex;
    private ConcurrentHashMap<String,Item> itemIndex;

    private ArrayList<String> startingInventory;

    private String startingResourceName;
    private String grantingPolicy;



    public PlatformResourceServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
        this.inventoryServiceProvider = gameServiceProvider.inventoryServiceProvider();
        this.gameResourceIndex = new ConcurrentHashMap<>();
        this.itemIndex = new ConcurrentHashMap<>();
        this.startingInventory = new ArrayList<>();
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        super.setup(serviceContext);
        Configuration configuration = serviceContext.configuration("game-presence-settings");
        JsonObject startingInventory = ((JsonElement)configuration.property("startingInventory")).getAsJsonObject();
        startingResourceName = startingInventory.get("resourceName").getAsString();
        grantingPolicy = startingInventory.get("grantPolicy").getAsString();
        this.logger = JDKLogger.getLogger(PlatformResourceServiceProvider.class);
        this.logger.warn("Resource service provider started on ->"+gameServiceName);
    }

    public boolean onItemRegistered(String category,String itemId){
        ConfigurableObject configurationObject = new ConfigurableObject();
        configurationObject.distributionKey(itemId);
        Descriptor app = gameCluster.serviceWithCategory(category);
        if(!applicationPreSetup.load(app,configurationObject)){
            return false;
        }
        if(configurationObject.configurationCategory().equals("Achievement")){
            this.platformGameServiceProvider.achievementServiceProvider().onItemRegistered(category,itemId);
            return true;
        }
        if(configurationObject.configurationCategory().equals("DailyGiveaway")){
            this.platformGameServiceProvider.dailyGiveawayServiceProvider().onItemRegistered(category,itemId);
            return true;
        }
        if(configurationObject.configurationCategory().equals("Shop")){
            this.platformGameServiceProvider.storeServiceProvider().onItemRegistered(category,itemId);
            return true;
        }
        GameResource gameResource = new GameResource(configurationObject);
        gameResource.configurableSetting(gameCluster.configurableCategories(Configurable.APPLICATION_CONFIG_TYPE));
        gameResource.setup();
        setup(gameResource);
        gameResourceIndex.put(gameResource.name(),gameResource);
        this.platformGameServiceProvider.gameServiceProvider().onApplicationResourceRegistered(gameResource);
        return true;
    }
    public boolean onItemReleased(String category,String itemId){
        this.platformGameServiceProvider.dailyGiveawayServiceProvider().onItemReleased(category,itemId);
        this.platformGameServiceProvider.achievementServiceProvider().onItemReleased(category,itemId);
        this.platformGameServiceProvider.storeServiceProvider().onItemReleased(category,itemId);
        String[] released = {null};
        gameResourceIndex.forEach((k,v)->{
            if(v.distributionKey().equals(itemId)) released[0] = k;
        });
        if(released[0]!=null) {
            GameResource removed = gameResourceIndex.remove(released[0]);
            this.platformGameServiceProvider.gameServiceProvider().onApplicationResourceReleased(removed);
            clear(removed);
        }
        return true;
    }

    @Override
    public String registerConfigurableListener(Descriptor descriptor, Configurable.Listener listener) {
        List<GameResource> items = applicationPreSetup.list(descriptor,new GameResourceQuery(descriptor.key(),"Resource"));
        items.forEach((a)-> {
            a.configurableSetting(gameCluster.configurableCategories(Configurable.APPLICATION_CONFIG_TYPE));
            a.setup();
            if(!a.disabled()) {
                setup(a);
                gameResourceIndex.put(a.name(),a);
                this.platformGameServiceProvider.gameServiceProvider().onApplicationResourceRegistered(a);
            }
        });
        this.application = descriptor;
        this.platformGameServiceProvider.achievementServiceProvider().registerConfigurableListener(descriptor,listener);
        this.platformGameServiceProvider.dailyGiveawayServiceProvider().registerConfigurableListener(descriptor,listener);
        this.platformGameServiceProvider.storeServiceProvider().registerConfigurableListener(descriptor,listener);
        return null;
    }

    public GameResource list(String name){
        return gameResourceIndex.get(name);
    }

    public List<GameResource> list(){
        ArrayList<GameResource> gameResources = new ArrayList<>();
        gameResourceIndex.forEach((k,v)->gameResources.add(v));
        return gameResources;
    }

    public Commodity item(String itemId){
        Commodity configurable = new Commodity();
        configurable.distributionKey(itemId);
        if(!applicationPreSetup.load(application,configurable)){
            logger.warn("Item not existed->"+itemId);
            return configurable;
        }
        configurable.setup();
        return configurable;
    }

    public boolean grant(long systemId,String itemId){
        Item item = itemIndex.get(itemId);
        if(item==null){
            logger.warn("Item not existed->"+itemId);
            return false;
        }
        return this.inventoryServiceProvider.redeem(systemId,item);
    }


    private void setup(GameResource gameResource){
        List<Item> items = gameResource.list();
        items.forEach(c->{
            if(gameResource.configurationName().equals(startingResourceName)){
                synchronized (startingInventory){
                    startingInventory.add(c.distributionKey());
                }
            }
            itemIndex.put(c.distributionKey(),c);
        });
    }

    private void clear(GameResource gameResource){
        List<Item> items = gameResource.list();
        items.forEach(c->{
            if(gameResource.configurationName().equals(startingResourceName)){
                synchronized (startingInventory){
                    startingInventory.clear();
                }
            }
            itemIndex.remove(c.distributionKey());
            logger.warn("Item removed->"+gameResource.configurationName());
        });
    }


}
