package com.tarantula.platform.inbox;

import com.icodesoftware.*;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.inventory.PlatformInventoryServiceProvider;

public class PlatformInboxServiceProvider implements ServiceProvider {

    public static final String NAME = "inbox";

    private TarantulaLogger logger;
    private final String gameServiceName;
    private final GameCluster gameCluster;
    private final PlatformInventoryServiceProvider inventoryServiceProvider;
    private ServiceContext serviceContext;

    public PlatformInboxServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        this.gameCluster = gameServiceProvider.gameCluster();
        this.gameServiceName = gameCluster.serviceType();
        this.inventoryServiceProvider = gameServiceProvider.inventoryServiceProvider();
    }
    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void start() throws Exception {
        logger.warn("Inbox service provider started on ->"+gameServiceName);
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
        this.logger = this.serviceContext.logger(PlatformInboxServiceProvider.class);
    }
}
