package com.tarantula.game.service;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.tarantula.game.*;

public class PVERoomProxy extends RoomProxyHeader implements GameLobby.TimerLister {

    private String registerKey;
    @Override
    public void setup(ApplicationContext applicationContext, GameLobby gameLobby,GameZone gameZone) {
        super.setup(applicationContext,gameLobby,gameZone);
        this.gameServiceProvider.registerRoomProxy(gameZone.distributionKey(),this);
        this.registerKey = this.gameLobby.registerTimerListener(this);
    }
    @Override
    public GameRoom join(Session session,Rating rating) {
        String roomId = gameServiceProvider.distributionRoomService().register(gameServiceProvider.name(),gameZone.distributionKey(),rating);
        GameRoom _rm = gameServiceProvider.distributionRoomService().join(gameServiceProvider.name(),gameZone.arena(rating.arenaLevel),roomId,session.systemId());
        this.context.log("RoomId->"+roomId,OnLog.WARN);
        gameServiceProvider.distributionRoomService().leave(gameServiceProvider.name(),gameZone.distributionKey(),roomId,session.systemId());
        GameRoom room = new GameRoom(true);
        if(application.tournamentEnabled()){
            Tournament.Instance instance = gameServiceProvider.tournamentServiceProvider().join(session.tournamentId(),session.systemId());
            room.instance = instance;
        }
        room.arena = gameZone.arena(rating.arenaLevel);
        room.totalJoined = 1;
        room.distributionKey(_rm.roomId());
        return room;
    }
    public void leave(Stub stub){
        this.context.log(stub.systemId()+" leave room", OnLog.WARN);
        if(application.tournamentEnabled()){
            //gameServiceProvider.tournamentServiceProvider().leave();
        }
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
