package com.icodesoftware.service;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.protocol.Channel;
import com.icodesoftware.protocol.GameServerListener;

import java.util.List;

public interface DeploymentServiceProvider extends ConfigurationServiceProvider,MetricsListener,EndPoint.Listener{

    String DEPLOY_DATA_STORE = "tarantula";
    String SERVER_KEY_SPEC = "AES";
    String CIPHER_NAME_CBC_PKC5PADDING = "AES/CBC/PKCS5PADDING";
    int KEY_SIZE = 16;

    String NAME = "DeploymentServiceProvider";

    //GAME SERVER APIs
    OnAccess registerConnection(Connection connection);
    boolean registerChannel(Channel channel);

    void updateRoom(String typeId,String lobby,byte[] payload);
    void startConnection(Connection connection);
    void stopConnection(Connection connection);
    void verifyConnection(String typeId,String serverId);
    byte[] serverKey(String typeId);


    String registerGameServerListener(GameServerListener gameServerListener);
    void unregisterGameServerListener(String registerKey);
    //END OF GAME SERVER/PUSH SERVER APIs

    /**
     * Register/Release the service provider on service pool
     * */
    void register(ServiceProvider serviceProvider);
    void release(ServiceProvider serviceProvider);


    OnView view(String viewId);
    Response createView(OnView onView);
    Response deployResource(String contentUrl,String resourceName);

    Content resource(String name);
    void deleteResource(String name);
    String resetCode(String key);
    String checkCode(String resetCode);
    //end

    //Module operation APIs
    Module module(Descriptor descriptor);
    void resource(Descriptor descriptor, String name, Module.OnResource onResource);
    Response deployModule(String contextUrl,String resourceName);
    Response createModule(Descriptor descriptor);
    Response exportModule(Descriptor descriptor);
    boolean launchModule(String typeId);
    boolean resetModule(Descriptor descriptor);
    boolean shutdownModule(String typeId);
    ClassLoader classLoader(String moduleId);
    //end

    //game cluster operation APIs
    boolean createApplication(Descriptor descriptor,String postSetup,String configName,boolean launching);

    boolean updateApplication(Descriptor descriptor,OnAccess properties);
    boolean enableApplication(String applicationId);
    boolean disableApplication(String applicationId);

    <T extends OnAccess> T createGameCluster(String owner,String name,OnAccess properties);
    <T extends OnAccess> T updateGameCluster(String gameClusterId,OnAccess properties);


    <T extends OnAccess> boolean launchGameCluster(T gameCluster);
    <T extends OnAccess> boolean shutdownGameCluster(T gameCluster);
    <T extends OnAccess> T gameCluster(String key);
    List<Descriptor> gameServiceList();
    <T extends Configuration,S extends OnAccess> T configuration(S gameCluster,String config);
    Lobby lobby(String typeId);
    //END OF CLUSTER


    //Access index set operation API
    void registerAccessIndexListener(AccessIndexService.Listener listener);
    AccessIndexService.AccessIndexStore accessIndexStore();
    KeyIndexService.KeyIndexStore keyIndexStore();
    //data store backup operation API
    void issueDataStoreBackup();
    List<String> listDataStore();
    List<String> listServiceView();
    List<String> listMetricsView();
    DataStoreSummary validDataStore(String dataStore);
    ClusterProvider.Summary clusterSummary();

    DistributionCallback distributionCallback();


    //local callbacks on distributed operations
    interface DistributionCallback{

        void onGameServiceStarted(String gameClusterId);

        void onGameClusterLaunched(String gameClusterId);
        void onGameClusterShutdown(String gameClusterId);
        void onGameClusterCreated(String gameClusterId);

        void onModuleLaunched(String typeId);
        void onModuleShutdown(String typeId);
        void onModuleUpdated(Descriptor descriptor);
        void onModuleDeployed(String contentUrl,String resourceName);

        void onApplicationLaunched(String typeId,String applicationId);
        void onApplicationShutdown(String typeId,String applicationId);

        void onViewUpdated(OnView onView);

        void onResourceUpdated(String contentUrl,String resourceName);

        void onConnectionRegistered(String typeId,Connection connection);
        void onConnectionStarted(String typeId,Connection connection);
        void onConnectionVerified(String typeId,String serverId);
        void onConnectionReleased(String typeId,Connection connection);

        void onAccessIndexDisabled();
        void onAccessIndexEnabled();

        void onConfigurableUpdated(String key);

    }

}
