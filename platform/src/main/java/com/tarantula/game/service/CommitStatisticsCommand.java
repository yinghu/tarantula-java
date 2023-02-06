package com.tarantula.game.service;

import com.icodesoftware.Session;
import com.icodesoftware.Statistics;
import com.icodesoftware.protocol.MessageBuffer;


public class CommitStatisticsCommand extends GameServiceProxyHeader {


    public CommitStatisticsCommand(short serviceId,boolean exported){
        super(serviceId,exported);
    }


    @Override
    public byte[] onService(Session stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        String name = messageBuffer.readUTF8();
        double delta = messageBuffer.readFloat();
        Statistics statistics = gameServiceProvider.statistics(stub.systemId());
        statistics.entry(name).update(delta).update();
        return null;
    }
}
