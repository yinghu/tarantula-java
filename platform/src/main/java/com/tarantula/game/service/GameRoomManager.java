package com.tarantula.game.service;

import com.icodesoftware.DataStore;
import com.tarantula.game.Arena;
import com.tarantula.game.GameRoom;
import com.tarantula.game.GameZone;

public class GameRoomManager {
    private DataStore dataStore;
    private GameZone gameZone;
    public GameRoomManager(DataStore dataStore,GameZone gameZone){
        this.dataStore = dataStore;
        this.gameZone = gameZone;
    }
    public GameRoom join(Arena arena,String roomId, String systemId){
        Arena  localArena = gameZone.arena(arena.level);
        GameRoom gameRoom = new GameRoom(true);
        gameRoom.distributionKey(roomId);
        dataStore.createIfAbsent(gameRoom,true);
        return gameRoom;
    }
}
