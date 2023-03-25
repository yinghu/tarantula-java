package com.tarantula.game.service;

import com.icodesoftware.Session;
import com.icodesoftware.Statistics;
import com.icodesoftware.protocol.MessageBuffer;

public class RequestStatisticsCommand extends GameServiceProxyHeader {


    public RequestStatisticsCommand(short serviceId,GameServiceProvider gameServiceProvider){
        super(serviceId,gameServiceProvider);
    }
    //From http endpoint
    @Override
    public byte[] onService(Session session, byte[] payload){
        Statistics statistics = gameServiceProvider.presenceServiceProvider().statistics(session.systemId());
        return statistics.toJson().toString().getBytes();
    }


    @Override
    public byte[] onService(Session stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        //messageHeader.encrypted = true;
        Statistics statistics = gameServiceProvider.presenceServiceProvider().statistics(stub.systemId());
        return statistics.toJson().toString().getBytes();
    }
}
