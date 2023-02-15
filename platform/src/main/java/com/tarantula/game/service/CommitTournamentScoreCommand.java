package com.tarantula.game.service;

import com.icodesoftware.Session;
import com.icodesoftware.protocol.MessageBuffer;

public class CommitTournamentScoreCommand extends GameServiceProxyHeader {


    public CommitTournamentScoreCommand(short serviceId,boolean exported){
        super(serviceId,exported);
    }

   @Override
    public byte[] onService(Session stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        if(!application.tournamentEnabled() || stub.tournamentId()==null) return null;
        this.gameServiceProvider.tournamentServiceProvider().score("",stub.tournamentId(),stub.systemId(),messageBuffer.readDouble());
        return null;
    }
}
