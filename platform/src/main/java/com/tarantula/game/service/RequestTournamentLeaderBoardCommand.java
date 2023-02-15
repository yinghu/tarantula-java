package com.tarantula.game.service;


import com.icodesoftware.Session;
import com.icodesoftware.Tournament;
import com.icodesoftware.protocol.MessageBuffer;


public class RequestTournamentLeaderBoardCommand extends GameServiceProxyHeader {


    public RequestTournamentLeaderBoardCommand(short serviceId,boolean exported){
        super(serviceId,exported);
    }

    @Override
    public byte[] onService(Session stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        if(!application.tournamentEnabled() || stub.tournamentId()==null) return null;
        Tournament.RaceBoard board = gameServiceProvider.tournamentServiceProvider().list(stub.tournamentId(),stub.trackId());
        return board.toJson().toString().getBytes();
    }
}
