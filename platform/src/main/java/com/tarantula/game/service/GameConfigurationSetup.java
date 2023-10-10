package com.tarantula.game.service;

import com.icodesoftware.*;
import com.icodesoftware.service.ApplicationSchema;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.inventory.InventoryQuery;
import com.tarantula.platform.inventory.PlatformInventoryServiceProvider;
import com.tarantula.platform.inventory.UserInventory;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.VersionedConfigurableObject;
import com.tarantula.platform.item.VersionedConfigurableObjectQuery;
import com.icodesoftware.service.ApplicationPreSetup;

import java.util.ArrayList;
import java.util.List;

public class GameConfigurationSetup implements ApplicationPreSetup {


    protected String DS_CONFIG = "configuration";

    protected ServiceContext serviceContext;
    protected Listener listener;
    protected GameCluster gameCluster;

    public GameConfigurationSetup(GameCluster gameCluster){
        this.gameCluster = gameCluster;
        this.listener = gameCluster;
    }
    public <T extends Configurable> boolean save(Descriptor application,T t){
        DataStore dataStore = parentContext.onDataStore(serviceDataStore(application));
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
        return false;
    }

    public <T extends Configurable> boolean load(Descriptor application, T t){
        DataStore dataStore = parentContext.onDataStore(serviceDataStore(application));
        t.dataStore(dataStore);
        return dataStore.load(t);
    }

    public  <T extends Configurable> boolean delete(Descriptor application,T t){
        DataStore dataStore = parentContext.onDataStore(serviceDataStore(application));
        if(!dataStore.delete(t)) return false;
        deleteVersion(dataStore,(ConfigurableObject)t);
        if(listener!=null) listener.onDeleted(application,t);
        return true;
    }
    public <T extends Configurable> List<T> list(Descriptor application, RecoverableFactory<T> recoverableFactory){
        DataStore dataStore = parentContext.onDataStore(serviceDataStore(application));
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
        ret.configurableSetting(((GameCluster)gameCluster).configurableCategories(Configurable.APPLICATION_CONFIG_TYPE));
        return ret.setup();
        //return ret;
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


    protected String query(String type,String category){
        return new StringBuffer().append(type).append(Recoverable.PATH_SEPARATOR).append(category).toString();
    }


    public <T extends Configurable> boolean save(ApplicationSchema gameCluster, T t){
        DataStore dataStore = parentContext.onDataStore(configureDataStore(gameCluster,DS_CONFIG));
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
        DataStore dataStore = parentContext.onDataStore(configureDataStore(gameCluster,DS_CONFIG));
        return dataStore.createEdge(t,label);
    }

    public <T extends Configurable> boolean load(ApplicationSchema gameCluster, T t){
        DataStore dataStore = parentContext.onDataStore(configureDataStore(gameCluster,DS_CONFIG));
        t.dataStore(dataStore);
        return dataStore.load(t);
    }
    public <T extends Configurable> boolean delete(ApplicationSchema gameCluster, T t){
        DataStore dataStore = parentContext.onDataStore(configureDataStore(gameCluster,DS_CONFIG));
        boolean suc = dataStore.delete(t);
        if(suc && this.listener!=null) listener.onDeleted(gameCluster,t);
        return suc;
    }
    public <T extends Configurable> List<T> list(ApplicationSchema gameCluster, RecoverableFactory<T> recoverableFactory){
        DataStore dataStore = parentContext.onDataStore(configureDataStore(gameCluster,DS_CONFIG));
        return dataStore.list(recoverableFactory);
    }
    public DataStore dataStore(ApplicationSchema gameCluster){
        return parentContext.onDataStore(configureDataStore(gameCluster,DS_CONFIG));
    }

    public DataStore dataStore(ApplicationSchema gameCluster,String service){
        return parentContext.onDataStore(configureDataStore(gameCluster,DS_CONFIG));
    }

    public DataStore dataStore(Descriptor application){
        return parentContext.onDataStore(serviceDataStore(application));
    }

    private String configureDataStore(ApplicationSchema application,String suffix){
        String serviceTypeId = application.serviceType();
        return serviceTypeId.replaceAll("-","_")+"_"+suffix;
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
    }

    public void registerListener(Listener listener){
        this.listener = listener;
    }
    private void saveVersion(DataStore dataStore,ConfigurableObject configurableObject){
        VersionedConfigurableObject versionedConfigurableObject = new VersionedConfigurableObject(configurableObject);
        if(dataStore.createIfAbsent(versionedConfigurableObject,true)) return;
        dataStore.update(versionedConfigurableObject);
    }
    private void deleteVersion(DataStore dataStore,ConfigurableObject configurableObject){
        dataStore.list(new VersionedConfigurableObjectQuery(configurableObject.distributionId())).forEach(d->{
            dataStore.delete(d);
        });
        //dataStore.deleteEdge(configurableObject.key(),VersionedConfigurableObject.LABEL);
    }
    public Inventory createInventory(String category,String typeId){
        return gameCluster.createInventory(category,typeId);
    }
    public List<Inventory> inventoryList(long systemId){
        DataStore ids = onDataStore(Inventory.DataStore);
        InventoryQuery query = new InventoryQuery(systemId);
        List<Inventory> inventoryList = new ArrayList<>();
        ids.list(query).forEach(t->{
            t.dataStore(ids);
            t.list();
            t.resetListener(gameCluster);
            inventoryList.add(t);
        });
        return inventoryList;
    }

    public Inventory inventory(long systemId,String typeId){
        DataStore ids = onDataStore(Inventory.DataStore);
        InventoryQuery query = new InventoryQuery(systemId,typeId);
        List<UserInventory> inventoryList = new ArrayList<>();
        ids.list(query).forEach(t->{
            t.dataStore(ids);
            t.list();
            t.resetListener(gameCluster);
            inventoryList.add(t);
        });
        return inventoryList.isEmpty()?null:inventoryList.get(0);
    }
    public Inventory inventory(long inventoryId){
        DataStore ids = onDataStore(Inventory.DataStore);
        UserInventory inventory = new UserInventory();
        inventory.distributionId(inventoryId);
        if(!ids.load(inventory)) return null;
        inventory.dataStore(ids);
        inventory.list();
        inventory.resetListener(gameCluster);
        return inventory;
    }

    @Override
    public DataStore onDataStore(String name) {
        return parentContext.onDataStore(configureDataStore(gameCluster,name));
    }
    @Override
    public void parent(Transaction.DataStoreContext parentContext) {
        this.parentContext = parentContext;
    }
    private Transaction.DataStoreContext parentContext;

    @Override
    public void close() {
        System.out.println("closing resource");
    }
}
