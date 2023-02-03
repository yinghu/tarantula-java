package com.tarantula.game.service;

import com.google.gson.JsonObject;
import com.icodesoftware.Statistics;
import com.icodesoftware.protocol.MessageBuffer;
import com.tarantula.game.GameLobby;
import com.tarantula.game.Stub;

public class RequestStatisticsCommand extends ServiceCommandHeader implements GameLobby.ServiceMessageListener {


    //From http endpoint
    @Override
    public byte[] update(Stub stub, JsonObject payload){
        Statistics statistics = gameServiceProvider.statistics(stub.systemId());
        return statistics.toJson().toString().getBytes();
    }


    @Override
    public byte[] update(Stub stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        Statistics statistics = gameServiceProvider.statistics(stub.systemId());
        return statistics.toJson().toString().getBytes();
    }
}
