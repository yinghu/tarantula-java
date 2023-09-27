package com.tarantula.platform.service;

import com.icodesoftware.*;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.GameCluster;

import java.util.List;

public interface ApplicationPreSetup extends Transaction.DataStoreContext {

    String SET_UP_NAME = "applicationPreSetup";

    <T extends Configurable> boolean save(Descriptor application,T t);

    <T extends Configurable> boolean edge(Descriptor application, T t,String label);
    <T extends Configurable> boolean load(Descriptor application,T t);
    <T extends Configurable> boolean delete(Descriptor application,T t);
    <T extends Configurable> List<T> list(Descriptor application, RecoverableFactory<T> recoverableFactory);


    //[game]_service_configuration data store
    <T extends Configurable> boolean save(GameCluster gameCluster, T t);

    <T extends Configurable> boolean edge(GameCluster gameCluster, T t,String label);

    <T extends Configurable> boolean load(GameCluster gameCluster, T t);
    <T extends Configurable> boolean delete(GameCluster gameCluster, T t);
    <T extends Configurable> List<T> list(GameCluster gameCluster, RecoverableFactory<T> recoverableFactory);

    //{game}_service data store
    DataStore dataStore(GameCluster gameCluster);

    //{game}_service_{suffix_service}
    DataStore dataStore(GameCluster gameCluster,String service);

    //{game}_service data store
    DataStore dataStore(Descriptor descriptor);

    void setup(ServiceContext serviceContext);

    void registerGameCluster(GameCluster gameCluster);
    void registerListener(Listener listener);

    interface Listener{
        default <T extends Configurable> void onUpdated(Descriptor application,T t){};
        default <T extends Configurable> void onCreated(Descriptor application,T t){};
        default <T extends Configurable> void onDeleted(Descriptor application,T t){};
        default <T extends Configurable> void onUpdated(GameCluster application,T t){};
        default <T extends Configurable> void onCreated(GameCluster application,T t){};

        default <T extends Configurable> void onDeleted(GameCluster application,T t){};
    }

}
