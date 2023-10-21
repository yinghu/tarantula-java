package com.icodesoftware.service;

import com.icodesoftware.*;

import java.util.List;

public interface ApplicationPreSetup extends Transaction.DataStoreContext{

    <T extends Configurable> boolean save(Descriptor application, T t);

    <T extends Configurable> boolean edge(Descriptor application, T t,String label);
    <T extends Configurable> boolean deleteEdge(Descriptor application, T t,String label);
    <T extends Configurable> boolean load(Descriptor application,T t);
    <T extends Configurable> boolean delete(Descriptor application,T t);
    <T extends Configurable> List<T> list(Descriptor application, RecoverableFactory<T> recoverableFactory);

    <T extends Configurable> boolean save(ApplicationSchema gameCluster, T t);

    <T extends Configurable> boolean edge(ApplicationSchema gameCluster, T t, String label);
    <T extends Configurable> boolean deleteEdge(ApplicationSchema gameCluster, T t, String label);
    <T extends Configurable> boolean load(ApplicationSchema gameCluster, T t);
    <T extends Configurable> boolean delete(ApplicationSchema gameCluster, T t);

    <T extends Configurable> List<T> list(ApplicationSchema gameCluster, RecoverableFactory<T> recoverableFactory);

    //{game}_service data store
    DataStore dataStore(ApplicationSchema gameCluster);

    //{game}_service_{suffix_service}
    DataStore dataStore(ApplicationSchema gameCluster,String service);

    //{game}_service data store
    DataStore dataStore(Descriptor descriptor);

    void setup(ServiceContext serviceContext);

    //
    //
    Inventory createInventory(String category,String typeId);
    List<Inventory> inventoryList(long systemId);
    Inventory inventory(long systemId,String typeId);
    Inventory inventory(long inventoryId);
    Configurable load(Descriptor application,long configurableId);

    long distributionId();

    Recoverable create(int factoryId,int classId);

    interface Listener{
        default <T extends Configurable> void onUpdated(Descriptor application,T t){};
        default <T extends Configurable> void onCreated(Descriptor application,T t){};
        default <T extends Configurable> void onDeleted(Descriptor application,T t){};
        default <T extends Configurable> void onUpdated(ApplicationSchema application,T t){};
        default <T extends Configurable> void onCreated(ApplicationSchema application,T t){};

        default <T extends Configurable> void onDeleted(ApplicationSchema application,T t){};
    }
}
