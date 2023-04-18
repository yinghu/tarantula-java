package com.tarantula.game.service;

import com.icodesoftware.*;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.GameZone;

import com.tarantula.platform.GameCluster;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.service.ApplicationPreSetup;

import java.util.ArrayList;
import java.util.List;

public class GameObjectSetup implements ApplicationPreSetup {


    protected String DS_CONFIG = "configuration";

    protected ServiceContext serviceContext;

    public <T extends Configurable> boolean save(Descriptor application,T t){
        DataStore dataStore = serviceContext.dataStore(serviceDataStore(application),serviceContext.node().partitionNumber());
        t.dataStore(dataStore);
        if(!t.configureAndValidate()){
            return false;
        }
        IndexSet superCategoryIndex = null;
        int superIndex;
        if((superIndex = t.configurationCategory().indexOf(".")) > 0){
            superCategoryIndex = new IndexSet(query("category",t.configurationCategory().substring(0,superIndex)));
            superCategoryIndex.distributionKey(application.distributionKey());
            dataStore.createIfAbsent(superCategoryIndex,true);
        }
        IndexSet categoryIndex = new IndexSet(query("category",t.configurationCategory()));//category/{category}
        categoryIndex.distributionKey(application.distributionKey());
        dataStore.createIfAbsent(categoryIndex,true);

        superIndex = t.configurationType().indexOf(".");
        IndexSet typeIndex = new IndexSet(query("type",superIndex>0?t.configurationType().substring(0,superIndex):t.configurationType()));//type/{asset|commodity|item|application}
        typeIndex.distributionKey(application.distributionKey());
        dataStore.createIfAbsent(typeIndex,true);

        IndexSet typeIdIndex = new IndexSet(query("typeId",t.configurationTypeId()));//typeId app assigned commodity type line Gold
        typeIdIndex.distributionKey(application.distributionKey());
        dataStore.createIfAbsent(typeIdIndex,true);

        IndexSet nameIndex = new IndexSet(query("name",t.configurationName()));//name app assigned name
        nameIndex.distributionKey(application.distributionKey());
        dataStore.createIfAbsent(nameIndex,true);

        if(dataStore.update(t)) return true;
        if(!dataStore.create(t)) return false;
        categoryIndex.addKey(t.distributionKey());
        dataStore.update(categoryIndex);
        typeIndex.addKey(t.distributionKey());
        dataStore.update(typeIndex);
        typeIdIndex.addKey(t.distributionKey());
        dataStore.update(typeIdIndex);
        nameIndex.addKey(t.distributionKey());
        dataStore.update(nameIndex);
        if(superCategoryIndex!=null){
            superCategoryIndex.addKey(t.distributionKey());
            dataStore.update(superCategoryIndex);
        }
        return true;
    }

    public <T extends Configurable> boolean load(Descriptor application,T t){
        DataStore dataStore = serviceContext.dataStore(serviceDataStore(application),serviceContext.node().partitionNumber());
        t.dataStore(dataStore);
        return dataStore.load(t);
    }
    public <T extends Configurable> List<T> list(Descriptor application, RecoverableFactory<T> recoverableFactory){
        DataStore dataStore = serviceContext.dataStore(serviceDataStore(application),serviceContext.node().partitionNumber());
        return list(dataStore,application,recoverableFactory);
    }


    protected String serviceDataStore(Descriptor application){
        if(application.typeId().endsWith("-data")){
            return application.typeId().replace("-data","_service");
        }
        if(application.typeId().endsWith("-lobby")){
            return application.typeId().replace("-lobby","_service");
        }
        if(application.typeId().endsWith("-service")){
            return application.typeId().replace("-service","_service");
        }
        return null;
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
                arrayList.add(t);//convert one of asset, commodity, item
            }
        });
        return arrayList;
    }

    protected String query(String type,String category){
        return new StringBuffer().append(type).append(Recoverable.PATH_SEPARATOR).append(category).toString();
    }


    public <T extends Configurable> boolean save(GameCluster gameCluster, T t){
        DataStore dataStore = serviceContext.dataStore(configurationDataStore(gameCluster,DS_CONFIG),serviceContext.node().partitionNumber());
        if(dataStore.update(t)) return true;
        return dataStore.createIfAbsent(t,false);
    }

    public <T extends Configurable> boolean load(GameCluster gameCluster, T t){
        DataStore dataStore = serviceContext.dataStore(configurationDataStore(gameCluster,DS_CONFIG),serviceContext.node().partitionNumber());
        return dataStore.load(t);
    }

    public <T extends Configurable> List<T> list(GameCluster gameCluster, RecoverableFactory<T> recoverableFactory){
        DataStore dataStore = serviceContext.dataStore(configurationDataStore(gameCluster,DS_CONFIG),serviceContext.node().partitionNumber());
        return dataStore.list(recoverableFactory);
    }
    public DataStore dataStore(GameCluster gameCluster){
        String serviceDataStoreName = ((String)gameCluster.property(GameCluster.GAME_SERVICE)).replace("-","_");
        return serviceContext.dataStore(serviceDataStoreName,serviceContext.node().partitionNumber());
    }

    public DataStore dataStore(GameCluster gameCluster,String service){
        return serviceContext.dataStore(configurationDataStore(gameCluster,service),serviceContext.node().partitionNumber());
    }

    public DataStore dataStore(Descriptor descriptor){
        return serviceContext.dataStore(serviceDataStore(descriptor),serviceContext.node().partitionNumber());
    }

    private String configurationDataStore(GameCluster application,String suffix){
        String serviceTypeId = (String) application.property(GameCluster.GAME_SERVICE);
        return serviceTypeId.replaceAll("-","_")+"_"+suffix;
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
    }
}
