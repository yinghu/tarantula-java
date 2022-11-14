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
        int zoneCapacityBase = ((Number)configuration.property("zoneCapacityBase")).intValue();
        int roomCapacity = ((Number)configuration.property(configName+"MaxRoomCapacity")).intValue();
        int joinsOnStart = ((Number)configuration.property("defaultJoinsOnStart")).intValue();
        long duration = ((Number)configuration.property("defaultRoundDuration")).longValue();
        int levelUpXpBase = ((Number)configuration.property("defaultLevelUpXpBase")).intValue();
        DataStore dataStore = serviceContext.dataStore(serviceDataStore(application),serviceContext.node().partitionNumber());
        int levelEnd = application.accessRank()*levelMatchOffset;
        int levelStart = levelEnd-(levelMatchOffset-1);
        for(int i=1;i<=initialZoneCount;i++){
            int levelMatch = (levelStart-1)+i*levelMatchFactor;
            GameZone zone = createGameZone(dataStore,"zone"+i,configName,levelMatch,arenaLimit,zoneCapacityBase/roomCapacity,roomCapacity,joinsOnStart,duration,levelUpXpBase);
            gameLobby.addKey(zone.distributionKey());
        }
        gameLobby.levelMatchOffset(levelMatchOffset);
        dataStore.createIfAbsent(gameLobby,true);
    }

    @Override
    public <T extends Configurable> T load(Descriptor application) {
        DataStore dataStore = serviceContext.dataStore(serviceDataStore(application),serviceContext.node().partitionNumber());
        DynamicGameLobby gameLobby = new DynamicGameLobby();
        gameLobby.distributionKey(application.distributionKey());
        if(!dataStore.load(gameLobby)) throw new RuntimeException("no lobby data for key->"+application.distributionKey());
        gameLobby.dataStore(dataStore);
        gameLobby.keySet().forEach((k)->{
            GameZone zone = loadGameZone(dataStore,k);
            gameLobby.addGameZone(zone);
        });
        return (T)gameLobby;
    }

    protected GameZone createGameZone(DataStore dataStore,String name,String configName,int levelMatch,int arenaLimit,int capacity,int roomCapacity,int joinsOnStart,long roundDuration,int levelUpXpBase){
        DynamicZone zone = new DynamicZone(name,configName,levelMatch,arenaLimit,capacity,roomCapacity,joinsOnStart,roundDuration);
        dataStore.create(zone);
        for(int i = 1; i< arenaLimit+1; i++){
            Arena arena = new Arena();
            arena.name("level"+i);
            arena.level = i;
            arena.xp = i* levelUpXpBase;
            arena.capacity = zone.maxJoinsPerRoom();
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
        if(!dataStore.load(zone)) throw new RuntimeException("no zone data for key->"+distributionKey);
        dataStore.list(new ArenaQuery(zone.distributionKey()),(a)->{
            zone.addArena(a);
            return true;
        });
        zone.dataStore(dataStore);
        zone.roomProxy(joinProxy(zone.playMode()));
        return zone;
    }
}
