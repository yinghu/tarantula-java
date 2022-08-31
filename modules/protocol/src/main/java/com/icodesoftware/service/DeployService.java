package com.icodesoftware.service;

import com.icodesoftware.*;

public interface DeployService extends ServiceProvider {

    String NAME = "DeployService";

    boolean addLobby(Descriptor lobby,String publishingId);

    boolean onUpdateView(OnView onView);
    boolean onUpdateResource(String contentUrl,String resourceName);


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
    boolean onUpload(String fileName,byte[] content);

    boolean launchModule(String typeId);
    boolean shutdownModule(String typeId);
    boolean updateModule(Descriptor descriptor);
    boolean deployModule(String context,String moduleFile);

    boolean onUpdateConfigurable(String key);

    boolean onRegisterChannel(String typeId,Channel channel);
    void onVerifyConnection(String typeId,String serverId);
    void onRegisterConnection(Connection connection);
    void onReleaseConnection(Connection connection);

    byte[] onClusterKey();

    void onResetClusterKey();
    void onEnablePresenceService(String root,String password,String classNameSuffix,String host);
    void onDisablePresenceService(String classNameSuffix);


}
