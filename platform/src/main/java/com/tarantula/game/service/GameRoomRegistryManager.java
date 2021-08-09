package com.tarantula.game.service;

import com.icodesoftware.DataStore;
import com.tarantula.game.GameRoomRegistry;
import com.tarantula.game.GameZone;

public class GameRoomRegistryManager {

    private DataStore dataStore;
    private GameZone gameZone;
    public GameRoomRegistryManager(DataStore dataStore,GameZone gameZone){
        this.dataStore = dataStore;
        this.gameZone = gameZone;
    }
    public GameRoomRegistry register(String systemId){
        GameRoomRegistry gameRoomRegistry = new GameRoomRegistry();
        this.dataStore.create(gameRoomRegistry);
        gameRoomRegistry.addPlayer(systemId);
        this.dataStore.update(gameRoomRegistry);
        return gameRoomRegistry;
    }

}
