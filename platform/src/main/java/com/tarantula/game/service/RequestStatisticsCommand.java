package com.tarantula.game.service;

import com.google.gson.JsonObject;
import com.icodesoftware.Statistics;
import com.icodesoftware.protocol.MessageBuffer;
import com.tarantula.game.Stub;

public class RequestStatisticsCommand extends ServiceCommandHeader{


    //From http endpoint
    @Override
    public byte[] onService(Stub stub, JsonObject payload){
        Statistics statistics = gameServiceProvider.statistics(stub.systemId());
        return statistics.toJson().toString().getBytes();
    }


    @Override
    public byte[] onService(Stub stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        Statistics statistics = gameServiceProvider.statistics(stub.systemId());
        return statistics.toJson().toString().getBytes();
    }
}
