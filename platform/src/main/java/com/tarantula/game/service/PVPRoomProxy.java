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
        //GameRoom room = gameServiceProvider.roomServiceProvider().join(zoneId,rating);
        String roomId = gameServiceProvider.distributionRoomService().register(gameServiceProvider.name(),gameZone.distributionKey(),rating);
        GameRoom _rm = gameServiceProvider.distributionRoomService().join(gameServiceProvider.name(),gameZone.arena(rating.arenaLevel),roomId,session.systemId());
        this.context.log("RoomId->"+roomId,OnLog.WARN);
        gameServiceProvider.distributionRoomService().leave(gameServiceProvider.name(),gameZone.distributionKey(),roomId,session.systemId());

        return new Stub();
    }
    public void leave(Stub stub){
        //this.gameServiceProvider.roomServiceProvider().leave(stub.arena,stub.room.roomId(),stub.owner());
    }
    @Override
    public void onTimer(Module.OnUpdate onUpdate) {
        //this.context.log("calling on ->"+registerKey,OnLog.WARN);
    }
    public String onRegister(Rating rating){
        return rating.systemId();
    }
    public GameRoom onJoin(Arena arena,String roomId,String systemId){
        GameRoom gameRoom = new GameRoom(true);
        gameRoom.distributionKey(roomId);
        return gameRoom;
    }
    public void onLeave(String roomId,String systemId){
        this.context.log(systemId+" leave distributed room", OnLog.WARN);
    }

}
