package com.tarantula.game.service;

import com.icodesoftware.*;
import com.icodesoftware.service.ApplicationSchema;


import com.tarantula.platform.GameCluster;
import com.tarantula.platform.inventory.InventoryQuery;
import com.tarantula.platform.inventory.UserInventory;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.VersionedConfigurableObject;
import com.tarantula.platform.item.VersionedConfigurableObjectQuery;
import com.icodesoftware.service.ApplicationPreSetup;

import java.util.ArrayList;
import java.util.List;

public class GameObjectSetup extends GamePreSetup implements ApplicationPreSetup {


    public GameObjectSetup(GameCluster gameCluster){
        super(gameCluster);
    }
    public <T extends Configurable> boolean save(Descriptor application,T t){
        DataStore dataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,serviceDataStore(application));
        t.dataStore(dataStore);
        if(!t.configureAndValidate()){
            return false;
        }
        if(dataStore.update(t)){
            saveVersion(dataStore,(ConfigurableObject)t);
            if(listener!=null) listener.onUpdated(application,t);
            return true;
        }
        if(!dataStore.create(t)) return false;
        t.ownerKey(application.key());
        t.onEdge(true);
        dataStore.createEdge(t,t.configurationCategory());
        dataStore.createEdge(t,t.configurationTypeId());
        dataStore.createEdge(t,t.configurationName());
        int superIndex;
        if((superIndex = t.configurationType().indexOf(".")) > 0){
            dataStore.createEdge(t,t.configurationType().substring(0,superIndex));
        }
        else{
            dataStore.createEdge(t,t.configurationType());
        }
        if((superIndex = t.configurationCategory().indexOf(".")) > 0){
            dataStore.createEdge(t,t.configurationCategory().substring(0,superIndex));
        }
        saveVersion(dataStore,(ConfigurableObject)t);
        if(listener!=null) listener.onCreated(application,t);
        return true;
    }

    @Override
    public <T extends Configurable> boolean edge(Descriptor application, T t, String label) {
        DataStore dataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,serviceDataStore(application));
        return dataStore.createEdge(t,label);
    }

    @Override
    public <T extends Configurable> boolean deleteEdge(Descriptor application, T t, String label) {
        DataStore dataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,serviceDataStore(application));
        return dataStore.deleteEdge(t.ownerKey(),t.key(),label);
    }

    public <T extends Configurable> boolean load(Descriptor application, T t){
        DataStore dataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,serviceDataStore(application));
        t.dataStore(dataStore);
        return dataStore.load(t);
    }

    public  <T extends Configurable> boolean delete(Descriptor application,T t){
        DataStore dataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,serviceDataStore(application));
        if(!dataStore.delete(t)) return false;
        deleteVersion(dataStore,(ConfigurableObject)t);
        if(listener!=null) listener.onDeleted(application,t);
        return true;
    }
    public <T extends Configurable> List<T> list(Descriptor application, RecoverableFactory<T> recoverableFactory){
        DataStore dataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,serviceDataStore(application));
        ArrayList<T> list = new ArrayList<>();
        dataStore.list(recoverableFactory,(c)->{
            c.dataStore(dataStore);
            list.add(c);
            return true;
        });
        return list;
    }
    public Configurable load(Descriptor application,long configurableId){
        ConfigurableObject ret = new ConfigurableObject();
        ret.distributionId(configurableId);
        if(!load(application,ret)) return null;
        ret.configurableSetting(gameCluster.configurableCategories(Configurable.APPLICATION_CONFIG_TYPE));
        return ret.setup();
    }


    public <T extends Configurable> boolean save(ApplicationSchema gameCluster, T t){
        DataStore dataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,configureDataStore(gameCluster,DS_CONFIG));
        if(dataStore.update(t)) {
            if(listener!=null) listener.onUpdated(gameCluster,t);
            return true;
        }
        boolean created = dataStore.createIfAbsent(t,false);
        if(created && this.listener!=null) listener.onCreated(gameCluster,t);
        return created;
    }

    @Override
    public <T extends Configurable> boolean edge(ApplicationSchema gameCluster, T t, String label) {
        DataStore dataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,configureDataStore(gameCluster,DS_CONFIG));
        return dataStore.createEdge(t,label);
    }
    @Override
    public <T extends Configurable> boolean deleteEdge(ApplicationSchema gameCluster, T t, String label) {
        DataStore dataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,configureDataStore(gameCluster,DS_CONFIG));
        return dataStore.deleteEdge(t.ownerKey(),t.key(),label);
    }
    public <T extends Configurable> boolean load(ApplicationSchema gameCluster, T t){
        DataStore dataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,configureDataStore(gameCluster,DS_CONFIG));
        t.dataStore(dataStore);
        return dataStore.load(t);
    }
    public <T extends Configurable> boolean delete(ApplicationSchema gameCluster, T t){
        DataStore dataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,configureDataStore(gameCluster,DS_CONFIG));
        boolean suc = dataStore.delete(t);
        if(suc && this.listener!=null) listener.onDeleted(gameCluster,t);
        return suc;
    }
    public <T extends Configurable> List<T> list(ApplicationSchema gameCluster, RecoverableFactory<T> recoverableFactory){
        DataStore dataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,configureDataStore(gameCluster,DS_CONFIG));
        return dataStore.list(recoverableFactory);
    }
    public DataStore dataStore(ApplicationSchema gameCluster){
        String serviceDataStoreName = gameCluster.serviceType().replaceAll("-","_");
        return serviceContext.dataStore(Distributable.DATA_SCOPE,serviceDataStoreName);
    }

    public DataStore dataStore(ApplicationSchema gameCluster,String service){
        return serviceContext.dataStore(Distributable.DATA_SCOPE,configureDataStore(gameCluster,service));
    }

    public DataStore dataStore(Descriptor descriptor){
        return serviceContext.dataStore(Distributable.DATA_SCOPE,serviceDataStore(descriptor));
    }
    @Override
    public DataStore onDataStore(String name) {
        return serviceContext.dataStore(Distributable.DATA_SCOPE,configureDataStore(gameCluster,name));
    }

    private String configureDataStore(ApplicationSchema application,String suffix){
        String serviceTypeId = application.serviceType();
        return serviceTypeId.replaceAll("-","_")+"_"+suffix;
    }


    private void saveVersion(DataStore dataStore,ConfigurableObject configurableObject){
        VersionedConfigurableObject versionedConfigurableObject = new VersionedConfigurableObject(configurableObject);
        if(dataStore.createIfAbsent(versionedConfigurableObject,true)) return;
        dataStore.update(versionedConfigurableObject);
    }
    private void deleteVersion(DataStore dataStore,ConfigurableObject configurableObject){
        dataStore.list(new VersionedConfigurableObjectQuery(configurableObject.distributionId()),v->{
            dataStore.delete(v);
            return true;
        });
        //dataStore.deleteEdge(configurableObject.key(),VersionedConfigurableObject.LABEL);
    }

    public Inventory createInventory(String category, String typeId){
        return gameCluster.createInventory(this,category,typeId);
    }
    public List<Inventory> inventoryList(long systemId){
        DataStore ids = serviceContext.dataStore(Distributable.DATA_SCOPE,configureDataStore(gameCluster,Inventory.DataStore));
        InventoryQuery query = new InventoryQuery(systemId);
        List<Inventory> inventoryList = new ArrayList<>();
        ids.list(query).forEach(t->{
            t.dataStore(ids);
            t.resetListener(gameCluster);
            t.applicationPreSetup(this);
            t.list();
            inventoryList.add(t);
        });
        return inventoryList;
    }
    public Inventory inventory(long systemId,String typeId){
        DataStore ids = serviceContext.dataStore(Distributable.DATA_SCOPE,configureDataStore(gameCluster,Inventory.DataStore));
        InventoryQuery query = new InventoryQuery(systemId,typeId);
        List<Inventory> inventoryList = new ArrayList<>();
        ids.list(query).forEach(t->{
            t.dataStore(ids);
            t.resetListener(gameCluster);
            t.applicationPreSetup(this);
            t.list();
            inventoryList.add(t);
        });
        return inventoryList.isEmpty()?null:inventoryList.get(0);
    }
    public Inventory inventory(long inventoryId){
        DataStore ids = serviceContext.dataStore(Distributable.DATA_SCOPE,configureDataStore(gameCluster,Inventory.DataStore));
        UserInventory inventory = new UserInventory();
        inventory.distributionId(inventoryId);
        if(!ids.load(inventory)) return null;
        inventory.dataStore(ids);
        inventory.applicationPreSetup(this);
        inventory.resetListener(gameCluster);
        inventory.list();
        return inventory;
    }


}
