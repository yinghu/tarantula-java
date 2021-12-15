package com.tarantula.game.service;

import com.icodesoftware.Statistics;
import com.icodesoftware.protocol.MessageBuffer;
import com.tarantula.game.GameLobby;
import com.tarantula.game.Stub;
import com.tarantula.platform.statistics.StatisticsSerializer;

public class RequestStatisticsCommand extends ServiceCommandHeader implements GameLobby.ServiceMessageListener {


    @Override
    public short command() {
        return ServiceCommand.REQUEST_STATISTICS;
    }

    @Override
    public byte[] update(Stub stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        Statistics statistics = gameServiceProvider.statistics(stub.systemId());
        StatisticsSerializer serializer = new StatisticsSerializer();
        return serializer.serialize(statistics,Statistics.class,null).toString().getBytes();
    }
}
