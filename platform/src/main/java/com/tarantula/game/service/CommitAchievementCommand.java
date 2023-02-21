package com.tarantula.game.service;

import com.icodesoftware.Session;
import com.icodesoftware.Statistics;
import com.icodesoftware.protocol.MessageBuffer;

public class CommitAchievementCommand extends GameServiceProxyHeader {


    public CommitAchievementCommand(short serviceId,boolean exported,GameServiceProvider gameServiceProvider){
        super(serviceId,exported,gameServiceProvider);
    }

    @Override
    public byte[] onService(Session stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        String name = messageBuffer.readUTF8();
        double delta = messageBuffer.readInt();
        Statistics statistics = gameServiceProvider.statistics(stub.systemId());
        statistics.entry(name).update(delta).update();
        return null;
    }
}
