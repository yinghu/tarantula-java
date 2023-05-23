package com.tarantula.game.service;

import com.icodesoftware.*;
import com.icodesoftware.service.ServiceContext;

import com.tarantula.platform.GameCluster;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.VersionedConfigurableObject;
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

        if(dataStore.update(t)){
            saveVersion(dataStore,(ConfigurableObject)t);
            return true;
        }
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
        saveVersion(dataStore,(ConfigurableObject)t);
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
            return application.typeId().replaceAll("-data","_service");
        }
        if(application.typeId().endsWith("-lobby")){
            return application.typeId().replaceAll("-lobby","_service");
        }
        if(application.typeId().endsWith("-service")){
            return application.typeId().replaceAll("-service","_service");
        }
        return null;
    }


    protected <T extends Configurable> List<T> list(DataStore dataStore, Descriptor application, RecoverableFactory<T> recoverableFactory){
        IndexSet indexSet = new IndexSet(recoverableFactory.label());
        indexSet.distributionKey(recoverableFactory.distributionKey()==null?application.distributionKey():recoverableFactory.distributionKey());
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
        DataStore dataStore = serviceContext.dataStore(configureDataStore(gameCluster,DS_CONFIG),serviceContext.node().partitionNumber());
        if(dataStore.update(t)) return true;
        return dataStore.createIfAbsent(t,false);
    }

    public <T extends Configurable> boolean load(GameCluster gameCluster, T t){
        DataStore dataStore = serviceContext.dataStore(configureDataStore(gameCluster,DS_CONFIG),serviceContext.node().partitionNumber());
        return dataStore.load(t);
    }

    public <T extends Configurable> List<T> list(GameCluster gameCluster, RecoverableFactory<T> recoverableFactory){
        DataStore dataStore = serviceContext.dataStore(configureDataStore(gameCluster,DS_CONFIG),serviceContext.node().partitionNumber());
        return dataStore.list(recoverableFactory);
    }
    public DataStore dataStore(GameCluster gameCluster){
        String serviceDataStoreName = gameCluster.serviceType().replaceAll("-","_");
        return serviceContext.dataStore(serviceDataStoreName,serviceContext.node().partitionNumber());
    }

    public DataStore dataStore(GameCluster gameCluster,String service){
        return serviceContext.dataStore(configureDataStore(gameCluster,service),serviceContext.node().partitionNumber());
    }

    public DataStore dataStore(Descriptor descriptor){
        return serviceContext.dataStore(serviceDataStore(descriptor),serviceContext.node().partitionNumber());
    }

    private String configureDataStore(GameCluster application,String suffix){
        String serviceTypeId = application.serviceType();
        return serviceTypeId.replaceAll("-","_")+"_"+suffix;
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
    }

    private void saveVersion(DataStore dataStore,ConfigurableObject configurableObject){
        IndexSet versionIndex = new IndexSet("version");
        versionIndex.distributionKey(configurableObject.distributionKey());
        dataStore.createIfAbsent(versionIndex,true);
        VersionedConfigurableObject versionedConfigurableObject = new VersionedConfigurableObject(configurableObject);
        if(dataStore.createIfAbsent(versionedConfigurableObject,true)){
            versionIndex.addKey(versionedConfigurableObject.key().asString());
            dataStore.update(versionIndex);
        }
        else{
            dataStore.update(versionedConfigurableObject);
        }
    }
}
