package com.tarantula.game.service;

import com.icodesoftware.Statistics;
import com.icodesoftware.protocol.MessageBuffer;
import com.tarantula.game.GameLobby;
import com.tarantula.game.Stub;

public class CommitStatisticsCommand extends ServiceCommandHeader implements GameLobby.ServiceMessageListener {


    @Override
    public byte[] update(Stub stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        String name = messageBuffer.readUTF8();
        double delta = messageBuffer.readFloat();
        Statistics statistics = gameServiceProvider.statistics(stub.systemId());
        statistics.entry(name).update(delta).update();
        return null;
    }
}
