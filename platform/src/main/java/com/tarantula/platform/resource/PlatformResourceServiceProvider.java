package com.tarantula.platform.resource;

import com.icodesoftware.Configurable;
import com.icodesoftware.Descriptor;

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

    public PlatformResourceServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
        this.inventoryServiceProvider = gameServiceProvider.inventoryServiceProvider();
        this.gameResourceIndex = new ConcurrentHashMap<>();
        this.itemIndex = new ConcurrentHashMap<>();
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        super.setup(serviceContext);
        gameCluster.addListener(this);
        this.logger = serviceContext.logger(PlatformResourceServiceProvider.class);
        this.logger.warn("Resource service provider started on ->"+gameServiceName);
    }

    public boolean onItemRegistered(String category,String itemId){
        GameResource gameResource = new GameResource();
        gameResource.distributionKey(itemId);
        Descriptor app = gameCluster.serviceWithCategory(category);
        if(!applicationPreSetup.load(app,gameResource)){
            return false;
        }
        gameResource.setup();
        setup(gameResource);
        gameResourceIndex.put(gameResource.name(),gameResource);
        return true;
    }
    public boolean onItemReleased(String category,String itemId){
        String[] released = {null};
        gameResourceIndex.forEach((k,v)->{
            if(v.distributionKey().equals(itemId)) released[0] = k;
        });
        if(released[0]!=null) gameResourceIndex.remove(released[0]);
        return true;
    }

    @Override
    public String registerConfigurableListener(Descriptor descriptor, Configurable.Listener listener) {
        List<GameResource> items = applicationPreSetup.list(descriptor,new GameResourceQuery("typeId/"+descriptor.category()));
        items.forEach((a)-> {
            a.setup();
            if(!a.disabled()) {
                setup(a);
                gameResourceIndex.put(a.name(),a);
            }
        });
        this.application = descriptor;
        this.gameCluster.addListener(this);
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

    public boolean grant(String systemId,String itemId){
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
            itemIndex.put(c.distributionKey(),c);
        });
    }


}
