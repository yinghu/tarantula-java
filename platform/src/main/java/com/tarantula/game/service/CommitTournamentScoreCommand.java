package com.tarantula.game.service;

import com.icodesoftware.Session;
import com.icodesoftware.protocol.MessageBuffer;

public class CommitTournamentScoreCommand extends GameServiceProxyHeader {


    public CommitTournamentScoreCommand(short serviceId,GameServiceProvider gameServiceProvider){
        super(serviceId,gameServiceProvider);
    }

   @Override
    public byte[] onService(Session stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        if(!tournamentEnabled || stub.tournamentId()==null) return null;
        this.gameServiceProvider.tournamentServiceProvider().score(stub.tournamentId(),stub.trackId(),stub.systemId(),200,messageBuffer.readDouble());
        return null;
    }
}
