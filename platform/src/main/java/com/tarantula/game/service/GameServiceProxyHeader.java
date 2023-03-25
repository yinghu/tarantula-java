package com.tarantula.game.service;

import com.icodesoftware.Session;
import com.icodesoftware.protocol.GameServiceProxy;
import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.util.JsonUtil;


public class GameServiceProxyHeader implements GameServiceProxy {

    protected final GameServiceProvider gameServiceProvider;
    protected final boolean tournamentEnabled;

    private final short serviceId;

    public GameServiceProxyHeader(short serviceId,GameServiceProvider gameServiceProvider){
        this.serviceId = serviceId;
        this.gameServiceProvider = gameServiceProvider;
        this.tournamentEnabled = this.gameServiceProvider!=null? this.gameServiceProvider.gameCluster().tournamentEnabled() : false;
    }

    public short serviceId(){
        return serviceId;
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
