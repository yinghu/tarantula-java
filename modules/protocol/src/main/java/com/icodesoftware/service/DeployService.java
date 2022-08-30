package com.icodesoftware.service;

import com.icodesoftware.*;

public interface DeployService extends ServiceProvider {

    String NAME = "DeployService";

    boolean addLobby(Descriptor lobby,String publishingId);
    boolean addView(OnView onView);
    boolean updateView(OnView onView);
    boolean updateResource(String contentUrl,String resourceName);


    boolean onLaunchApplication(String typeId,String applicationId);
    boolean onShutdownApplication(String typeId,String applicationId);
    boolean resetModule(Descriptor descriptor);

    boolean enableLobby(String typeId);
    boolean disableLobby(String typeId);

    void onCreateGameCluster(String gameClusterId);
    boolean onEnableGameCluster(String gameClusterId);
    boolean onDisableGameCluster(String gameClusterId);

    boolean startGameService(String gameClusterKey);
    boolean onLaunchGameCluster(String gameClusterKey);
    boolean onShutdownGameCluster(String gameClusterKey);

    //distribute the module or view content in cluster
    boolean upload(String fileName,byte[] content);

    boolean launchModule(String typeId);
    boolean shutdownModule(String typeId);
    boolean updateModule(Descriptor descriptor);
    boolean deployModule(String context,String moduleFile);

    boolean sync(String key);

    boolean registerChannel(String typeId,Channel channel);
    void ping(String typeId,String serverId);
    void registerConnection(Connection connection);
    void releaseConnection(Connection connection);

    byte[] clusterKey();

    void resetClusterKey();
    void enablePresenceService(String root,String password,String classNameSuffix,String host);
    void disablePresenceService(String classNameSuffix);


}
