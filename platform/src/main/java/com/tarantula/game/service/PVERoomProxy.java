package com.tarantula.game.service;

import com.icodesoftware.*;
import com.tarantula.game.*;
import com.tarantula.platform.presence.saves.PlayerSavedGames;
import com.tarantula.platform.store.Shop;

public class PVERoomProxy extends RoomProxyHeader {

    @Override
    public void setup(ApplicationContext applicationContext, GameLobby gameLobby,GameZone gameZone) {
        super.setup(applicationContext,gameLobby,gameZone);
    }
    @Override
    public Stub join(Session session,Rating rating) {
        Stub stub = new Stub();
        stub.distributionKey(session.systemId());
        stub.label(application.tag());
        this.dataStore.createIfAbsent(stub,true);
        stub.room = this.gameServiceProvider.roomServiceProvider().join(gameZone,rating);
        if(application.tournamentEnabled()&&session.tournamentId()!=null){
            Tournament.Instance instance = gameServiceProvider.tournamentServiceProvider().join(session.tournamentId(),session.systemId());
            stub.tournament = instance;
        }
        stub.pushChannel = context.register(session.systemId(),(h,m)->super.update(stub,h,m),(s)->{
            gameLobby.timeout(s);
        });
        stub.roomId = stub.room.roomId();
        stub.zone = gameZone;
        stub.joined = true;
        stub.offline = true;
        stub.tag = application.tag();
        stub.ticket = this.context.validator().ticket(session.systemId(),session.stub());
        stub.rating = rating;
        stub.inbox = this.gameServiceProvider.inboxServiceProvider().inbox(session.systemId());
        stub.shop = new Shop(this.gameServiceProvider.storeServiceProvider().list());
        stub.statistics = gameServiceProvider.statistics(session.systemId());
        stub.dailyLogin = gameServiceProvider.dailyLogin(session.systemId());
        PlayerSavedGames playerSavedGames = new PlayerSavedGames(session.systemId(),session.clientId(),this.gameServiceProvider.presenceServiceProvider().listSaves(session.systemId(),session.clientId(),session.name()));
        playerSavedGames.presenceServiceProvider = gameServiceProvider.presenceServiceProvider();
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
