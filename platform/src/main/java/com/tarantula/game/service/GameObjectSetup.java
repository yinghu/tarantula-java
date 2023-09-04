package com.tarantula.game.service;

import com.icodesoftware.*;
import com.icodesoftware.service.ServiceContext;

import com.tarantula.platform.GameCluster;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.VersionedConfigurableObject;
import com.tarantula.platform.item.VersionedConfigurableObjectQuery;
import com.tarantula.platform.service.ApplicationPreSetup;

import java.util.ArrayList;
import java.util.List;

public class GameObjectSetup implements ApplicationPreSetup {


    protected String DS_CONFIG = "configuration";

    protected ServiceContext serviceContext;
    protected Listener listener;
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
        return false;
    }

    public <T extends Configurable> boolean load(Descriptor application, T t){
        DataStore dataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,serviceDataStore(application));
        t.dataStore(dataStore);
        return dataStore.load(t);
    }

    public  <T extends Configurable> boolean delete(Descriptor application,T t){
        DataStore dataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,serviceDataStore(application));
        if(!dataStore.delete(t)) return false;
        dataStore.deleteEdge(t.ownerKey(),t.key(),t.configurationName());
        dataStore.deleteEdge(t.ownerKey(),t.key(),t.configurationTypeId());
        dataStore.deleteEdge(t.ownerKey(),t.key(),t.configurationCategory());
        int superIndex;
        if((superIndex = t.configurationType().indexOf(".")) > 0){
            dataStore.deleteEdge(t.ownerKey(),t.key(),t.configurationType().substring(0,superIndex));
        }
        else{
            dataStore.deleteEdge(t.ownerKey(),t.key(),t.configurationType());
        }
        if((superIndex = t.configurationCategory().indexOf("."))> 0){
            dataStore.deleteEdge(t.ownerKey(),t.key(),t.configurationCategory().substring(0,superIndex));
        }
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


    public <T extends Configurable> boolean save(GameCluster gameCluster, T t){
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
    public <T extends Configurable> boolean edge(GameCluster gameCluster, T t, String label) {
        DataStore dataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,configureDataStore(gameCluster,DS_CONFIG));
        return dataStore.createEdge(t,label);
    }

    public <T extends Configurable> boolean load(GameCluster gameCluster, T t){
        DataStore dataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,configureDataStore(gameCluster,DS_CONFIG));
        t.dataStore(dataStore);
        return dataStore.load(t);
    }
    public <T extends Configurable> boolean delete(GameCluster gameCluster, T t){
        DataStore dataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,configureDataStore(gameCluster,DS_CONFIG));
        boolean suc = dataStore.delete(t.key().asString().getBytes());
        if(suc && this.listener!=null) listener.onDeleted(gameCluster,t);
        return suc;
    }
    public <T extends Configurable> List<T> list(GameCluster gameCluster, RecoverableFactory<T> recoverableFactory){
        DataStore dataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,configureDataStore(gameCluster,DS_CONFIG));
        return dataStore.list(recoverableFactory);
    }
    public DataStore dataStore(GameCluster gameCluster){
        String serviceDataStoreName = gameCluster.serviceType().replaceAll("-","_");
        return serviceContext.dataStore(Distributable.DATA_SCOPE,serviceDataStoreName);
    }

    public DataStore dataStore(GameCluster gameCluster,String service){
        return serviceContext.dataStore(Distributable.DATA_SCOPE,configureDataStore(gameCluster,service));
    }

    public DataStore dataStore(Descriptor descriptor){
        return serviceContext.dataStore(Distributable.DATA_SCOPE,serviceDataStore(descriptor));
    }

    private String configureDataStore(GameCluster application,String suffix){
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
        dataStore.list(new VersionedConfigurableObjectQuery(configurableObject.oid()),v->{
            dataStore.delete(v);
            return true;
        });
        dataStore.deleteEdge(configurableObject.key(),VersionedConfigurableObject.LABEL);
    }
}
