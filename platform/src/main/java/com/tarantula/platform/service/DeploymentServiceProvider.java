package com.tarantula.platform.service;

import com.tarantula.*;
import com.tarantula.Module;

import java.util.List;

/**
 * Updated by yinghu lu on 5/30/2020
 */

public interface DeploymentServiceProvider extends ServiceProvider,MetricsListener {

    String DEPLOY_DATA_STORE = "tarantula";

    String NAME = "DeploymentServiceProvider";

    //UDP SERVER APIs
    void onUDPConnection(String typeId,Connection connection);
    Connection onUDPConnection(String typeId, Connection.StateListener listener);
    void onStartedUDPConnection(String serverId,byte[] started);
    void onUpdatedUDPConnection(String serverId,byte[] updated);
    void onEndedUDPConnection(String serverId,byte[] ended);
    void onEndedUDPConnection(String serverId);
    byte[] onStartedUDPConnection(String serverId);
    //END OF DEDICATED SERVER APIs

    /**
     * Register/Release the service provider on service pool
     * */
    void register(ServiceProvider serviceProvider);
    void release(ServiceProvider serviceProvider);


    //deploy and callback instance registry
    void registerInstanceRegistryListener(InstanceRegistry.Listener deploymentListener);

    //register server push listener
    void registerOnConnectionListener(Connection.Listener listener);

    //list configurations
    List<Configuration> configuration();
    OnView onView(String viewId);

    void registerOnLobbyListener(OnLobby.Listener onLobbyListener);

    byte[] resource(String name,String flag);

    //message publisher
    PostOffice registerPostOffice();
    String resetCode(String key);
    String checkCode(String resetCode);

    //Module and application operation API
    Module module(Descriptor descriptor);
    void resource(Descriptor descriptor, String name, Module.OnResource onResource);
    boolean createModule(Descriptor descriptor);
    boolean launchModule(String typeId);
    boolean resetModule(Descriptor descriptor);
    boolean shutdownModule(String typeId);

    boolean createApplication(Descriptor descriptor,boolean launching);
    boolean enableApplication(String applicationId,boolean enabled);

    //END OF Module API

    //GAME CLUSTER APIs
    <T extends OnAccess> T createGameCluster(String owner,String name);
    <T extends OnAccess> boolean launchGameCluster(T gameCluster);
    <T extends OnAccess> boolean shutdownGameCluster(T gameCluster);
    <T extends OnAccess> T gameCluster(String key);

    Lobby lobby(String typeId);
    //END OF CLUSTER

    //System metrics data
    <T extends OnAccess> T metrics();

    //Access index set operation API
    void registerAccessIndexListener(AccessIndexService.Listener listener);

    //data store backup operation API
    void issueDataStoreBackup();

    DistributionCallback distributionCallback();

    //local callbacks on distributed operations
    interface DistributionCallback{
        void addLobby(String typeId);
        void removeLobby(String typeId);
        void addApplication(String typeId,String applicationId);
        void removeApplication(String typeId,String applicationId);
        void updateModule(Descriptor descriptor);

        void registerServerPushEvent(Event event);
        void releaseServerPushEvent(String serverId);
        void syncServerPushEvent(String memberId);

        void stopAccessIndex();
        void startAccessIndex();

        void memberRemoved(String memberId);
        void memberAdded(String memberId);

    }

}
