package com.tarantula.platform.inventory;

import com.icodesoftware.Descriptor;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.Item;
import com.tarantula.platform.item.ItemConfigurationServiceProvider;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.util.SystemUtil;

import java.util.ArrayList;
import java.util.List;

public class InventoryServiceProvider implements ServiceProvider {
    private TarantulaLogger logger;

    private final String name;
    private GameCluster gameCluster;
    private ServiceContext serviceContext;
    private ApplicationPreSetup applicationPreSetup;
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
        this.applicationPreSetup = SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
        this.logger = serviceContext.logger(ItemConfigurationServiceProvider.class);
    }
    public Inventory inventory(String systemId,String category){
        Inventory inventory = new Inventory(category);
        inventory.distributionKey(systemId);
        GameCluster _gc = this.serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory("store");
        if(!this.applicationPreSetup.load(serviceContext,app,inventory)) return null;
        inventory.list();
        return inventory;
    }
    public boolean redeem(String systemId, Application item){
        InventoryRedeemer redeemer = new InventoryRedeemer(systemId);
        redeemer.distributionKey(item.distributionKey());
        GameCluster _gc = this.serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory(item.configurationCategory());
        if(!applicationPreSetup.load(serviceContext,app,redeemer)) return false;
        redeemer.redeem();
        return true;
    }
}
