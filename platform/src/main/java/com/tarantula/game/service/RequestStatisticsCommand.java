package com.tarantula.game.service;

import com.icodesoftware.Session;
import com.icodesoftware.Statistics;
import com.icodesoftware.protocol.MessageBuffer;

public class RequestStatisticsCommand extends GameServiceProxyHeader {


    public RequestStatisticsCommand(short serviceId,boolean exported){
        super(serviceId,exported);
    }
    //From http endpoint
    @Override
    public byte[] onService(Session session, byte[] payload){
        Statistics statistics = gameServiceProvider.statistics(session.systemId());
        return statistics.toJson().toString().getBytes();
    }


    @Override
    public byte[] onService(Session stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        Statistics statistics = gameServiceProvider.statistics(stub.systemId());
        return statistics.toJson().toString().getBytes();
    }
}
