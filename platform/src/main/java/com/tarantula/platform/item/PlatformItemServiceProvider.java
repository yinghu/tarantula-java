package com.tarantula.platform.item;

import com.icodesoftware.*;
import com.icodesoftware.service.ConfigurationServiceProvider;
import com.icodesoftware.service.ServiceContext;

import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.service.ApplicationPreSetup;


public class PlatformItemServiceProvider implements ConfigurationServiceProvider, ItemDistributionCallback,ApplicationPreSetup.Listener {

    private final String SERVICE_NAME;

    protected TarantulaLogger logger;

    protected ServiceContext serviceContext;

    protected DistributionItemService distributionItemService;
    protected PlatformGameServiceProvider platformGameServiceProvider;
    protected final String gameServiceName;
    protected GameCluster gameCluster;
    protected ApplicationPreSetup applicationPreSetup;
    protected DataStore dataStore;
    protected Descriptor application;

    public PlatformItemServiceProvider(PlatformGameServiceProvider gameServiceProvider,String name){
        this.platformGameServiceProvider = gameServiceProvider;
        this.gameCluster = gameServiceProvider.gameCluster();
        this.gameServiceName = gameCluster.serviceType();
        this.SERVICE_NAME = name;
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.applicationPreSetup = gameCluster.applicationPreSetup();
        //this.dataStore = applicationPreSetup.dataStore(gameCluster,SERVICE_NAME);
        this.distributionItemService = this.serviceContext.clusterProvider().serviceProvider(DistributionItemService.NAME);
    }
    @Override
    public String name() {
        return SERVICE_NAME;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

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
       return false;
    }
    public boolean onItemReleased(String category,String itemId){
        return false;
    }

}
