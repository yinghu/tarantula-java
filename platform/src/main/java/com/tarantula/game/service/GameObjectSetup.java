package com.tarantula.game.service;

import com.icodesoftware.*;
import com.tarantula.game.GameZone;

import com.tarantula.platform.GameCluster;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.service.ApplicationPreSetup;

import java.util.ArrayList;
import java.util.List;

abstract public class GameObjectSetup implements ApplicationPreSetup {


    public <T extends Configurable> void save(ApplicationContext context,Descriptor application,T t){
        DataStore dataStore = context.dataStore(serviceDataStore(application));
        IndexSet indexSet = new IndexSet(application.category());
        indexSet.distributionKey(application.distributionKey());
        dataStore.load(indexSet);
        if(!dataStore.update(t)){
            dataStore.create(t);
            indexSet.keySet.add(t.distributionKey());
            dataStore.update(indexSet);
        }
    }
    public <T extends Configurable> void save(ApplicationContext context, GameCluster application, T t){
        DataStore dataStore = context.dataStore(serviceDataStore(application));
        IndexSet indexSet = new IndexSet(GameCluster.TEMPLATE_LABEL);
        indexSet.distributionKey(application.distributionKey());
        dataStore.load(indexSet);
        if(!dataStore.update(t)){
            dataStore.create(t);
            indexSet.keySet.add(t.distributionKey());
            dataStore.update(indexSet);
        }
    }
    public <T extends Configurable> boolean load(ApplicationContext context,Descriptor application,T t){
        DataStore dataStore = context.dataStore(serviceDataStore(application));
        return dataStore.load(t);
    }
    public <T extends Configurable> List<T> list(ApplicationContext context, Descriptor application, RecoverableFactory<T> recoverableFactory){
        DataStore dataStore = context.dataStore(serviceDataStore(application));
        IndexSet indexSet = new IndexSet(application.category());
        indexSet.distributionKey(application.distributionKey());
        ArrayList<T> arrayList = new ArrayList<>();
        if(!dataStore.load(indexSet)){
            return arrayList;
        }
        indexSet.keySet.forEach((k)->{
            T t = recoverableFactory.create();
            t.distributionKey(k);
            if(dataStore.load(t)){
                arrayList.add(t);
            }
        });
        return arrayList;
    }
    public <T extends Configurable> List<T> list(ApplicationContext context, GameCluster application, RecoverableFactory<T> recoverableFactory){
        DataStore dataStore = context.dataStore(serviceDataStore(application));
        IndexSet indexSet = new IndexSet(GameCluster.TEMPLATE_LABEL);
        indexSet.distributionKey(application.distributionKey());
        ArrayList<T> arrayList = new ArrayList<>();
        if(!dataStore.load(indexSet)){
            return arrayList;
        }
        indexSet.keySet.forEach((k)->{
            T t = recoverableFactory.create();
            t.distributionKey(k);
            if(dataStore.load(t)){
                arrayList.add(t);
            }
        });
        return arrayList;
    }
    public byte[] load(ApplicationContext context,GameCluster application,byte[] key){
        DataStore dataStore = context.dataStore(serviceDataStore(application));
        byte[] data = dataStore.backup().get(key);
        return data!=null?data:"{}".getBytes();
    }

    protected String serviceDataStore(Descriptor application){
        return application.typeId().replace("-lobby","_service");
    }
    protected String serviceDataStore(GameCluster application){
        String serviceTypeId = (String) application.property(GameCluster.GAME_SERVICE);
        return serviceTypeId.replaceAll("-","_");
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
}
