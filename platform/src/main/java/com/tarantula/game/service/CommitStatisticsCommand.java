package com.tarantula.game.service;

import com.icodesoftware.Statistics;
import com.icodesoftware.protocol.MessageBuffer;
import com.tarantula.game.Stub;

public class CommitStatisticsCommand extends ServiceCommandHeader {


    @Override
    public byte[] onService(Stub stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        String name = messageBuffer.readUTF8();
        double delta = messageBuffer.readFloat();
        Statistics statistics = gameServiceProvider.statistics(stub.systemId());
        statistics.entry(name).update(delta).update();
        return null;
    }
}
