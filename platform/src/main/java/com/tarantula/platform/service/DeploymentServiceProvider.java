package com.tarantula.platform.service;

import com.tarantula.*;
import com.tarantula.Module;

import java.io.InputStream;

/**
 * Updated by yinghu lu on 5/30/2020
 */

public interface DeploymentServiceProvider extends ServiceProvider,MetricsListener {

    String DEPLOY_TOPIC = "tarantula-deployment";
    String DEPLOY_DATA_STORE = "tarantula";

    String NAME = "DeploymentServiceProvider";

    void clusterUpdated(int scope,String nodeId,boolean state);
    int clusterPartitionCount();
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

    void registerOnConnectionListener(Connection.Listener listener);
    //deploy and callback configuration
    void deploy(Configuration configuration);
    void registerConfigurationListener(Configuration.Listener listener);

    //deploy and callback on view

    boolean deploy(OnView onView);
    OnView invalidView();
    void registerOnViewListener(OnView.Listener onViewListener);

    //deploy and callback on lobby
    void deploy(OnLobby onLobby);

    void registerOnLobbyListener(OnLobby.Listener onLobbyListener);

    byte[] resource(String name,String flag);

    //message publisher
    PostOffice registerPostOffice();
    String resetCode(String key);
    String checkCode(String resetCode);

    //Module application operation API
    String upload(InputStream inputStream,String fname) throws Exception;
    //boolean createLobby(Descriptor descriptor);
    Module module(Descriptor descriptor);
    void resource(Descriptor descriptor, String name, Module.OnResource onResource);
    boolean reset(Descriptor descriptor);
    boolean createModule(Descriptor descriptor);

    //END OF MODULE OPERATION API

    boolean createApplication(Descriptor descriptor,boolean launching);
    boolean enableApplication(String applicationId,boolean enabled);
    boolean launch(String typeId);
    boolean shutdown(String typeId);

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

}
