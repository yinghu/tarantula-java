package com.tarantula.game.service;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.DataStore;
import com.icodesoftware.Descriptor;
import com.icodesoftware.Recoverable;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.PVPZone;
import com.tarantula.platform.service.ApplicationPreSetup;

public class PVPLobbySetup implements ApplicationPreSetup {
    @Override
    public void setup(ServiceContext serviceContext, Descriptor application) {
        //create zone/arena for lobby
        DataStore dataStore = serviceContext.dataStore(serviceDataStore(application),serviceContext.partitionNumber());
        PVPZone pveZone = new PVPZone(application);

    }
    @Override
    public <T extends Recoverable> T load(ApplicationContext context, Descriptor application){

        return (T)new PVPZone(application);
    }
    @Override
    public <T extends Recoverable> T load(ServiceContext context,Descriptor application){
        return (T)new PVPZone(application);
    }
    private String serviceDataStore(Descriptor application){
        return application.typeId().replace("-lobby","_service");
    }
}
