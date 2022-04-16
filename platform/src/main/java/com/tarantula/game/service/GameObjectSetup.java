package com.tarantula.game.service;

import com.icodesoftware.*;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.GameZone;

import com.tarantula.platform.GameCluster;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.service.ApplicationPreSetup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

abstract public class GameObjectSetup implements ApplicationPreSetup {

    protected DataStore _dataStore;

    public <T extends Configurable> boolean save(ApplicationContext context,Descriptor application,T t){
        DataStore dataStore = _dataStore!=null?_dataStore:context.dataStore(serviceDataStore(application));
        t.dataStore(dataStore);
        if(!t.configureAndValidate()){
            return false;
        }
        IndexSet indexSet = new IndexSet(query("category",t.configurationCategory()));//category/{category}
        indexSet.distributionKey(application.distributionKey());
        dataStore.createIfAbsent(indexSet,true);

        IndexSet typeIndex = new IndexSet(query("type",t.configurationType()));//type/{asset|commodity|item|application}
        typeIndex.distributionKey(application.distributionKey());
        dataStore.createIfAbsent(typeIndex,true);

        IndexSet typeIdIndex = new IndexSet(query("typeId",t.configurationTypeId()));//typeId/{asset|commodity|item|application}
        typeIdIndex.distributionKey(application.distributionKey());
        dataStore.createIfAbsent(typeIdIndex,true);

        IndexSet nameIndex = new IndexSet(query("name",t.configurationName()));//name/{asset|commodity|item|application}
        nameIndex.distributionKey(application.distributionKey());
        dataStore.createIfAbsent(nameIndex,true);

        if(!dataStore.update(t)){
            dataStore.create(t);
            indexSet.addKey(t.distributionKey());
            dataStore.update(indexSet);
            typeIndex.addKey(t.distributionKey());
            dataStore.update(typeIndex);
            typeIdIndex.addKey(t.distributionKey());
            dataStore.update(typeIdIndex);
            nameIndex.addKey(t.distributionKey());
            dataStore.update(nameIndex);
        }
        return true;
    }

    public <T extends Configurable> boolean load(ApplicationContext context,Descriptor application,T t){
        DataStore dataStore = _dataStore!=null?_dataStore:context.dataStore(serviceDataStore(application));
        t.dataStore(dataStore);
        return dataStore.load(t);
    }
    public <T extends Configurable> List<T> list(ApplicationContext context, Descriptor application, RecoverableFactory<T> recoverableFactory){
        DataStore dataStore = _dataStore!=null?_dataStore:context.dataStore(serviceDataStore(application));
        return list(dataStore,application,recoverableFactory);
    }

    public <T extends Configurable> List<T> list(ServiceContext context, Descriptor application, RecoverableFactory<T> recoverableFactory){
        DataStore dataStore = _dataStore!=null?_dataStore:context.dataStore(this.serviceDataStore(application),context.partitionNumber());
        return list(dataStore,application,recoverableFactory);
    }

    public Set<String> list(ApplicationContext context, Descriptor application){
        DataStore dataStore = _dataStore!=null?_dataStore:context.dataStore(serviceDataStore(application));
        IndexSet referenceSet = new IndexSet("reference");
        referenceSet.distributionKey(application.distributionKey());
        dataStore.load(referenceSet);
        return referenceSet.keySet();
    }


    public <T extends Configurable> boolean load(ServiceContext context,Descriptor application,T t){
        DataStore dataStore = _dataStore!=null?_dataStore:context.dataStore(this.serviceDataStore(application),context.partitionNumber());
        t.dataStore(dataStore);
        return dataStore.load(t);
    }

    protected String serviceDataStore(Descriptor application){
        String replaced = application.typeId().endsWith("-lobby")?"-lobby":"-service";
        return application.typeId().replace(replaced,"_service");
    }

    protected GameZone.RoomProxy joinProxy(String playMode){
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

    protected <T extends Configurable> List<T> list(DataStore dataStore, Descriptor application, RecoverableFactory<T> recoverableFactory){
        IndexSet indexSet = new IndexSet(recoverableFactory.label());
        indexSet.distributionKey(application.distributionKey());
        ArrayList<T> arrayList = new ArrayList<>();
        if(!dataStore.load(indexSet)){
            return arrayList;
        }
        indexSet.keySet().forEach((k)->{
            T t = recoverableFactory.create();
            t.distributionKey(k);
            if(dataStore.load(t)){
                t.dataStore(dataStore);
                arrayList.add(t.setup());//convert one of asset, commodity, item
            }
        });
        return arrayList;
    }

    protected String query(String type,String category){
        return new StringBuffer().append(type).append(Recoverable.PATH_SEPARATOR).append(category).toString();
    }


    public <T extends Configurable> boolean save(ApplicationContext context, GameCluster gameCluster, T t){
        DataStore dataStore = context.dataStore(configurationDataStore(gameCluster));
        return dataStore.update(t);
    }
    public <T extends Configurable> boolean load(ApplicationContext context, GameCluster gameCluster, T t){
        DataStore dataStore = context.dataStore(configurationDataStore(gameCluster));
        return dataStore.load(t);
    }
    public <T extends Configurable> List<T> list(ApplicationContext context, GameCluster gameCluster, RecoverableFactory<T> recoverableFactory){
        DataStore dataStore = context.dataStore(configurationDataStore(gameCluster));
        return dataStore.list(recoverableFactory);
    }

    private String configurationDataStore(GameCluster application){
        String serviceTypeId = (String) application.property(GameCluster.GAME_SERVICE);
        return serviceTypeId.replaceAll("-","_")+"_configuration";
    }
}
