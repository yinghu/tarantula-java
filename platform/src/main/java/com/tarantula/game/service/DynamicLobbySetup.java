package com.tarantula.game.service;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.DataStore;
import com.icodesoftware.Descriptor;
import com.icodesoftware.Recoverable;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.Arena;
import com.tarantula.game.DynamicZone;
import com.tarantula.game.GameZone;
import com.tarantula.game.Zone;
import com.tarantula.platform.service.ApplicationPreSetup;

public class DynamicLobbySetup implements ApplicationPreSetup {

    @Override
    public void setup(ServiceContext serviceContext, Descriptor application, String configName) {
        DynamicZone zone = new DynamicZone(application.name(),configName,application.capacity(), GameZone.DEFAULT_LEVEL_COUNT);
        DataStore dataStore = serviceContext.dataStore(serviceDataStore(application),serviceContext.partitionNumber());
        zone.distributionKey(application.distributionKey());
        dataStore.create(zone);
        for(int i = 1; i< Zone.DEFAULT_LEVEL_COUNT+1; i++){
            Arena arena = new Arena(zone.bucket(),zone.oid(),i);
            arena.name("level"+i);
            arena.level = i;
            arena.xp = i* Zone.DEFAULT_LEVEL_UP_BASE;
            arena.disabled(false);
            dataStore.create(arena);
            //zone.arenas.add(arena);
        }
    }

    @Override
    public <T extends Recoverable> T load(ApplicationContext context, Descriptor application) {
        return null;
    }

    @Override
    public <T extends Recoverable> T load(ServiceContext context, Descriptor application) {
        return null;
    }

    private String serviceDataStore(Descriptor application){
        return application.typeId().replace("-lobby","_service");
    }

}
