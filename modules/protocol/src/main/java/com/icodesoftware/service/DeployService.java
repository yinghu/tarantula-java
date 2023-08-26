package com.icodesoftware.service;

import com.icodesoftware.*;

public interface DeployService extends ServiceProvider {

    String NAME = "DeployService";

    boolean onUpdateView(OnView onView);
    boolean onUpdateResource(String contentUrl,String resourceName);


    boolean onLaunchApplication(String typeId,long applicationId);
    boolean onShutdownApplication(String typeId,long applicationId);


    void onCreateGameCluster(String gameClusterId);

    boolean onStartGameService(String gameClusterKey);
    boolean onLaunchGameCluster(String gameClusterKey);
    boolean onShutdownGameCluster(String gameClusterKey);

    //distribute the module or view content in cluster
    boolean onUpload(String fileName,byte[] content);

    boolean onLaunchModule(String typeId);
    boolean onShutdownModule(String typeId);
    boolean onUpdateModule(Descriptor descriptor);
    boolean onDeployModule(String context,String moduleFile);

    boolean onUpdateConfigurable(String key);

    //boolean onRegisterChannel(String typeId,Channel channel);
    void onVerifyConnection(String typeId,String serverId);
    void onRegisterConnection(Connection connection);
    void onStartConnection(Connection connection);
    void onReleaseConnection(Connection connection);

    byte[] onClusterKey();

    void onResetClusterKey();
    void onEnablePresenceService(String root,String password,String classNameSuffix,String host);
    void onDisablePresenceService(String classNameSuffix);


}
