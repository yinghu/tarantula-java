package com.tarantula.game.service;

import com.icodesoftware.DataStore;
import com.icodesoftware.Descriptor;
import com.icodesoftware.TarantulaLogger;

import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.platform.GameCluster;


public class PlatformGameServiceSetup implements ServiceProvider {

    private final String SERVICE_NAME;

    protected TarantulaLogger logger;

    protected ServiceContext serviceContext;

    protected PlatformGameServiceProvider platformGameServiceProvider;
    protected final String gameServiceName;
    protected GameCluster gameCluster;
    protected ApplicationPreSetup applicationPreSetup;
    protected DataStore dataStore;
    protected Descriptor application;

    public PlatformGameServiceSetup(PlatformGameServiceProvider gameServiceProvider, String name){
        this.platformGameServiceProvider = gameServiceProvider;
        this.gameCluster = gameServiceProvider.gameCluster();
        this.gameServiceName = gameCluster.serviceType();
        this.SERVICE_NAME = name;
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.applicationPreSetup = gameCluster.applicationPreSetup();
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


}
