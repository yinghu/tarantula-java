package com.tarantula.game.service;

import com.google.gson.JsonObject;
import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Descriptor;
import com.icodesoftware.Session;
import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.GameLobby;
import com.tarantula.game.Stub;

public class ServiceCommandHeader implements GameLobby.ServiceProxy {

    protected ApplicationContext applicationContext;
    protected GameServiceProvider gameServiceProvider;
    protected Descriptor application;

    public ServiceCommandHeader(){

    }

    public void setup(ApplicationContext applicationContext) throws Exception{
        this.applicationContext = applicationContext;
        this.application = this.applicationContext.descriptor();
        this.gameServiceProvider = applicationContext.serviceProvider(application.typeId().replace("lobby","service"));
    }

    public Descriptor descriptor(){
        return this.application;
    }

    public void descriptor(Descriptor descriptor){
        this.application = descriptor;
    }

    public byte[] onService(Stub stub, JsonObject payload){
        return JsonUtil.toSimpleResponse(false,"service not ready").getBytes();
    }

    @Override
    public byte[] onService(Stub stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        return new byte[0];
    }

    public byte[] onService(Session session, JsonObject payload){
        return JsonUtil.toSimpleResponse(false,"service not ready").getBytes();
    }
}
