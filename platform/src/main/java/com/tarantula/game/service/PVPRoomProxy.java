package com.tarantula.game.service;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Module;
import com.icodesoftware.OnLog;
import com.icodesoftware.Session;
import com.tarantula.game.*;

public class PVPRoomProxy extends RoomProxyHeader{


    @Override
    public void setup(ApplicationContext applicationContext, GameLobby gameLobby, GameZone gameZone) {
        super.setup(applicationContext,gameLobby,gameZone);
        this.gameServiceProvider.registerRoomProxy(gameZone.distributionKey(),this);
    }
    @Override
    public Stub join(Session session, Rating rating) {
        Stub stub = new Stub();
        stub.distributionKey(session.systemId());
        stub.label(application.tag());
        this.dataStore.createIfAbsent(stub,true);
        String roomId = gameServiceProvider.distributionRoomService().register(gameServiceProvider.name(),gameZone.distributionKey(),rating);
        GameRoom _rm = gameServiceProvider.distributionRoomService().join(gameServiceProvider.name(),gameZone.arena(rating.arenaLevel),roomId,session.systemId());
        stub.room = _rm;
        stub.joined = _rm!=null;
        stub.zone = gameZone;
        stub.rating = rating;
        stub.tag = application.tag();
        stub.serverKey = serverKey;
        return stub;
    }
    public void leave(Stub stub){
        //this.gameServiceProvider.distributionRoomService().leave(stub.room.roomId(),stub.owner());
    }
    @Override
    public void onTimer(Module.OnUpdate onUpdate) {
        //this.context.log("calling on ->"+registerKey,OnLog.WARN);
    }
    public String onRegister(Rating rating){ return rating.systemId();}
    public GameRoom onJoin(Arena arena,String roomId,String systemId){
        GameRoom gameRoom = new GameRoom(true);
        gameRoom.setup(arena,null);
        return gameRoom;
    }
    public void onLeave(String roomId,String systemId){
        this.context.log(systemId+" leave distributed room", OnLog.WARN);
    }

}
