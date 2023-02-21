package com.tarantula.game.service;

import com.icodesoftware.Session;
import com.icodesoftware.protocol.MessageBuffer;

public class CommitTournamentScoreCommand extends GameServiceProxyHeader {


    public CommitTournamentScoreCommand(short serviceId,boolean exported,GameServiceProvider gameServiceProvider){
        super(serviceId,exported,gameServiceProvider);
    }

   @Override
    public byte[] onService(Session stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        if(!application.tournamentEnabled() || stub.tournamentId()==null) return null;
        this.gameServiceProvider.tournamentServiceProvider().score(stub.tournamentId(),stub.trackId(),stub.systemId(),200,messageBuffer.readDouble());
        return null;
    }
}
