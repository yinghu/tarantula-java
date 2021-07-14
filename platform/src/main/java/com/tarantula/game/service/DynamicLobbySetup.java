package com.tarantula.game.service;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.DataStore;
import com.icodesoftware.Descriptor;
import com.icodesoftware.Recoverable;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.Arena;
import com.tarantula.game.DynamicZone;
import com.tarantula.game.GameZone;

import com.tarantula.platform.service.ApplicationPreSetup;

public class DynamicLobbySetup implements ApplicationPreSetup {

    @Override
    public void setup(ServiceContext serviceContext, Descriptor application, String configName) {
        DynamicZone zone = new DynamicZone(application.name(),configName);
        DataStore dataStore = serviceContext.dataStore(serviceDataStore(application),serviceContext.partitionNumber());
        zone.distributionKey(application.distributionKey());
        dataStore.create(zone);
        for(int i = 1; i< GameZone.DEFAULT_LEVEL_COUNT+1; i++){
            Arena arena = new Arena(zone.bucket(),zone.oid(),i);
            arena.name("level"+i);
            arena.level = i;
            arena.xp = i* GameZone.DEFAULT_LEVEL_UP_BASE;
            arena.disabled(false);
            dataStore.create(arena);
            zone.addArena(arena);
        }
    }

    @Override
    public <T extends Recoverable> T load(ApplicationContext context, Descriptor application) {
        DataStore dataStore = context.dataStore(serviceDataStore(application));
        return (T)load(dataStore,application);
    }

    @Override
    public <T extends Recoverable> T load(ServiceContext context, Descriptor application) {
        DataStore dataStore = context.dataStore(serviceDataStore(application),context.partitionNumber());
        return (T)load(dataStore,application);
    }

    private GameZone load(DataStore dataStore,Descriptor application){
        GameZone zone = new DynamicZone();
        zone.distributionKey(application.distributionKey());
        dataStore.load(zone);
        for(int i=1;i<application.capacity()+1;i++){
            Arena arena = new Arena(zone.bucket(),zone.oid(),i);
            if(dataStore.load(arena)){
                zone.addArena(arena);
            }
        }
        zone.dataStore(dataStore);
        zone.roomProxy(joinProxy(zone.playMode()));
        return zone;
    }

    private String serviceDataStore(Descriptor application){
        return application.typeId().replace("-lobby","_service");
    }
    private GameZone.RoomProxy joinProxy(String playMode){
        GameZone.RoomProxy roomProxy = new PVERoomProxy();
        if(playMode.equals(GameZone.PLAY_MODE_PVP)){
            roomProxy = new PVPRoomProxy();
        }
        else if(playMode.equals(GameZone.PLAY_MODE_TVE)){
            roomProxy = new TVERoomProxy();
        }
        else if(playMode.equals(GameZone.PLAY_MODE_TVT)){
            roomProxy = new TVTRoomProxy();
        }
        return roomProxy;
    }
}
