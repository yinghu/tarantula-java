package com.tarantula.game.service;

import com.icodesoftware.Session;
import com.icodesoftware.Statistics;
import com.icodesoftware.protocol.MessageBuffer;

public class RequestStatisticsCommand extends GameServiceProxyHeader {


    public RequestStatisticsCommand(short serviceId, PlatformGameServiceProvider gameServiceProvider){
        super(serviceId,gameServiceProvider);
    }
    //From http endpoint
    @Override
    public byte[] onService(Session session, byte[] payload){
        Statistics statistics = gameServiceProvider.presenceServiceProvider().statistics(session);
        return statistics.toJson().toString().getBytes();
    }


    @Override
    public byte[] onService(Session stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        //messageHeader.encrypted = true;
        Statistics statistics = gameServiceProvider.presenceServiceProvider().statistics(stub);
        return statistics.toJson().toString().getBytes();
    }
}
