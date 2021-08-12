package com.tarantula.game.service;

import com.icodesoftware.*;

import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.*;

public class DynamicGameLobbySetup extends GameObjectSetup {
    private static String CONFIG = "game-lobby-settings";
    @Override
    public void setup(ServiceContext serviceContext, Descriptor application, String configName) {
        DynamicGameLobby gameLobby = new DynamicGameLobby();
        gameLobby.distributionKey(application.distributionKey());
        Configuration configuration = serviceContext.configuration(CONFIG);
        int initialZoneCount = ((Number)configuration.property("initialZoneCount")).intValue();
        int levelMatchOffset = ((Number)configuration.property("levelMatchOffset")).intValue();
        int zoneLimit = ((Number)configuration.property("zoneLimit")).intValue();
        int arenaLimit = ((Number)configuration.property("arenaLimit")).intValue();
        int levelMatchFactor = levelMatchOffset/zoneLimit;
        int roomCapacity = ((Number)configuration.property(configName+"MaxRoomCapacity")).intValue();
        int joinsOnStart = ((Number)configuration.property("defaultJoinsOnStart")).intValue();
        long duration = ((Number)configuration.property("defaultRoundDuration")).longValue();
        int levelUpBase = ((Number)configuration.property("defaultLevelUpBase")).intValue();
        DataStore dataStore = serviceContext.dataStore(serviceDataStore(application),serviceContext.partitionNumber());
        int levelEnd = application.accessRank()*levelMatchOffset;
        int levelStart = levelEnd-(levelMatchOffset-1);
        for(int i=1;i<=initialZoneCount;i++){
            int levelMatch = (levelStart-1)+i*levelMatchFactor;
            GameZone zone = createGameZone(dataStore,"zone"+i,configName,levelMatch,arenaLimit,roomCapacity,joinsOnStart,duration,levelUpBase);
            gameLobby.keySet.add(zone.distributionKey());
        }
        gameLobby.levelMatchOffset(levelMatchOffset);
        dataStore.create(gameLobby);
    }

    @Override
    public <T extends Configurable> T load(ApplicationContext context, Descriptor application) {
        DataStore dataStore = context.dataStore(serviceDataStore(application));
        DynamicGameLobby gameLobby = new DynamicGameLobby();
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
        DynamicGameLobby gameLobby = new DynamicGameLobby();
        gameLobby.distributionKey(application.distributionKey());
        dataStore.load(gameLobby);
        gameLobby.keySet.forEach((k)->{
            GameZone zone = loadGameZone(dataStore,k);
            gameLobby.addGameZone(zone);
        });
        return (T)gameLobby;
    }
    protected GameZone createGameZone(DataStore dataStore,String name,String configName,int levelMatch,int arenaLimit,int roomCapacity,int joinsOnStart,long roundDuration,int levelUpBase){
        DynamicZone zone = new DynamicZone(name,configName,levelMatch,arenaLimit,roomCapacity,joinsOnStart,roundDuration);
        dataStore.create(zone);
        for(int i = 1; i< arenaLimit+1; i++){
            Arena arena = new Arena();
            arena.name("level"+i);
            arena.level = i;
            arena.xp = i* levelUpBase;
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
