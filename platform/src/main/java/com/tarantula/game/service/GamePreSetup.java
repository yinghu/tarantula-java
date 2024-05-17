package com.tarantula.game.service;

import com.icodesoftware.Descriptor;
import com.icodesoftware.Inventory;
import com.icodesoftware.Recoverable;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.GameCluster;

public class GamePreSetup{

    protected String DS_CONFIG = "configuration";

    protected ServiceContext serviceContext;
    protected ApplicationPreSetup.Listener listener;
    protected GameCluster gameCluster;

    public GamePreSetup(GameCluster gameCluster){
        this.gameCluster = gameCluster;
        this.listener = gameCluster;
    }

    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
    }

    public void registerListener(ApplicationPreSetup.Listener listener){
        this.listener = listener;
    }

    public long distributionId(){
        return serviceContext.distributionId();
    }

    public Recoverable create(int factoryId, int classId){
        return serviceContext.recoverableRegistry(factoryId).create(classId);
    }

    protected String serviceDataStore(Descriptor application){
        if(application.typeId().endsWith("-data")){
            return application.typeId().replaceAll("-","_").replace("data","service");
        }
        if(application.typeId().endsWith("-lobby")){
            return application.typeId().replaceAll("-","_").replace("lobby","service");
        }
        if(application.typeId().endsWith("-service")){
            return application.typeId().replaceAll("-","_");
        }
        return null;
    }
}
