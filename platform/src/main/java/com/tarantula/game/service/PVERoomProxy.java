package com.tarantula.game.service;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Descriptor;
import com.tarantula.game.*;

public class PVERoomProxy implements GameZone.RoomProxy {

    private GameServiceProvider gameServiceProvider;
    private Descriptor application;
    @Override
    public Stub join(Arena arena, Rating rating) {
        Stub stub = new Stub();
        stub.successful(true);
        stub.rating = rating;
        stub.arena = arena;
        stub.offline = true;
        stub.owner(rating.distributionKey());
        Room _remote = gameServiceProvider.roomServiceProvider().join(arena,rating);
        gameServiceProvider.roomServiceProvider().leave(arena,_remote.roomId,rating.distributionKey());
        return stub;
    }
    public void leave(String systemId){

    }
    public void setup(ApplicationContext applicationContext){
        this.application = applicationContext.descriptor();
        this.gameServiceProvider = applicationContext.serviceProvider(application.typeId().replace("lobby","service"));
    }
}
