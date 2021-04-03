package com.tarantula.game.service;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.DataStore;
import com.icodesoftware.Descriptor;
import com.icodesoftware.Recoverable;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.Arena;
import com.tarantula.game.PVEZone;
import com.tarantula.game.Zone;
import com.tarantula.platform.service.ApplicationPreSetup;

public class PVELobbySetup implements ApplicationPreSetup {

    @Override
    public void setup(ServiceContext serviceContext, Descriptor application) {
        //create zone/arena for lobby
        DataStore dataStore = serviceContext.dataStore(serviceDataStore(application),serviceContext.partitionNumber());
        PVEZone pveZone = new PVEZone();
        pveZone.name = application.name();
        pveZone.levelLimit = application.capacity();
        pveZone.distributionKey(application.distributionKey());
        dataStore.create(pveZone);
        for(int i=1;i<Zone.DEFAULT_LEVEL_COUNT+1;i++){
            Arena arena = new Arena(pveZone.bucket(),pveZone.oid(),i);
            arena.name("level"+i);
            arena.level = i;
            arena.xp = i* Zone.DEFAULT_LEVEL_UP_BASE;
            arena.disabled(false);
            dataStore.create(arena);
            pveZone.arenas.add(arena);
        }
    }
    @Override
    public <T extends Recoverable> T load(ApplicationContext context, Descriptor application){
        PVEZone pveZone = new PVEZone();
        pveZone.distributionKey(application.distributionKey());
        DataStore dataStore = context.dataStore(serviceDataStore(application));
        dataStore.load(pveZone);
        for(int i=1;i<application.capacity()+1;i++){
            Arena arena = new Arena(pveZone.bucket(),pveZone.oid(),i);
            if(dataStore.load(arena)){
                pveZone.arenas.add(arena);
            }
        }
        pveZone.dataStore(dataStore);
        return (T)pveZone;
    }
    @Override
    public <T extends Recoverable> T load(ServiceContext context,Descriptor application){
        PVEZone pveZone = new PVEZone();
        pveZone.distributionKey(application.distributionKey());
        DataStore dataStore = context.dataStore(serviceDataStore(application),context.partitionNumber());
        dataStore.load(pveZone);
        for(int i=1;i<application.capacity()+1;i++){
            Arena arena = new Arena(pveZone.bucket(),pveZone.oid(),i);
            if(dataStore.load(arena)){
                pveZone.arenas.add(arena);
            }
        }
        pveZone.dataStore(dataStore);
        return (T)pveZone;
    }

    private String serviceDataStore(Descriptor application){
        return application.typeId().replace("-lobby","_service");
    }
}
