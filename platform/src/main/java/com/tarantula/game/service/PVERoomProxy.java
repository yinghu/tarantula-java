package com.tarantula.game.service;

import com.icodesoftware.*;
import com.tarantula.game.*;
import com.tarantula.game.PlayerSavedGames;

public class PVERoomProxy extends RoomProxyHeader {

    @Override
    public void setup(ApplicationContext applicationContext, GameLobby gameLobby,GameZone gameZone) {
        super.setup(applicationContext,gameLobby,gameZone);
    }
    @Override
    public Stub join(Session session,Rating rating) {
        Stub stub = new Stub();
        stub.distributionKey(session.systemId());
        stub.stub(session.stub());
        stub.label(application.tag());
        this.dataStore.createIfAbsent(stub,true);
        stub.room = this.gameServiceProvider.roomServiceProvider().join(gameZone,rating);
        if(application.tournamentEnabled()&&session.tournamentId()!=null){
            Tournament.Instance instance = gameServiceProvider.tournamentServiceProvider().join(session.tournamentId(),session.systemId());
            stub.tournament = instance;
        }
        stub.pushChannel = context.register(session,(h,m)->super.update(stub,h,m),(s,d)->{
            gameLobby.timeout(s,d);
        });
        stub.roomId = stub.room.roomId();
        //stub.zone = gameZone;
        stub.joined = true;
        stub.offline = true;
        stub.tag = application.tag();
        stub.ticket = this.context.validator().ticket(session.systemId(),session.stub());
        stub.rating = rating;
        stub.inbox = this.gameServiceProvider.inboxServiceProvider().inbox(session.systemId());
        stub.shop = this.gameServiceProvider.storeServiceProvider().shop("Tami");
        stub.statistics = gameServiceProvider.statistics(session.systemId());
        stub.dailyLogin = gameServiceProvider.dailyLogin(session.systemId());
        PlayerSavedGames playerSavedGames = new PlayerSavedGames(session.systemId(),session.clientId(),this.gameServiceProvider.presenceServiceProvider().listSaves(session.systemId(),session.clientId(),session.name()));
        playerSavedGames.gameServiceProvider = gameServiceProvider;
        stub.playerSavedGames = playerSavedGames;
        return stub;
    }
    public void leave(Stub stub){
        stub.joined = false;
        this.dataStore.update(stub);
        this.gameServiceProvider.roomServiceProvider().leave(stub.roomId,stub.systemId());
        if(application.tournamentEnabled()&&stub.tournament!=null){
            gameServiceProvider.tournamentServiceProvider().leave(stub.tournament.distributionKey(),stub.systemId());
        }
    }
}
