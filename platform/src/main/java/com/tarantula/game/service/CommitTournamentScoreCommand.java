package com.tarantula.game.service;

import com.icodesoftware.protocol.MessageBuffer;
import com.tarantula.game.Stub;

public class CommitTournamentScoreCommand extends ServiceCommandHeader{


   @Override
    public byte[] onService(Stub stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        if(!application.tournamentEnabled() || stub.tournament==null) return null;
        this.gameServiceProvider.tournamentServiceProvider().score(stub.tournament.distributionKey(),stub.systemId(),messageBuffer.readDouble());
        return null;
    }
}
