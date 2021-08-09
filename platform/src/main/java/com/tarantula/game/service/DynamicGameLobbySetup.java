package com.tarantula.game.service;

import com.icodesoftware.*;

import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.Arena;
import com.tarantula.game.DynamicZone;
import com.tarantula.game.GameLobby;
import com.tarantula.game.GameZone;

public class DynamicGameLobbySetup extends GameObjectSetup {
    private static String CONFIG = "game-lobby-settings";
    @Override
    public void setup(ServiceContext serviceContext, Descriptor application, String configName) {
        GameLobby gameLobby = new GameLobby();
        gameLobby.distributionKey(application.distributionKey());
        Configuration configuration = serviceContext.configuration(CONFIG);
        int initialZoneCount = ((Number)configuration.property("initialZoneCount")).intValue();
        DataStore dataStore = serviceContext.dataStore(serviceDataStore(application),serviceContext.partitionNumber());
        for(int i=0;i<initialZoneCount;i++){
            GameZone zone = createGameZone(dataStore,application,configName,i+1);
            gameLobby.keySet.add(zone.distributionKey());
        }
        dataStore.create(gameLobby);
    }

    @Override
    public <T extends Configurable> T load(ApplicationContext context, Descriptor application) {
        DataStore dataStore = context.dataStore(serviceDataStore(application));
        GameLobby gameLobby = new GameLobby();
        gameLobby.distributionKey(application.distributionKey());
        dataStore.load(gameLobby);
        gameLobby.keySet.forEach((k)->{
            GameZone zone = loadGameZone(dataStore,k);
            gameLobby.addGameZone(zone);
        });
        return (T)gameLobby;
    }

    @Override
    public <T extends Configurable> T load(ServiceContext context, Descriptor application) {
        DataStore dataStore = context.dataStore(serviceDataStore(application),context.partitionNumber());
        GameLobby gameLobby = new GameLobby();
        gameLobby.distributionKey(application.distributionKey());
        dataStore.load(gameLobby);
        gameLobby.keySet.forEach((k)->{
            GameZone zone = loadGameZone(dataStore,k);
            gameLobby.addGameZone(zone);
        });
        return (T)gameLobby;
    }
    protected GameZone createGameZone(DataStore dataStore,Descriptor application,String configName,int levelMatch){
        DynamicZone zone = new DynamicZone(application.name(),configName,levelMatch);
        dataStore.create(zone);
        for(int i = 1; i< GameZone.DEFAULT_LEVEL_COUNT+1; i++){
            Arena arena = new Arena();
            arena.name("level"+i);
            arena.level = i;
            arena.xp = i* GameZone.DEFAULT_LEVEL_UP_BASE;
            arena.capacity = zone.capacity();
            arena.duration = zone.roundDuration();
            arena.joinsOnStart = zone.joinsOnStart();
            arena.disabled(false);
            arena.owner(zone.distributionKey());
            dataStore.create(arena);
            zone.addArena(arena);
        }
        return zone;
    }
    public GameZone loadGameZone(DataStore dataStore,String distributionKey){
        GameZone zone = new DynamicZone();
        zone.distributionKey(distributionKey);
        dataStore.load(zone);
        dataStore.list(new ArenaQuery(zone.distributionKey()),(a)->{
            zone.addArena(a);
            return true;
        });
        zone.dataStore(dataStore);
        zone.roomProxy(joinProxy(zone.playMode()));
        return zone;
    }
}
