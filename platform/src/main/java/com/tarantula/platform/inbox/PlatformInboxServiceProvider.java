package com.tarantula.platform.inbox;

import com.icodesoftware.*;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.achievement.Achievement;
import com.tarantula.platform.inventory.PlatformInventoryServiceProvider;
import com.tarantula.platform.item.DistributionItemService;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.util.SystemUtil;

import java.util.concurrent.ConcurrentHashMap;

public class PlatformInboxServiceProvider implements ServiceProvider {

    private TarantulaLogger logger;
    private final String name;
    private final GameCluster gameCluster;
    private final PlatformInventoryServiceProvider inventoryServiceProvider;
    private ServiceContext serviceContext;
    private DistributionItemService distributionItemService;
    private DataStore dataStore;
    private ApplicationPreSetup applicationPreSetup;
    private ConcurrentHashMap<String,Configurable.Listener<Achievement>> rListeners = new ConcurrentHashMap<>();

    public PlatformInboxServiceProvider(GameCluster gameCluster, PlatformInventoryServiceProvider inventoryServiceProvider){
        this.name = (String)gameCluster.property(GameCluster.GAME_SERVICE);
        this.gameCluster = gameCluster;
        this.inventoryServiceProvider = inventoryServiceProvider;
    }
    @Override
    public String name() {
        return "InboxService";
    }

    @Override
    public void start() throws Exception {
        logger.warn("Inbox service provider started");
    }
    public Inbox inbox(String systemId){
        Inbox inbox = new Inbox();
        inbox.inventoryList = this.inventoryServiceProvider.inventoryList(systemId);
        return inbox;
    }
    @Override
    public void shutdown() throws Exception {

    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.applicationPreSetup = SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
        this.logger = serviceContext.logger(PlatformInboxServiceProvider.class);
        this.dataStore = serviceContext.dataStore(name.replace("-","_"),serviceContext.partitionNumber());
        this.distributionItemService = this.serviceContext.clusterProvider(Distributable.DATA_SCOPE).serviceProvider(DistributionItemService.NAME);
    }
}
