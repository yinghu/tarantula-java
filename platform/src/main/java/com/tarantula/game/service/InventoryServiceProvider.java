package com.tarantula.game.service;

import com.icodesoftware.DataStore;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.Item;
import com.tarantula.platform.item.ItemConfigurationServiceProvider;

public class InventoryServiceProvider implements ServiceProvider {
    private TarantulaLogger logger;

    private final String name;
    private GameCluster gameCluster;
    private ServiceContext serviceContext;
    private DataStore dataStore;
    public InventoryServiceProvider(GameCluster gameCluster){
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
        //this.applicationPreSetup = SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
        this.dataStore = serviceContext.dataStore(name.replace("-","_"),serviceContext.partitionNumber());
        this.logger = serviceContext.logger(ItemConfigurationServiceProvider.class);
        //this.distributionItemService = this.serviceContext.clusterProvider(Distributable.DATA_SCOPE).serviceProvider(DistributionItemService.NAME);
    }
    public void redeem(String systemId, Item item){
        logger.warn(item.toJson().toString());

    }
}
