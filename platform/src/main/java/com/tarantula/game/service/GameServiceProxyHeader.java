package com.tarantula.game.service;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Descriptor;
import com.icodesoftware.Session;
import com.icodesoftware.protocol.GameServiceProxy;
import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.util.JsonUtil;


public class GameServiceProxyHeader implements GameServiceProxy {

    protected ApplicationContext applicationContext;
    protected GameServiceProvider gameServiceProvider;
    protected Descriptor application;
    private final boolean exported;
    private final short serviceId;

    public GameServiceProxyHeader(short serviceId, boolean exported){
        this.serviceId = serviceId;
        this.exported = exported;
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

    public short serviceId(){
        return serviceId;
    }

    public boolean exported(){
        return exported;
    }


    @Override
    public byte[] onService(Session stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        return JsonUtil.toSimpleResponse(false,"service without implementation").getBytes();
    }

    @Override
    public byte[] onService(Session session, byte[] payload){
        return JsonUtil.toSimpleResponse(false,"service without implementation").getBytes();
    }
}
