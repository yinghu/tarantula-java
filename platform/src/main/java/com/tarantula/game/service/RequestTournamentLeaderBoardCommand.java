package com.tarantula.game.service;


import com.icodesoftware.Session;
import com.icodesoftware.Tournament;
import com.icodesoftware.protocol.MessageBuffer;


public class RequestTournamentLeaderBoardCommand extends GameServiceProxyHeader {


    public RequestTournamentLeaderBoardCommand(short serviceId, PlatformGameServiceProvider gameServiceProvider){
        super(serviceId,gameServiceProvider);
    }

    @Override
    public byte[] onService(Session stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        //if(!tournamentEnabled || stub.tournamentId()==null) return null;
        //Tournament.RaceBoard board = gameServiceProvider.tournamentServiceProvider().list(stub.tournamentId(),stub.trackId());
        //return board.toJson().toString().getBytes();
        return null;
    }
}
