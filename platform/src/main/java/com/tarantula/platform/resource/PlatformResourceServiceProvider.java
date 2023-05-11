package com.tarantula.platform.resource;

import com.icodesoftware.Configurable;
import com.icodesoftware.DataStore;
import com.icodesoftware.Descriptor;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.ConfigurationServiceProvider;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.inventory.PlatformInventoryServiceProvider;
import com.tarantula.platform.item.DistributionItemService;
import com.tarantula.platform.item.ItemDistributionCallback;
import com.tarantula.platform.service.ApplicationPreSetup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PlatformResourceServiceProvider implements ConfigurationServiceProvider, ItemDistributionCallback {

    public static final String NAME = "resource";

    private TarantulaLogger logger;
    private final String gameServiceName;
    private final GameCluster gameCluster;
    private final PlatformInventoryServiceProvider inventoryServiceProvider;
    private ServiceContext serviceContext;
    private DistributionItemService distributionItemService;
    //private DataStore dataStore;
    private ApplicationPreSetup applicationPreSetup;
    private ConcurrentHashMap<String, GameResource> gameResourceIndex;

    public PlatformResourceServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        this.gameCluster = gameServiceProvider.gameCluster();
        this.gameServiceName = gameCluster.serviceType();
        this.inventoryServiceProvider = gameServiceProvider.inventoryServiceProvider();
        this.gameResourceIndex = new ConcurrentHashMap<>();
    }
    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.applicationPreSetup = gameCluster.applicationPreSetup();
        this.logger = serviceContext.logger(PlatformResourceServiceProvider.class);
        //this.dataStore = serviceContext.dataStore(gameServiceName.replace("-","_"),serviceContext.node().partitionNumber());
        this.distributionItemService = this.serviceContext.clusterProvider().serviceProvider(DistributionItemService.NAME);
        this.logger.warn("Resource service provider started on ->"+gameServiceName);
    }


    @Override
    public <T extends Configurable> void register(T t) {
        t.registered();
        distributionItemService.onRegisterItem(gameServiceName,name(),t.configurationTypeId(),t.distributionKey());
    }
    @Override
    public <T extends Configurable> void release(T t) {
        t.released();
        distributionItemService.onReleaseItem(gameServiceName,name(),t.configurationTypeId(),t.distributionKey());
    }
    public boolean onItemRegistered(String category,String itemId){
        GameResource configurableObject = new GameResource();
        configurableObject.distributionKey(itemId);
        GameCluster _gc = serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory(category);
        if(!applicationPreSetup.load(app,configurableObject)){
            return false;
        }
        configurableObject.setup();
        gameResourceIndex.put(configurableObject.name(),configurableObject);
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
            if(!a.disabled()) gameResourceIndex.put(a.name(),a);
        });
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

}
