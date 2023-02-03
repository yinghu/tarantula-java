package com.tarantula.game.service;

import com.google.gson.JsonObject;
import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Descriptor;
import com.tarantula.game.Stub;

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

    public byte[] update(Stub stub, JsonObject payload){
        return null;//JsonUtil.toSimpleResponse(false,"wrong command").getBytes();
    }
}
