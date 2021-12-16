package com.tarantula.game.service;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Descriptor;

public class ServiceCommandHeader {

    protected ApplicationContext applicationContext;
    protected GameServiceProvider gameServiceProvider;
    protected Descriptor application;

    public ServiceCommandHeader(){

    }

    public void setup(ApplicationContext applicationContext){
        this.applicationContext = applicationContext;
        this.application = this.applicationContext.descriptor();
        this.gameServiceProvider = applicationContext.serviceProvider(application.typeId().replace("lobby","service"));
    }
}
