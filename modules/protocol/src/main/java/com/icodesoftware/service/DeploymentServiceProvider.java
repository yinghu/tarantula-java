package com.icodesoftware.service;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.protocol.GameChannelListener;

import java.util.List;

public interface DeploymentServiceProvider extends ConfigurationServiceProvider,MetricsListener{

    String DEPLOY_DATA_STORE = "tarantula";
    String SERVER_KEY_SPEC = "AES";
    String CIPHER_NAME_CBC_PKC5PADDING = "AES/CBC/PKCS5PADDING";
    int KEY_SIZE = 16;

    String NAME = "DeploymentServiceProvider";

    //GAME SERVER APIs

    byte[] serverKey();
    boolean registerChannel(String typeId,Channel channel);
    boolean ping(String typeId,String serverId);


    String registerGameChannelListener(GameChannelListener gameChannelListener);
    void unregisterGameChannelListener(String registerKey);
    //END OF GAME SERVER/PUSH SERVER APIs

    /**
     * Register/Release the service provider on service pool
     * */
    void register(ServiceProvider serviceProvider);
    void release(ServiceProvider serviceProvider);


    OnView onView(String viewId);
    Response createView(OnView onView);
    Response deployResource(String contentUrl,String resourceName);

    Content resource(String name);

    //message publisher
    PostOffice registerPostOffice();
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
    boolean enableApplication(String applicationId);
    boolean disableApplication(String applicationId);
    <T extends OnAccess> T createGameCluster(String owner,String name,String mode,boolean tournamentEnabled);
    <T extends OnAccess> boolean launchGameCluster(T gameCluster);
    <T extends OnAccess> boolean shutdownGameCluster(T gameCluster);
    <T extends OnAccess> T gameCluster(String key);
    <T extends OnAccess> List<T> gameServiceList();
    <T extends OnAccess> T gameService(String name);
    <T extends Configuration,S extends OnAccess> T configuration(S gameCluster,String config);
    Lobby lobby(String typeId);
    //END OF CLUSTER

    //System metrics data
    <T extends OnAccess> T metrics();

    //Access index set operation API
    void registerAccessIndexListener(AccessIndexService.Listener listener);
    AccessIndexService.AccessIndexStore accessIndexStore();

    //data store backup operation API
    void issueDataStoreBackup();
    List<String> listDataStore();
    boolean validDataStore(String dataStore);

    RecoverableListener registerRecoverableListener(String topic,RecoverableListener recoverableListener);
    void unregisterRecoverableListener(String topic);

    DistributionCallback distributionCallback();


    //local callbacks on distributed operations
    interface DistributionCallback{
        <T extends OnAccess> void addGameCluster(T gameCluster);
        <T extends OnAccess> void closeGameCluster(T gameCluster);
        void addLobby(String typeId);
        void removeLobby(String typeId);
        void addApplication(String typeId,String applicationId);
        void removeApplication(String typeId,String applicationId);
        void updateModule(Descriptor descriptor);
        void updateView(OnView onView);
        void updateModule(String contentUrl,String resourceName);
        void updateResource(String contentUrl,String resourceName);

        boolean addChannel(String typeId,Channel channel);
        void addConnection(String typeId,Connection connection);
        boolean pingConnection(String typeId,String serverId);
        void removeConnection(String typeId,Connection connection);

        void stopAccessIndex();
        void startAccessIndex();

        void memberRemoved(String memberId);
        void memberAdded(String memberId);

        void syncKey(String key);

        String registerQueryCallback(RecoverService.QueryCallback queryCallback, RecoverService.QueryEndCallback queryEndCallback);
        RecoverService.QueryCallback queryCallback(String source);
        RecoverService.QueryEndCallback queryEndCallback(String source);
        void removeQueryCallback(String callId);
    }

}
