package com.tarantula.platform.service;

import com.tarantula.*;
import com.tarantula.Module;

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
     * Deploys the service provider on service pool
     * */
    boolean deploy(ServiceProvider serviceProvider);
    void release(ServiceProvider serviceProvider);

    //deploy and callback instance registry
    void registerInstanceRegistryListener(InstanceRegistry.Listener deploymentListener);
    void deploy(InstanceRegistry registry);

    void addServerPushEvent(Event event);
    void removeConnection(String serverId);
    void registerOnConnectionListener(Connection.Listener listener);


    //deploy and callback configuration
    void deploy(Configuration configuration);

    void registerConfigurationListener(Configuration.Listener listener);

    //deploy and callback on view

    boolean deploy(OnView onView);
    void update(OnView onView);
    OnView invalidView();
    void registerOnViewListener(OnView.Listener onViewListener);

    //deploy and callback on lobby
    void deployLobby(String typeId);
    void shutdownLobby(String typeId);
    void deployApplication(String applicationId,String typeId);
    void shutdownApplication(String applicationId,String typeId);
    void deploy(OnLobby onLobby);

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
    void update(Descriptor descriptor);
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
    void stopAccessIndex();
    void startAccessIndex();
    void registerAccessIndexListener(AccessIndexService.Listener listener);

    //data store backup operation API
    void issueDataStoreBackup();

    //DeployService deployService(int scope);

}
