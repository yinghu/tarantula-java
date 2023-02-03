package com.tarantula.game.service;

import com.icodesoftware.Statistics;
import com.icodesoftware.protocol.MessageBuffer;
import com.tarantula.game.Stub;

public class CommitAchievementCommand extends ServiceCommandHeader {


    public CommitAchievementCommand(short serviceId,boolean exported){
        super(serviceId,exported);
    }

    @Override
    public byte[] onService(Stub stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        String name = messageBuffer.readUTF8();
        double delta = messageBuffer.readInt();
        Statistics statistics = gameServiceProvider.statistics(stub.systemId());
        statistics.entry(name).update(delta).update();
        return null;
    }
}
