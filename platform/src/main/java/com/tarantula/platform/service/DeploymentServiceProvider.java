package com.tarantula.platform.service;

import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.presence.GameCluster;

import java.io.InputStream;

/**
 * Updated by yinghu lu on 6/15/2019.
 */

public interface DeploymentServiceProvider extends ServiceProvider,MetricsListener {

    String DEPLOY_TOPIC = "tarantula-deployment";
    String DEPLOY_DATA_STORE = "tarantula";

    String NAME = "DeploymentServiceProvider";
    enum Mode{
        ALL,
        PRESENCE,
        APPLICATION
    }
    Mode deploymentMode();
    void clusterUpdated(int scope,String nodeId,boolean state);

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
    boolean createLobby(Descriptor descriptor);
    boolean createApplication(Descriptor descriptor);
    boolean enableApplication(String applicationId,boolean enabled);
    boolean launch(String typeId);
    boolean shutdown(String typeId);
    Module module(Descriptor descriptor);
    void resource(Descriptor descriptor, String name, Module.OnResource onResource);
    boolean reset(Descriptor descriptor);
    boolean createModule(Descriptor descriptor);
    //END OF MODULE OPERATION API

    //GAME CLUSTER APIs
    <T extends OnAccess> T createGameCluster(String owner,String name,String plan);
    <T extends OnAccess> void launchGameCluster(T gameCluster);
    <T extends OnAccess> void shutdownGameCluster(T gameCluster);
    <T extends OnAccess> T gameCluster(String key);
    Lobby lobby(String typeId);
    //END OF CLUSTER

    //System metrics data
    <T extends OnAccess> T metrics();
}
