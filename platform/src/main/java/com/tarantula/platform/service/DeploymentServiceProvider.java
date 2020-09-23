package com.tarantula.platform.service;

import com.tarantula.*;
import com.tarantula.Module;

import javax.crypto.SecretKey;
import java.util.List;

/**
 * Updated by yinghu lu on 5/30/2020
 */

public interface DeploymentServiceProvider extends ServiceProvider,MetricsListener{

    String DEPLOY_DATA_STORE = "tarantula";
    String SERVER_KEY_SPEC = "AES";

    String NAME = "DeploymentServiceProvider";

    //GAME SERVER APIs
    Connection onConnection(String serverId, Connection.InboundListener listener);
    //void onStartedConnection(String serverId,byte[] started);
    //void onEndedConnection(String serverId);
    SecretKey serverKey();
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
    void register(Configurable configurable);
    void release(Configurable configurable);
    void configure(String key);
    OnView onView(String viewId);
    boolean createView(OnView onView);
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

        void syncKey(String key);

        //game server callbacks
        void onConnection(String serverId,Connection connection);
        byte[] onStartedConnection(String serverId);
        void onUpdatedConnection(String serverId,byte[] updated);
        void onEndedConnection(String serverId,byte[] ended);

    }

}
