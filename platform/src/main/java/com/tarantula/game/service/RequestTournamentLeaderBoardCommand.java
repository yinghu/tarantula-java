package com.tarantula.game.service;


import com.icodesoftware.Tournament;
import com.icodesoftware.protocol.MessageBuffer;

import com.tarantula.game.Stub;

public class RequestTournamentLeaderBoardCommand extends ServiceCommandHeader {


    public RequestTournamentLeaderBoardCommand(short serviceId,boolean exported){
        super(serviceId,exported);
    }

    @Override
    public byte[] onService(Stub stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        if(!application.tournamentEnabled() || stub.tournament==null) return null;
        Tournament.RaceBoard board = gameServiceProvider.tournamentServiceProvider().list(stub.tournament.distributionKey());
        return board.toJson().toString().getBytes();
    }
}
